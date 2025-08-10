package com.acme.insurance.core.domain.model.order

import java.math.BigDecimal
import java.time.Instant

data class Order(
    val id: String? = null,
    val customerId: String,
    val productId: String,
    val category: Category,
    val salesChannel: String,
    val paymentMethod: String,
    val status: Status,
    val createdAt: Instant,
    val finishedAt: Instant? = null,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>,
    val history: List<History>,
)
