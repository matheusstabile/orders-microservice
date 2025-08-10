package com.acme.insurance.core.domain.model.riskclassification

import java.time.Instant

data class RiskClassification(
    val orderId: String,
    val customerId: String,
    val analyzedAt: Instant,
    val classification: Classification,
    val occurrences: List<Occurrences>,
)
