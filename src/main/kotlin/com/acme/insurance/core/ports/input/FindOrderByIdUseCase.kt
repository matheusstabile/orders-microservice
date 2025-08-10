package com.acme.insurance.core.ports.input

import com.acme.insurance.core.application.dto.OrderResponse

interface FindOrderByIdUseCase {
    fun execute(orderId: String): OrderResponse
}
