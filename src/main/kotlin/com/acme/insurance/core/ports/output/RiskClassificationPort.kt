package com.acme.insurance.core.ports.output

import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.riskclassification.RiskClassification

interface RiskClassificationPort {
    fun classifyRisk(order: Order): RiskClassification
}
