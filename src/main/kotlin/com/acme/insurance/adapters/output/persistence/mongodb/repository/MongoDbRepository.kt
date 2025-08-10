package com.acme.insurance.adapters.output.persistence.mongodb.repository

import com.acme.insurance.adapters.output.persistence.mongodb.document.OrderDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoDbRepository : MongoRepository<OrderDocument, String> {
    fun findAllByCustomerId(customerId: String): List<OrderDocument>
}
