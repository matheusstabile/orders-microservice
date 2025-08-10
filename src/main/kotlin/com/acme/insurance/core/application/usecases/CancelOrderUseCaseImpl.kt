package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.converter.toOrderResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.exception.DomainException
import com.acme.insurance.core.domain.model.order.History
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.domain.model.order.Status.APPROVED
import com.acme.insurance.core.domain.model.order.Status.REJECTED
import com.acme.insurance.core.ports.input.CancelOrderRequestUseCase
import com.acme.insurance.core.ports.output.RepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CancelOrderUseCaseImpl(
    private val repositoryPort: RepositoryPort,
) : CancelOrderRequestUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(orderId: String): OrderResponse =
        try {
            val order = repositoryPort.findById(orderId = orderId)
            when (order.status) {
                APPROVED, REJECTED -> {
                    logger
                        .atWarn()
                        .addKeyValue("orderId", orderId)
                        .addKeyValue("status", order.status)
                        .log("Order is in invalid state for cancellation")

                    throw DomainException(
                        message = "Order with id $orderId cannot be cancelled because it is in ${order.status} state",
                        cause = null,
                    )
                }

                else -> {
                    val updatedOrder =
                        order.copy(
                            status = Status.CANCELLED,
                            history = order.history + History(status = Status.CANCELLED),
                        )

                    val savedOrder = repositoryPort.save(order = updatedOrder)
                    logger
                        .atInfo()
                        .addKeyValue("orderId", updatedOrder.id)
                        .log("Order has been cancelled successfully")

                    savedOrder.toOrderResponse()
                }
            }
        } catch (e: Exception) {
            logger
                .atError()
                .addKeyValue("orderId", orderId)
                .setCause(e)
                .log("Error cancelling order: ${e.message}")
            throw DomainException(
                message = "Error cancelling order with id $orderId",
                cause = e,
            )
        }
}
