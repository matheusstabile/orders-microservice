package com.acme.insurance.core.application.dto

import java.math.BigDecimal

data class CreateOrderRequest(
    val customerId: String,
    val productId: String,
    val category: String,
    val salesChannel: String,
    val paymentMethod: String,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>,
)
