package com.acme.insurance.adapters.output.web.converter

import com.acme.insurance.adapters.output.web.dto.OccurrencesResponse
import com.acme.insurance.adapters.output.web.dto.RiskClassificationRequest
import com.acme.insurance.adapters.output.web.dto.RiskClassificationResponse
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.riskclassification.Classification
import com.acme.insurance.core.domain.model.riskclassification.Occurrences
import com.acme.insurance.core.domain.model.riskclassification.RiskClassification

object RiskClassificationConverter {
    fun toRequest(order: Order) =
        RiskClassificationRequest(
            customerId = order.customerId,
            orderId = order.id!!,
        )

    fun toDomain(response: RiskClassificationResponse) =
        RiskClassification(
            orderId = response.orderId,
            customerId = response.customerId,
            analyzedAt = response.analyzedAt,
            classification = Classification.valueOf(response.classification),
            occurrences = response.occurrences.map { toDomain(ocurrencesResponse = it) },
        )

    fun toDomain(ocurrencesResponse: OccurrencesResponse) =
        Occurrences(
            id = ocurrencesResponse.id,
            productId = ocurrencesResponse.productId,
            type = ocurrencesResponse.type,
            description = ocurrencesResponse.description,
            createdAt = ocurrencesResponse.createdAt,
            updatedAt = ocurrencesResponse.updatedAt,
        )
}
