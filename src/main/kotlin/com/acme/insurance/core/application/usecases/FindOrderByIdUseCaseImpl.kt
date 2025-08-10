package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.converter.toOrderResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.exception.DomainException
import com.acme.insurance.core.ports.input.FindOrderByIdUseCase
import com.acme.insurance.core.ports.output.RepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FindOrderByIdUseCaseImpl(
    private val repository: RepositoryPort,
) : FindOrderByIdUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(orderId: String): OrderResponse =
        try {
            val order = repository.findById(orderId = orderId)
            order.toOrderResponse()
        } catch (e: Exception) {
            logger
                .atError()
                .setCause(e)
                .addKeyValue("orderId", orderId)
                .log("Error retrieving order: ${e.message}")
            throw DomainException(
                message = "Error retrieving order with id $orderId",
                cause = e,
            )
        }
}
