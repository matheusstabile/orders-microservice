package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.converter.toCreateOrderResponse
import com.acme.insurance.core.application.converter.toDomain
import com.acme.insurance.core.application.dto.CreateOrderRequest
import com.acme.insurance.core.application.dto.CreateOrderResponse
import com.acme.insurance.core.domain.exception.DomainException
import com.acme.insurance.core.ports.input.CreateOrderUseCase
import com.acme.insurance.core.ports.output.MessageProducerPort
import com.acme.insurance.core.ports.output.RepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CreateOrderUseCaseImpl(
    private val repositoryPort: RepositoryPort,
    private val messageProducerPort: MessageProducerPort,
) : CreateOrderUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(request: CreateOrderRequest): CreateOrderResponse =
        try {
            val order = request.toDomain()

            val savedOrder = repositoryPort.save(order = order)

            messageProducerPort.publishEvent(orderId = savedOrder.id!!, orderStatus = savedOrder.status.value)
            logger
                .atInfo()
                .addKeyValue("orderId", savedOrder.id)
                .addKeyValue("customerId", request.customerId)
                .log("Order created successfully")

            savedOrder.toCreateOrderResponse()
        } catch (e: Exception) {
            logger
                .atError()
                .setCause(e)
                .addKeyValue("customerId", request.customerId)
                .log("Failed to create order: ${e.message}")
            throw DomainException(
                message = "Failed to create order for customer ${request.customerId}",
                cause = e,
            )
        }
}
