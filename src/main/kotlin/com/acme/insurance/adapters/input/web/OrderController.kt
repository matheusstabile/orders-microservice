package com.acme.insurance.adapters.input.web

import com.acme.insurance.core.application.dto.CreateOrderRequest
import com.acme.insurance.core.application.dto.CreateOrderResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.ports.input.CancelOrderRequestUseCase
import com.acme.insurance.core.ports.input.CreateOrderUseCase
import com.acme.insurance.core.ports.input.FindAllOrdersByCustomerIdUseCase
import com.acme.insurance.core.ports.input.FindOrderByIdUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val createOrderUseCase: CreateOrderUseCase,
    private val findOrderByIdUseCase: FindOrderByIdUseCase,
    private val findAllOrdersByCustomerIdUseCase: FindAllOrdersByCustomerIdUseCase,
    private val cancelOrderRequestUseCase: CancelOrderRequestUseCase,
) {
    @PostMapping
    fun createOrder(
        @RequestBody request: CreateOrderRequest,
    ): ResponseEntity<CreateOrderResponse> = ResponseEntity.status(HttpStatus.CREATED).body(createOrderUseCase.execute(request = request))

    @GetMapping("/{orderId}")
    fun findOrderById(
        @PathVariable orderId: String,
    ): ResponseEntity<OrderResponse> = ResponseEntity.ok(findOrderByIdUseCase.execute(orderId = orderId))

    @PatchMapping("/{orderId}")
    fun cancelOrder(
        @PathVariable orderId: String,
    ): ResponseEntity<OrderResponse> = ResponseEntity.ok(cancelOrderRequestUseCase.execute(orderId = orderId))

    @GetMapping("/customer/{customerId}")
    fun findAllOrdersByCustomerId(
        @PathVariable customerId: String,
    ): ResponseEntity<List<OrderResponse>> =
        findAllOrdersByCustomerIdUseCase
            .execute(customerId = customerId)
            .takeIf { it.isNotEmpty() }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()
}
