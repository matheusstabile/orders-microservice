package com.acme.insurance.adapters.output.web

import com.acme.insurance.adapters.output.web.converter.RiskClassificationConverter
import com.acme.insurance.adapters.output.web.dto.RiskClassificationResponse
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.riskclassification.RiskClassification
import com.acme.insurance.core.ports.output.RiskClassificationPort
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class RiskClassificationAdapter(
    private val restTemplate: RestTemplate,
) : RiskClassificationPort {
    override fun classifyRisk(order: Order): RiskClassification {
        val response =
            restTemplate.postForEntity(
                "http://mockserver:1080/risk-classification",
                RiskClassificationConverter.toRequest(order = order),
                RiskClassificationResponse::class.java,
            )

        if (response.statusCode.is2xxSuccessful) {
            response.body?.let { response ->
                return RiskClassificationConverter.toDomain(response)
            }
            throw RuntimeException("Response body is null")
        }
        throw RuntimeException("Failed to retrieve risk classification: ${response.statusCode}")
    }
}
