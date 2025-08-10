package com.acme.insurance.core.application.converter

import com.acme.insurance.core.application.dto.CreateOrderRequest
import com.acme.insurance.core.application.dto.CreateOrderResponse
import com.acme.insurance.core.application.dto.OrderHistoryResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.model.order.Category
import com.acme.insurance.core.domain.model.order.History
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status
import java.time.Instant

fun CreateOrderRequest.toDomain() =
    Order(
        customerId = this.customerId,
        productId = this.productId,
        category = Category.valueOf(this.category),
        salesChannel = this.salesChannel,
        paymentMethod = this.paymentMethod,
        status = Status.RECEIVED,
        createdAt = Instant.now(),
        totalMonthlyPremiumAmount = this.totalMonthlyPremiumAmount,
        insuredAmount = this.insuredAmount,
        coverages = this.coverages,
        assistances = this.assistances,
        history = listOf(History(status = Status.RECEIVED)),
    )

fun Order.toCreateOrderResponse() =
    CreateOrderResponse(
        id = this.id.orEmpty(),
        timestamp = this.createdAt,
    )

fun Order.toOrderResponse() =
    OrderResponse(
        id = this.id.orEmpty(),
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
        history = this.history.map { it.toOrderHistoryResponse() },
    )

fun History.toOrderHistoryResponse() =
    OrderHistoryResponse(
        status = this.status.value,
        timestamp = this.timestamp,
    )
