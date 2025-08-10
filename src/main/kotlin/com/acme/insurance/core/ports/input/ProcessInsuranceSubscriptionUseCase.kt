package com.acme.insurance.core.ports.input

interface ProcessInsuranceSubscriptionUseCase {
    fun execute(
        orderId: String,
        subscriptionId: String,
    ): Unit?
}
