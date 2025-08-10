package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.converter.toOrderResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.model.order.History
import com.acme.insurance.core.domain.model.order.Status.PENDING
import com.acme.insurance.core.domain.model.order.Status.VALIDATED
import com.acme.insurance.core.ports.input.ProcessOrderWithValidatedStatusUseCase
import com.acme.insurance.core.ports.output.MessageProducerPort
import com.acme.insurance.core.ports.output.RepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProcessOrderWithValidatedStatusUseCaseImpl(
    private val repository: RepositoryPort,
    private val messageProducerPort: MessageProducerPort,
) : ProcessOrderWithValidatedStatusUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(orderId: String): OrderResponse? =
        try {
            val order = repository.findById(orderId = orderId)

            when (order.status) {
                VALIDATED -> {
                    val updatedOrder = order.copy(status = PENDING, history = order.history + History(status = PENDING))

                    val savedOrder = repository.save(order = updatedOrder)

                    messageProducerPort.publishEvent(orderId = savedOrder.id!!, orderStatus = savedOrder.status.value)
                    logger
                        .atInfo()
                        .addKeyValue("orderId", savedOrder.id)
                        .addKeyValue("status", savedOrder.status)
                        .log("Order has been updated to PENDING status")

                    savedOrder.toOrderResponse()
                }

                else -> {
                    logger
                        .atInfo()
                        .addKeyValue("orderId", orderId)
                        .addKeyValue("status", order.status)
                        .log("Order is not in VALIDATED status")
                    order.toOrderResponse()
                }
            }
        } catch (e: Exception) {
            logger
                .atError()
                .setCause(e)
                .addKeyValue("orderId", orderId)
                .log("Error processing order: ${e.message}")
            null
        }
}
