package com.acme.insurance.adapters.input.messaging.kafka.event

data class InsuranceSubscriptionEvent(
    val id: String,
    val orderId: String,
)
