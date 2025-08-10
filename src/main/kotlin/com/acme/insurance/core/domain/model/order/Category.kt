package com.acme.insurance.core.domain.model.order

enum class Category(
    val value: String,
) {
    LIFE("LIFE"),
    AUTO("AUTO"),
    RESIDENTIAL("RESIDENTIAL"),
    OTHER("OTHER"),
}
