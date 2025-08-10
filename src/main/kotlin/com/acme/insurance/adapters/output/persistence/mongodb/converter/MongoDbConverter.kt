package com.acme.insurance.adapters.output.persistence.mongodb.converter

import com.acme.insurance.adapters.output.persistence.mongodb.document.OrderDocument
import com.acme.insurance.adapters.output.persistence.mongodb.document.OrderHistoryDocument
import com.acme.insurance.core.domain.model.order.Category
import com.acme.insurance.core.domain.model.order.History
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status

fun Order.toDocument(): OrderDocument =
    OrderDocument(
        id = this.id,
        customerId = this.customerId,
        productId = this.productId,
        category = this.category.value,
        salesChannel = this.salesChannel,
        paymentMethod = this.paymentMethod,
        status = this.status.value,
        createdAt = this.createdAt,
        finishedAt = this.finishedAt,
        totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
        insuredAmount = this.insuredAmount,
        coverages = this.coverages,
        assistances = this.assistances,
        history = this.history.map { it.toDocument() },
    )

fun History.toDocument(): OrderHistoryDocument =
    OrderHistoryDocument(
        status = this.status.value,
        timestamp = this.timestamp,
    )

fun OrderDocument.toDomain(): Order =
    Order(
        id = id,
        customerId = customerId,
        productId = productId,
        category = Category.valueOf(category),
        salesChannel = salesChannel,
        paymentMethod = paymentMethod,
        status = Status.valueOf(status),
        createdAt = createdAt,
        finishedAt = finishedAt,
        totalMonthlyPremiumAmount = totalMonthlyPremiumAmount,
        insuredAmount = insuredAmount,
        coverages = coverages,
        assistances = assistances,
        history = history.map { it.toDomain() },
    )

fun OrderHistoryDocument.toDomain(): History =
    History(
        status = Status.valueOf(status),
        timestamp = timestamp,
    )
