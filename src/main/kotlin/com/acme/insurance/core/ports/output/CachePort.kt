package com.acme.insurance.core.ports.output

interface CachePort {
    fun get(key: String): String?

    fun put(
        key: String,
        value: String,
    )

    fun delete(key: String): Boolean
}
