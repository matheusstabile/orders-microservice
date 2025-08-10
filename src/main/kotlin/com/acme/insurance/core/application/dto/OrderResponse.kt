package com.acme.insurance.core.application.dto

import java.math.BigDecimal
import java.time.Instant

data class OrderResponse(
    val id: String,
    val customerId: String,
    val productId: String,
    val category: String,
    val salesChannel: String,
    val paymentMethod: String,
    val status: String,
    val createdAt: Instant,
    val finishedAt: Instant?,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>,
    val history: List<OrderHistoryResponse>,
)
