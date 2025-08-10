package com.acme.insurance.adapters.output.messaging.kafka

import com.acme.insurance.adapters.input.messaging.kafka.event.OrderEvent
import com.acme.insurance.core.ports.output.MessageProducerPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class KafkaProducerAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : MessageProducerPort {
    override fun publishEvent(
        orderId: String,
        orderStatus: String,
    ) {
        val orderEvent = OrderEvent(id = orderId)

        val message =
            MessageBuilder
                .withPayload(orderEvent)
                .setHeader(KafkaHeaders.TOPIC, "orders-topic")
                .setHeader("orderStatus", orderStatus)
                .build()

        kafkaTemplate.send(message)
    }
}
