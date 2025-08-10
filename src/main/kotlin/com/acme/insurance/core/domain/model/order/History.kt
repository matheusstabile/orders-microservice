package com.acme.insurance.core.domain.model.order

import java.time.Instant

data class History(
    val status: Status,
    val timestamp: Instant = Instant.now(),
)
