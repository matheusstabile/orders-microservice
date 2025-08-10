package com.acme.insurance.adapters.input.messaging.kafka

import com.acme.insurance.adapters.input.messaging.kafka.event.InsuranceSubscriptionEvent
import com.acme.insurance.adapters.input.messaging.kafka.event.OrderEvent
import com.acme.insurance.adapters.input.messaging.kafka.event.PaymentEvent
import com.acme.insurance.core.application.converter.toOrderResponse
import com.acme.insurance.core.domain.model.order.Status.PENDING
import com.acme.insurance.core.domain.model.order.Status.RECEIVED
import com.acme.insurance.core.domain.model.order.Status.VALIDATED
import com.acme.insurance.core.ports.input.ProcessInsuranceSubscriptionUseCase
import com.acme.insurance.core.ports.input.ProcessOrderWithReceivedStatusUseCase
import com.acme.insurance.core.ports.input.ProcessOrderWithValidatedStatusUseCase
import com.acme.insurance.core.ports.input.ProcessPaymentUseCase
import com.acme.insurance.util.Mocks
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.support.Acknowledgment

@ExtendWith(MockKExtension::class)
class KafkaConsumerAdapterTest {
    @MockK
    lateinit var processOrderWithReceivedStatusUseCase: ProcessOrderWithReceivedStatusUseCase

    @MockK
    lateinit var processOrderWithValidatedStatusUseCase: ProcessOrderWithValidatedStatusUseCase

    @MockK
    lateinit var processPaymentUseCase: ProcessPaymentUseCase

    @MockK
    lateinit var processInsuranceSubscriptionUseCase: ProcessInsuranceSubscriptionUseCase

    @MockK
    lateinit var mapper: ObjectMapper

    @MockK
    lateinit var ack: Acknowledgment

    @InjectMockKs
    private lateinit var kafkaConsumerAdapter: KafkaConsumerAdapter

    private var objectMapper: ObjectMapper = ObjectMapper()
    private var orderId: String = "orderId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should process order event with RECEIVED status and acknowledge`() {
        val orderEvent = Mocks.createOrderEvent()
        val orderResponse = Mocks.createOrder().copy(id = orderId).toOrderResponse()

        val payload = objectMapper.writeValueAsString(OrderEvent(id = "orderId"))
        val record = Mocks.createConsumerRecord(topic = "orders-topic", payload = payload, status = RECEIVED)

