package com.acme.insurance.core.ports.input

import com.acme.insurance.core.application.dto.OrderResponse

interface FindAllOrdersByCustomerIdUseCase {
    fun execute(customerId: String): List<OrderResponse>
}
