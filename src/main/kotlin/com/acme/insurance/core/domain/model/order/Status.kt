package com.acme.insurance.core.domain.model.order

enum class Status(
    val value: String,
) {
    RECEIVED("RECEIVED"),
    VALIDATED("VALIDATED"),
    PENDING("PENDING"),
    REJECTED("REJECTED"),
    APPROVED("APPROVED"),
    CANCELLED("CANCELLED"),
}
