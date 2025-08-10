package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.converter.toOrderResponse
import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.exception.DomainException
import com.acme.insurance.core.ports.input.FindAllOrdersByCustomerIdUseCase
import com.acme.insurance.core.ports.output.RepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FindAllOrdersByCustomerIdUseCaseImpl(
    private val repository: RepositoryPort,
) : FindAllOrdersByCustomerIdUseCase {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(customerId: String): List<OrderResponse> =
        try {
            val orders = repository.getAllOrdersByCustomerId(customerId = customerId)
            orders.map { it.toOrderResponse() }
        } catch (e: Exception) {
            logger
                .atError()
                .addKeyValue("customerId", customerId)
                .setCause(e)
                .log("Error retrieving orders from customer: ${e.message}")
            throw DomainException(
                message = "Error retrieving orders from customer with id $customerId",
                cause = e,
            )
        }
}
