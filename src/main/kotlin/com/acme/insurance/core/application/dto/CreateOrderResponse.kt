package com.acme.insurance.core.application.dto

import java.time.Instant

data class CreateOrderResponse(
    val id: String,
    val timestamp: Instant,
)
