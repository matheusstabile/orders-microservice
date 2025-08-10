package com.acme.insurance.adapters.output.persistence.redis

import com.acme.insurance.core.ports.output.CachePort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisAdapter(
    private val redisTemplate: StringRedisTemplate,
) : CachePort {
    override fun get(key: String): String? = redisTemplate.opsForValue().get(key)

    override fun put(
        key: String,
        value: String,
    ) = redisTemplate.opsForValue().set(key, value)

    override fun delete(key: String) = redisTemplate.delete(key)
}
