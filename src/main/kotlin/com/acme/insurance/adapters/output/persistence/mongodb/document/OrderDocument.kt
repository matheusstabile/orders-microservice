package com.acme.insurance.adapters.output.persistence.mongodb.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "orders")
data class OrderDocument(
    @field:Id val id: String? = null,
    @field:Indexed val customerId: String,
    val productId: String,
    val category: String,
    val salesChannel: String,
    val paymentMethod: String,
    val status: String,
    val createdAt: Instant,
    val finishedAt: Instant? = null,
    val totalMonthlyPremiumAmount: BigDecimal,
    val insuredAmount: BigDecimal,
    val coverages: Map<String, BigDecimal>,
    val assistances: List<String>,
    val history: List<OrderHistoryDocument>,
)

data class OrderHistoryDocument(
    val status: String,
    val timestamp: Instant,
)
