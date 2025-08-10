package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status
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
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class ProcessPaymentUseCaseImplTest {
    @MockK
    lateinit var cachePort: CachePort

    @MockK
    lateinit var repositoryPort: RepositoryPort

    @InjectMockKs
    lateinit var processPaymentUseCaseImpl: ProcessPaymentUseCaseImpl

    private var orderMock: Order = Mocks.createOrder()
    private var orderId: String = "orderId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return null when redis register is found within the informed orderId key`() {
        orderMock = orderMock.copy(status = PENDING)

        every { cachePort.get(key = orderId) } returns orderId

        val result = processPaymentUseCaseImpl.execute(orderId = orderId, paymentId = "paymentId")

        assertNull(result)

        verify(exactly = 1) { cachePort.get(key = orderId) }
        verify(exactly = 0) { cachePort.put(key = any(), value = any()) }
        verify(exactly = 0) { repositoryPort.findById(orderId = orderId) }
    }

    @Test
    fun `should process payment when order is in PENDING status`() {
        orderMock = orderMock.copy(status = PENDING)

        every { cachePort.get(key = orderId) } returns null
        every { repositoryPort.findById(orderId = orderId) } returns orderMock
        every { cachePort.put(key = orderId, value = "paymentId") } returns Unit

        val result = processPaymentUseCaseImpl.execute(orderId = orderId, paymentId = "paymentId")

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<Unit>(result) },
        )

        verify(exactly = 1) { cachePort.get(key = orderId) }
        verify(exactly = 1) { cachePort.put(key = orderId, value = "paymentId") }
        verify(exactly = 1) { repositoryPort.findById(orderId = orderId) }
    }

    @Test
    fun `should return Unit when order is not in PENDING status`() {
        orderMock = orderMock.copy(status = Status.VALIDATED)

        every { cachePort.get(key = orderId) } returns null
        every { repositoryPort.findById(orderId = orderId) } returns orderMock

        val result = processPaymentUseCaseImpl.execute(orderId = orderId, paymentId = "paymentId")

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<Unit>(result) },
        )

        verify(exactly = 1) { cachePort.get(key = orderId) }
        verify(exactly = 0) { cachePort.put(key = any(), value = any()) }
        verify(exactly = 1) { repositoryPort.findById(orderId = orderId) }
    }

    @Test
    fun `should handle exception and return null when an error occurs during payment processing`() {
        every { cachePort.get(key = orderId) } throws RuntimeException("Error accessing cache")

        val result = processPaymentUseCaseImpl.execute(orderId = orderId, paymentId = "paymentId")

        assertNull(result)

        verify(exactly = 1) { cachePort.get(key = orderId) }
        verify(exactly = 0) { cachePort.put(key = any(), value = any()) }
        verify(exactly = 0) { repositoryPort.findById(orderId = orderId) }
    }
}
