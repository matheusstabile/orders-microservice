package com.acme.insurance.adapters.output.persistence.mongodb

import com.acme.insurance.adapters.output.persistence.mongodb.converter.toDocument
import com.acme.insurance.adapters.output.persistence.mongodb.repository.MongoDbRepository
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.util.Mocks
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class MongoDbAdapterTest {
    @MockK
    lateinit var repository: MongoDbRepository

    @InjectMockKs
    lateinit var mongoDbAdapter: MongoDbAdapter

    private var order: Order = Mocks.createOrder()
    private val orderId: String = "orderId"
    private val customerId: String = "customerId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should save order and return domain object`() {
        val document = order.toDocument().copy(id = orderId)

        every { repository.save(document) } returns document

        val result = mongoDbAdapter.save(order)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<Order>(result) },
            { assertEquals(orderId, result.id) },
        )
        verify(exactly = 1) { repository.save(document) }
    }

    @Test
    fun `should find order by id and return domain object`() {
        val document = order.toDocument().copy(id = orderId)

        every { repository.findById(orderId) } returns Optional.of(document)

        val result = mongoDbAdapter.findById(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<Order>(result) },
            { assertEquals(orderId, result.id) },
        )
        verify(exactly = 1) { repository.findById(orderId) }
    }

    @Test
    fun `should throw when order not found by id`() {
        every { repository.findById(orderId) } returns Optional.empty()

        assertThrows<Exception> {
            mongoDbAdapter.findById(orderId)
        }
        verify(exactly = 1) { repository.findById(orderId) }
    }

    @Test
    fun `should get all orders by customer id and return domain list`() {
        val documents = listOf(order.toDocument().copy(id = orderId))

        every { repository.findAllByCustomerId(customerId = customerId) } returns documents

        val result = mongoDbAdapter.getAllOrdersByCustomerId(customerId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<List<Order>>(result) },
            { assertEquals(1, result.size) },
            { assertEquals(orderId, result.first().id) },
        )
        verify(exactly = 1) { repository.findAllByCustomerId(customerId) }
    }
}
