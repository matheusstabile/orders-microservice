package com.acme.insurance.core.ports.input

import com.acme.insurance.core.application.dto.CreateOrderRequest
import com.acme.insurance.core.application.dto.CreateOrderResponse

interface CreateOrderUseCase {
    fun execute(request: CreateOrderRequest): CreateOrderResponse
}
