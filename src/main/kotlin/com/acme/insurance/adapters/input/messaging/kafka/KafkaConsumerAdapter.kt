package com.acme.insurance.adapters.input.messaging.kafka

import com.acme.insurance.adapters.input.messaging.kafka.event.InsuranceSubscriptionEvent
import com.acme.insurance.adapters.input.messaging.kafka.event.OrderEvent
import com.acme.insurance.adapters.input.messaging.kafka.event.PaymentEvent
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.ports.input.ProcessInsuranceSubscriptionUseCase
import com.acme.insurance.core.ports.input.ProcessOrderWithReceivedStatusUseCase
import com.acme.insurance.core.ports.input.ProcessOrderWithValidatedStatusUseCase
import com.acme.insurance.core.ports.input.ProcessPaymentUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class KafkaConsumerAdapter(
    private val processOrderWithReceivedStatusUseCase: ProcessOrderWithReceivedStatusUseCase,
    private val processOrderWithValidatedStatusUseCase: ProcessOrderWithValidatedStatusUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val processInsuranceSubscriptionUseCase: ProcessInsuranceSubscriptionUseCase,
    private val mapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["orders-topic", "payments-topic", "insurance-subscriptions-topic"])
    fun consumePolicyReceivedStatusEvent(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment,
    ) {
        val topic = record.topic()

        when (topic) {
            "orders-topic" -> handleOrdersTopicEvent(record = record, ack = ack)
            "payments-topic" -> handlePaymentsTopicEvent(record = record, ack = ack)
            "insurance-subscriptions-topic" -> handleInsuranceSubscriptionsTopicEvent(record = record, ack = ack)
        }
    }

    private fun handleOrdersTopicEvent(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment,
    ) {
        val orderStatus =
            record
                .headers()
                .lastHeader("orderStatus")
                .value()
                ?.let { String(it) } ?: "UNKNOWN"

        val payload = record.value()

        try {
            val orderEvent = mapper.readValue(payload, OrderEvent::class.java)

            when (orderStatus) {
                Status.RECEIVED.value -> {
                    processOrderWithReceivedStatusUseCase.execute(orderId = orderEvent.id)?.let {
                        ack.acknowledge()
                    }
                    return
                }

                Status.VALIDATED.value -> {
                    processOrderWithValidatedStatusUseCase.execute(orderId = orderEvent.id)?.let {
                        ack.acknowledge()
                    }
                    return
                }

                else -> {
                    logger
                        .atInfo()
                        .addKeyValue("orderStatus", orderStatus)
                        .addKeyValue("payload", payload)
                        .log("Skipping processing for order event")
                    ack.acknowledge()
                    return
                }
            }
        } catch (e: Exception) {
            logger
                .atError()
                .addKeyValue("payload", payload)
                .setCause(e)
                .log("Error processing order event: ${e.message}")
            return
        }
    }

    private fun handlePaymentsTopicEvent(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment,
    ) {
        val payload = record.value()

        try {
            val paymentEvent = mapper.readValue(payload, PaymentEvent::class.java)
            processPaymentUseCase.execute(orderId = paymentEvent.orderId, paymentId = paymentEvent.id)?.let {
                ack.acknowledge()
            }
            return
        } catch (e: Exception) {
            logger
                .atError()
                .addKeyValue("payload", payload)
                .setCause(e)
                .log("Error processing payment event: ${e.message}")
            return
        }
    }

    private fun handleInsuranceSubscriptionsTopicEvent(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment,
    ) {
        val payload = record.value()

        try {
            val insuranceSubscriptionEvent = mapper.readValue(payload, InsuranceSubscriptionEvent::class.java)
            processInsuranceSubscriptionUseCase
                .execute(
                    orderId = insuranceSubscriptionEvent.orderId,
                    subscriptionId = insuranceSubscriptionEvent.id,
                )?.let {
                    ack.acknowledge()
                }
            return
        } catch (e: Exception) {
            logger
                .atError()
                .addKeyValue("payload", payload)
                .setCause(e)
                .log("Error processing insurance subscription event: ${e.message}")
            return
        }
    }
}
