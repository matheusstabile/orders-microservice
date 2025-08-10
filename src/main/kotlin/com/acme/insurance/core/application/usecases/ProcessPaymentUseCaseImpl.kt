package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.domain.model.order.Status.PENDING
import com.acme.insurance.core.ports.input.ProcessPaymentUseCase
import com.acme.insurance.core.ports.output.CachePort
import com.acme.insurance.core.ports.output.RepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProcessPaymentUseCaseImpl(
    private val cachePort: CachePort,
    private val repositoryPort: RepositoryPort,
) : ProcessPaymentUseCase {
    private val logger = LoggerFactory.getLogger(ProcessPaymentUseCaseImpl::class.java)

    override fun execute(
        orderId: String,
        paymentId: String,
    ): Unit? =
        try {
            cachePort.get(key = orderId)?.let {
                logger
                    .atWarn()
                    .addKeyValue("orderId", orderId)
                    .log("Payment already processed for order")

                return null
            }

            val order = repositoryPort.findById(orderId = orderId)
            when (order.status) {
                PENDING -> {
                    cachePort.put(key = order.id!!, value = paymentId)
                    logger
                        .atInfo()
                        .addKeyValue("orderId", order.id)
                        .addKeyValue("paymentId", paymentId)
                        .log("Payment processed for order")

                    Unit
                }

                else -> {
                    logger
                        .atInfo()
                        .addKeyValue("orderId", orderId)
                        .addKeyValue("status", order.status)
                        .log("Order is not in PENDING status")

                    Unit
                }
            }
        } catch (e: Exception) {
            logger
                .atError()
                .setCause(e)
                .addKeyValue("orderId", orderId)
                .log("Error processing payment for order")

            null
        }
}
