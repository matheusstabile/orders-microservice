package com.acme.insurance.adapters.output.web.dto

data class RiskClassificationRequest(
    val customerId: String,
    val orderId: String,
)
