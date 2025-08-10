package com.acme.insurance.adapters.input.messaging.kafka.event

data class PaymentEvent(
    val id: String,
    val orderId: String,
)
