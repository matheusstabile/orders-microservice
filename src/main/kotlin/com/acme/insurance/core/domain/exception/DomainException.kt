package com.acme.insurance.core.domain.exception

data class DomainException(
    override val message: String,
    override val cause: Throwable?,
) : RuntimeException()
