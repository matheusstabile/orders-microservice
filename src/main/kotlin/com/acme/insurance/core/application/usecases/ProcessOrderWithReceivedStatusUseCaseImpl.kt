package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.converter.toOrderResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.application.validator.RiskClassificationValidator
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.ports.input.ProcessOrderWithReceivedStatusUseCase
import com.acme.insurance.core.ports.output.MessageProducerPort
import com.acme.insurance.core.ports.output.RepositoryPort
import com.acme.insurance.core.ports.output.RiskClassificationPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProcessOrderWithReceivedStatusUseCaseImpl(
    private val repository: RepositoryPort,
    private val riskClassificationPort: RiskClassificationPort,
    private val messageProducerPort: MessageProducerPort,
    private val riskClassificationValidator: RiskClassificationValidator,
) : ProcessOrderWithReceivedStatusUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(orderId: String): OrderResponse? =
        try {
            val order = repository.findById(orderId = orderId)
            when (order.status) {
                Status.RECEIVED -> {
                    val riskClassification = riskClassificationPort.classifyRisk(order = order)
                    val validatedOrder =
                        riskClassificationValidator.validateRiskClassification(
                            order = order,
                            classification = riskClassification.classification,
                        )
                    val updatedOrder = repository.save(order = validatedOrder)

                    messageProducerPort.publishEvent(
                        orderId = updatedOrder.id!!,
                        orderStatus = updatedOrder.status.value,
                    )
                    logger
                        .atInfo()
                        .addKeyValue("orderId", updatedOrder.id)
                        .addKeyValue("riskClassification", riskClassification.classification)
                        .addKeyValue("status", updatedOrder.status)
                        .log("Order's risk classification validated")

                    updatedOrder.toOrderResponse()
                }

                else -> {
                    logger
                        .atInfo()
                        .addKeyValue("orderId", orderId)
                        .addKeyValue("status", order.status)
                        .log("Order is not in RECEIVED status")
                    order.toOrderResponse()
                }
            }
        } catch (e: Exception) {
            logger
                .atError()
                .setCause(e)
                .addKeyValue("orderId", orderId)
                .log("Error processing order with id $orderId: ${e.message}")
            null
        }
}
