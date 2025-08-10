package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.domain.model.order.History
import com.acme.insurance.core.domain.model.order.Status.APPROVED
import com.acme.insurance.core.ports.input.ProcessInsuranceSubscriptionUseCase
import com.acme.insurance.core.ports.output.CachePort
import com.acme.insurance.core.ports.output.RepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

@Service
class ProcessInsuranceSubscriptionUseCaseImpl(
    private val cachePort: CachePort,
    private val repositoryPort: RepositoryPort,
) : ProcessInsuranceSubscriptionUseCase {
    private val logger = LoggerFactory.getLogger(ProcessInsuranceSubscriptionUseCaseImpl::class.java)

    override fun execute(
        orderId: String,
        subscriptionId: String,
    ): Unit? =
        try {
            cachePort.get(key = orderId)?.let {
                val order = repositoryPort.findById(orderId)
                val updatedOrder =
                    order.copy(
                        status = APPROVED,
                        finishedAt = Instant.now().plus(365, DAYS),
                        history =
                            order.history +
                                History(
                                    status = APPROVED,
                                    timestamp = Instant.now(),
                                ),
                    )
                val savedOrder = repositoryPort.save(order = updatedOrder)
                logger
                    .atInfo()
                    .addKeyValue("orderId", savedOrder.id)
                    .addKeyValue("status", savedOrder.status)
                    .log("Order has been approved and saved")

                cachePort.delete(key = savedOrder.id!!)
                logger
                    .atInfo()
                    .addKeyValue("orderId", savedOrder.id)
                    .log("Cache entry for order deleted")

                return Unit
            }
            logger
                .atInfo()
                .addKeyValue("orderId", orderId)
                .log("No cache entry found for order, skipping processing")
            null
        } catch (e: Exception) {
            logger
                .atError()
                .setCause(e)
                .addKeyValue("orderId", orderId)
                .log("Error processing insurance subscription for order: ${e.message}")

            null
        }
}
