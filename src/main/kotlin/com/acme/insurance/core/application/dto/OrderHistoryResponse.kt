package com.acme.insurance.core.application.dto

import java.time.Instant

data class OrderHistoryResponse(
    val status: String,
    val timestamp: Instant,
)
