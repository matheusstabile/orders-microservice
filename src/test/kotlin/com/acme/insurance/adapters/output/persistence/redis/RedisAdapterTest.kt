package com.acme.insurance.adapters.output.persistence.redis

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class RedisAdapterTest {
    @MockK
    lateinit var redisTemplate: StringRedisTemplate

    @MockK
    lateinit var valueOperations: ValueOperations<String, String>

    @InjectMockKs
    lateinit var redisAdapter: RedisAdapter

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should get value from redis`() {
        val key = "key"
        val value = "value"
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get(key) } returns value

        val result = redisAdapter.get(key)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(value, result) },
        )
        verify(exactly = 1) { redisTemplate.opsForValue() }
        verify(exactly = 1) { valueOperations.get(key) }
    }

    @Test
    fun `should return null when value not found in redis`() {
        val key = "key"
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get(key) } returns null

        val result = redisAdapter.get(key)

        assertAll(
            { assertNull(result) },
        )
        verify(exactly = 1) { redisTemplate.opsForValue() }
        verify(exactly = 1) { valueOperations.get(key) }
    }

    @Test
    fun `should put value in redis`() {
        val key = "key"
        val value = "value"
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.set(key, value) } just Runs

        redisAdapter.put(key, value)

        verify(exactly = 1) { redisTemplate.opsForValue() }
        verify(exactly = 1) { valueOperations.set(key, value) }
    }

    @Test
    fun `should delete value from redis`() {
        val key = "key"
        every { redisTemplate.delete(key) } returns true

        redisAdapter.delete(key)

        verify(exactly = 1) { redisTemplate.delete(key) }
    }
}
