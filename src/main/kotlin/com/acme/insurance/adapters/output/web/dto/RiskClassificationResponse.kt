package com.acme.insurance.adapters.output.web.dto

import java.time.Instant

data class RiskClassificationResponse(
    val orderId: String,
    val customerId: String,
    val analyzedAt: Instant,
    val classification: String,
    val occurrences: List<OccurrencesResponse>,
)
