package com.acme.insurance.adapters.output.messaging.kafka

import com.acme.insurance.adapters.input.messaging.kafka.event.OrderEvent
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class KafkaProducerAdapterTest {
    @MockK
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @InjectMockKs
    private lateinit var kafkaProducerAdapter: KafkaProducerAdapter

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should publish event with correct payload and headers`() {
        val orderId = "orderId"
        val orderStatus = "RECEIVED"
        val slot = slot<Message<OrderEvent>>()
        every { kafkaTemplate.send(capture(slot)) } returns mockk()

        kafkaProducerAdapter.publishEvent(orderId, orderStatus)

        verify(exactly = 1) { kafkaTemplate.send(any<Message<OrderEvent>>()) }
        val message = slot.captured
        assertAll(
            { assertNotNull(message) },
            { assertEquals(OrderEvent(orderId), message.payload) },
            { assertEquals("orders-topic", message.headers[KafkaHeaders.TOPIC]) },
            { assertEquals(orderStatus, message.headers["orderStatus"]) },
        )
    }
}
