package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status.PENDING
import com.acme.insurance.core.ports.output.CachePort
import com.acme.insurance.core.ports.output.RepositoryPort
import com.acme.insurance.util.Mocks
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProcessInsuranceSubscriptionUseCaseImplTest {
    @MockK
    lateinit var cachePort: CachePort

    @MockK
    lateinit var repositoryPort: RepositoryPort

    @InjectMockKs
    lateinit var processInsuranceSubscriptionUseCaseImpl: ProcessInsuranceSubscriptionUseCaseImpl

    private var orderMock: Order = Mocks.createOrder()
    private var orderId: String = "orderId"
    private var subscriptionId: String = "subscriptionId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should process insurance subscription successfully`() {
        orderMock = orderMock.copy(id = orderId, status = PENDING)

        every { cachePort.get(orderId) } returns String()
        every { repositoryPort.findById(orderId) } returns orderMock
        every { repositoryPort.save(any(Order::class)) } returns orderMock
        every { cachePort.delete(orderId) } returns true

        val result = processInsuranceSubscriptionUseCaseImpl.execute(orderId = orderId, subscriptionId = subscriptionId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<Unit>(result) },
        )

        verify(exactly = 1) { cachePort.get(orderId) }
        verify(exactly = 1) { repositoryPort.findById(orderId) }
        verify(exactly = 1) { repositoryPort.save(any(Order::class)) }
        verify(exactly = 1) { cachePort.delete(orderId) }
    }

    @Test
    fun `should return null when cache register not found`() {
        every { cachePort.get(orderId) } returns null

        val result = processInsuranceSubscriptionUseCaseImpl.execute(orderId = orderId, subscriptionId = subscriptionId)

        assertNull(result)

        verify(exactly = 1) { cachePort.get(orderId) }
        verify(exactly = 0) { repositoryPort.findById(orderId) }
        verify(exactly = 0) { repositoryPort.save(any(Order::class)) }
        verify(exactly = 0) { cachePort.delete(orderId) }
    }

    @Test
    fun `should return null when exception is thrown`() {
        every { cachePort.get(orderId) } throws RuntimeException("Cache error")

        val result = processInsuranceSubscriptionUseCaseImpl.execute(orderId = orderId, subscriptionId = subscriptionId)

        assertNull(result)

        verify(exactly = 1) { cachePort.get(orderId) }
        verify(exactly = 0) { repositoryPort.findById(orderId) }
        verify(exactly = 0) { repositoryPort.save(any(Order::class)) }
        verify(exactly = 0) { cachePort.delete(orderId) }
    }
}
