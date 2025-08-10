package com.acme.insurance.core.ports.input

interface ProcessPaymentUseCase {
    fun execute(
        orderId: String,
        paymentId: String,
    ): Unit?
}
