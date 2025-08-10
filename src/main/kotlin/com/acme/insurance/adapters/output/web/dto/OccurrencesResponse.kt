package com.acme.insurance.adapters.output.web.dto

import java.time.Instant

data class OccurrencesResponse(
    val id: String,
    val productId: String,
    val type: String,
    val description: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