        every { mapper.readValue(payload, OrderEvent::class.java) } returns orderEvent
        every { processOrderWithReceivedStatusUseCase.execute(orderEvent.id) } returns orderResponse
        every { ack.acknowledge() } just Runs

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 1) { mapper.readValue(payload, OrderEvent::class.java) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 1) { processOrderWithReceivedStatusUseCase.execute(orderEvent.id) }
        verify(exactly = 0) { processOrderWithValidatedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processPaymentUseCase.execute(any(), any()) }
        verify(exactly = 0) { processInsuranceSubscriptionUseCase.execute(any(), any()) }
    }

    @Test
    fun `should not acknowledge message when event is RECEIVED status but did not process`() {
        val orderEvent = Mocks.createOrderEvent()

        val payload = objectMapper.writeValueAsString(OrderEvent(id = "orderId"))
        val record = Mocks.createConsumerRecord(topic = "orders-topic", payload = payload, status = RECEIVED)

        every { mapper.readValue(payload, OrderEvent::class.java) } returns orderEvent
        every { processPaymentUseCase.execute(orderId = orderId, paymentId = "paymentId") } returns null

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 0) { ack.acknowledge() }
    }

    @Test
    fun `should process order event with VALIDATED status and acknowledge`() {
        val orderEvent = Mocks.createOrderEvent()
        val orderResponse = Mocks.createOrder().copy(id = orderId).toOrderResponse()
        val payload = objectMapper.writeValueAsString(OrderEvent(id = "orderId"))
        val record = Mocks.createConsumerRecord(topic = "orders-topic", payload = payload, status = VALIDATED)

        every { mapper.readValue(payload, OrderEvent::class.java) } returns orderEvent
        every { processOrderWithValidatedStatusUseCase.execute(orderId = "orderId") } returns orderResponse
        every { ack.acknowledge() } just Runs

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 1) { mapper.readValue(payload, OrderEvent::class.java) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 1) { processOrderWithValidatedStatusUseCase.execute(orderEvent.id) }
        verify(exactly = 0) { processOrderWithReceivedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processPaymentUseCase.execute(any(), any()) }
        verify(exactly = 0) { processInsuranceSubscriptionUseCase.execute(any(), any()) }
    }

    @Test
    fun `should skip processing and acknowledge for invalid order status`() {
        val orderEvent = Mocks.createOrderEvent()
        val payload = objectMapper.writeValueAsString(OrderEvent(id = "orderId"))
        val record = Mocks.createConsumerRecord(topic = "orders-topic", payload = payload, status = PENDING)

        every { mapper.readValue(payload, OrderEvent::class.java) } returns orderEvent
        every { ack.acknowledge() } just Runs

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 0) { processOrderWithReceivedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processOrderWithValidatedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processPaymentUseCase.execute(any(), any()) }
        verify(exactly = 0) { processInsuranceSubscriptionUseCase.execute(any(), any()) }
        verify { mapper.readValue(payload, OrderEvent::class.java) }
    }

    @Test
    fun `should log error and not acknowledge when exception occurs in order event`() {
        val payload = objectMapper.writeValueAsString(OrderEvent(id = "orderId"))
        val record = Mocks.createConsumerRecord(topic = "orders-topic", payload = payload, status = PENDING)

        every { mapper.readValue(payload, OrderEvent::class.java) } throws RuntimeException("error")

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 0) { ack.acknowledge() }
        verify(exactly = 0) { processOrderWithReceivedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processOrderWithValidatedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processPaymentUseCase.execute(any(), any()) }
        verify(exactly = 0) { processInsuranceSubscriptionUseCase.execute(any(), any()) }
        verify(exactly = 1) { mapper.readValue(payload, OrderEvent::class.java) }
    }

    @Test
    fun `should process payment event and acknowledge`() {
        val paymentEvent = Mocks.createPaymentEvent()
        val payload = objectMapper.writeValueAsString(PaymentEvent(id = "paymentId", orderId = orderId))
        val record = Mocks.createConsumerRecord(topic = "payments-topic", payload = payload, status = PENDING)

        every { mapper.readValue(payload, PaymentEvent::class.java) } returns paymentEvent
        every {
            processPaymentUseCase.execute(
                orderId = paymentEvent.orderId,
                paymentId = paymentEvent.id,
            )
        } returns Unit
        every { ack.acknowledge() } just Runs

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 1) { mapper.readValue(payload, PaymentEvent::class.java) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 1) {
            processPaymentUseCase.execute(
                orderId = paymentEvent.orderId,
                paymentId = paymentEvent.id,
            )
        }
        verify(exactly = 0) { processOrderWithReceivedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processOrderWithValidatedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processInsuranceSubscriptionUseCase.execute(any(), any()) }
    }

    @Test
    fun `should log error and not acknowledge when exception occurs in payment event`() {
        val paymentEvent = Mocks.createPaymentEvent()
        val payload = objectMapper.writeValueAsString(paymentEvent)
        val record = Mocks.createConsumerRecord(topic = "payments-topic", payload = payload, status = PENDING)

        every { mapper.readValue(payload, PaymentEvent::class.java) } throws RuntimeException("error")

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 1) { mapper.readValue(payload, PaymentEvent::class.java) }
        verify(exactly = 0) { ack.acknowledge() }
        verify(exactly = 0) {
            processPaymentUseCase.execute(
                orderId = paymentEvent.orderId,
                paymentId = paymentEvent.id,
            )
        }
        verify(exactly = 0) { processOrderWithReceivedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processOrderWithValidatedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processInsuranceSubscriptionUseCase.execute(any(), any()) }
    }

    @Test
    fun `should process insurance subscription event and acknowledge`() {
        val insuranceSubscriptionEvent = Mocks.createInsuranceSubscriptionEvent()
        val payload = objectMapper.writeValueAsString(insuranceSubscriptionEvent)
        val record =
            Mocks.createConsumerRecord(topic = "insurance-subscriptions-topic", payload = payload, status = PENDING)

        every { mapper.readValue(payload, InsuranceSubscriptionEvent::class.java) } returns insuranceSubscriptionEvent
        every {
            processInsuranceSubscriptionUseCase.execute(
                orderId = insuranceSubscriptionEvent.orderId,
                subscriptionId = insuranceSubscriptionEvent.id,
            )
        } returns Unit
        every { ack.acknowledge() } just Runs

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 1) { mapper.readValue(payload, InsuranceSubscriptionEvent::class.java) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 1) {
            processInsuranceSubscriptionUseCase.execute(
                orderId = insuranceSubscriptionEvent.orderId,
                subscriptionId = insuranceSubscriptionEvent.id,
            )
        }
        verify(exactly = 0) { processOrderWithReceivedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processPaymentUseCase.execute(any(), any()) }
        verify(exactly = 0) { processOrderWithValidatedStatusUseCase.execute(any()) }
    }

    @Test
    fun `should log error and not acknowledge when exception occurs in insurance subscription event`() {
        val insuranceSubuscriptionEvent = Mocks.createInsuranceSubscriptionEvent()
        val payload = objectMapper.writeValueAsString(insuranceSubuscriptionEvent)
        val record =
            Mocks.createConsumerRecord(topic = "insurance-subscriptions-topic", payload = payload, status = PENDING)

        every { mapper.readValue(payload, InsuranceSubscriptionEvent::class.java) } throws RuntimeException("error")

        kafkaConsumerAdapter.consumePolicyReceivedStatusEvent(record, ack)

        verify(exactly = 1) { mapper.readValue(payload, InsuranceSubscriptionEvent::class.java) }
        verify(exactly = 0) { ack.acknowledge() }
        verify(exactly = 0) {
            processPaymentUseCase.execute(
                orderId = insuranceSubuscriptionEvent.orderId,
                paymentId = insuranceSubuscriptionEvent.id,
            )
        }
        verify(exactly = 0) { processOrderWithReceivedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processOrderWithValidatedStatusUseCase.execute(any()) }
        verify(exactly = 0) { processInsuranceSubscriptionUseCase.execute(any(), any()) }
    }
}
