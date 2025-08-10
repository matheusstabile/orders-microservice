package com.acme.insurance.core.ports.output

import com.acme.insurance.core.domain.model.order.Order

interface RepositoryPort {
    fun save(order: Order): Order

    fun findById(orderId: String): Order

    fun getAllOrdersByCustomerId(customerId: String): List<Order>
}
