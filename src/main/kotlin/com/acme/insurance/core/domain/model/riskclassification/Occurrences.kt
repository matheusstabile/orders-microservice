package com.acme.insurance.core.domain.model.riskclassification

import java.time.Instant

data class Occurrences(
    val id: String,
    val productId: String,
    val type: String,
    val description: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
