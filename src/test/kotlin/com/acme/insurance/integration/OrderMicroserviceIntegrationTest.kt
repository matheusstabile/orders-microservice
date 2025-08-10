package com.acme.insurance.integration

import com.acme.insurance.adapters.output.persistence.mongodb.converter.toDocument
import com.acme.insurance.adapters.output.persistence.mongodb.repository.MongoDbRepository
import com.acme.insurance.core.application.converter.toDomain
import com.acme.insurance.core.application.dto.CreateOrderResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.util.Mocks
import com.redis.testcontainers.RedisContainer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderMicroserviceIntegrationTest {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var mongoDbRepository: MongoDbRepository

    companion object {
        private val mongoDBContainer =
            MongoDBContainer(DockerImageName.parse("mongo:latest"))
                .withEnv("CONNECT_TIMEOUT_MS", "3000")
                .withEnv("SOCKET_TIMEOUT_MS", "3000")
                .withEnv("SERVER_SELECTION_TIMEOUT_MS", "3000")
        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.15"))
        private val redisContainer = RedisContainer(DockerImageName.parse("redis:latest"))
        private val mockServerContainer =
            MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"))
                .withCopyFileToContainer(
                    MountableFile.forHostPath("mockserver-config/init.json"),
                    "/config/init.json",
                ).withEnv("MOCKSERVER_INITIALIZATION_JSON_PATH", "/config/init.json")

        @JvmStatic
        @BeforeAll
        fun startContainers() {
            mongoDBContainer.start()
            kafkaContainer.start()
            redisContainer.start()
            mockServerContainer.start()
            createKafkaTopics(kafkaContainer.bootstrapServers)
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
            registry.add("spring.kafka.bootstrap-servers") { kafkaContainer.bootstrapServers }
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.redisPort }
            registry.add("external.mockserver.url") { mockServerContainer.endpoint }
        }

        @JvmStatic
        fun createKafkaTopics(bootstrapServers: String) {
            val props = mapOf("bootstrap.servers" to bootstrapServers)
            AdminClient.create(props).use { admin ->
                val topics =
                    listOf(
                        NewTopic("orders-topic", 1, 1),
                        NewTopic("payments-topic", 1, 1),
                        NewTopic("insurance-subscriptions-topic", 1, 1),
                    )
                admin.createTopics(topics).all().get()
            }
        }
    }

    @Test
    fun `should create order with received value`() {
        val request = Mocks.createOrderRequest()

        val response =
            restTemplate.postForEntity(
                "/orders",
                request,
                CreateOrderResponse::class.java,
            )

        val orderId = response.body!!.id

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.body!!.id.isNotEmpty())

        val savedOrder = mongoDbRepository.findById(orderId).get()

        assertEquals(Status.RECEIVED.value, savedOrder.status)
    }

    @Test
    fun `should return order by id`() {
        val request = Mocks.createOrderRequest()

        val createdOrder = mongoDbRepository.save(request.toDomain().toDocument())

        val orderId = createdOrder.id

        val response =
            restTemplate.getForEntity(
                "/orders/$orderId",
                OrderResponse::class.java,
            )

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(orderId, response.body!!.id)
    }

    @Test
    fun `should cancel order by id`() {
        val request = Mocks.createOrderRequest().copy(customerId = UUID.randomUUID().toString())

        val createdOrder = mongoDbRepository.save(request.toDomain().toDocument())
        val orderId = createdOrder.id!!

        assertEquals(Status.RECEIVED.value, createdOrder.status)

        val response =
            restTemplate.patchForObject(
                "/orders/$orderId",
                null,
                OrderResponse::class.java,
            )

        assertNotNull(response)
        assertEquals(orderId, response.id)
        assertEquals(Status.CANCELLED.value, response.status)

        val updatedOrder = mongoDbRepository.findById(orderId).get()

        assertEquals(Status.CANCELLED.value, updatedOrder.status)
    }

    @Test
    fun `should return all orders by customer id`() {
        val request = Mocks.createOrderRequest().copy(customerId = UUID.randomUUID().toString())
        val customerId = request.customerId

        mongoDbRepository.save(request.toDomain().toDocument())

        val response =
            restTemplate.getForEntity(
                "/orders/customer/$customerId",
                Array<OrderResponse>::class.java,
            )

        assertAll(
            { assertNotNull(response) },
            { assertEquals(HttpStatus.OK, response.statusCode) },
            { assertEquals(1, response.body!!.size) },
            { assertEquals(customerId, response.body!!.first().customerId) },
        )
    }
}
