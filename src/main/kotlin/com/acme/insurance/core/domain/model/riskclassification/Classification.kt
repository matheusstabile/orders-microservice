package com.acme.insurance.core.domain.model.riskclassification

enum class Classification(
    val value: String,
) {
    REGULAR("REGULAR"),
    HIGH_RISK("HIGH_RISK"),
    PREFERENTIAL("PREFERENTIAL"),
    NO_INFORMATION("NO_INFORMATION"),
}
