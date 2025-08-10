package com.acme.insurance.adapters.output.persistence.mongodb

import com.acme.insurance.adapters.output.persistence.mongodb.converter.toDocument
import com.acme.insurance.adapters.output.persistence.mongodb.converter.toDomain
import com.acme.insurance.adapters.output.persistence.mongodb.repository.MongoDbRepository
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.ports.output.RepositoryPort
import org.springframework.stereotype.Repository

@Repository
class MongoDbAdapter(
    private val repository: MongoDbRepository,
) : RepositoryPort {
    override fun save(order: Order): Order {
        val document = order.toDocument()

        val savedDocument = repository.save(document)

        return savedDocument.toDomain()
    }

    override fun findById(orderId: String): Order {
        val orderDocument = repository.findById(orderId).orElseThrow()

        return orderDocument.toDomain()
    }

    override fun getAllOrdersByCustomerId(customerId: String): List<Order> {
        val policyDocuments = repository.findAllByCustomerId(customerId)

        return policyDocuments.map { it.toDomain() }
    }
}
