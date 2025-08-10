package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.domain.model.order.Status.PENDING
import com.acme.insurance.core.ports.output.MessageProducerPort
import com.acme.insurance.core.ports.output.RepositoryPort
import com.acme.insurance.util.Mocks
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class ProcessOrderWithValidatedStatusUseCaseImplTest {
    @MockK
    lateinit var repositoryPort: RepositoryPort

    @MockK
    lateinit var messageProducerPort: MessageProducerPort

    @InjectMockKs
    lateinit var processOrderWithValidatedStatusUseCaseImpl: ProcessOrderWithValidatedStatusUseCaseImpl

    private var orderMock: Order = Mocks.createOrder()
    private var orderId: String = "orderId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should process order with validated status successfully`() {
        val order = orderMock.copy(status = Status.VALIDATED)

        every { repositoryPort.findById(orderId = orderId) } returns order
        every { repositoryPort.save(any(Order::class)) } returns
            order.copy(status = PENDING, history = order.history + order.history.last().copy(status = PENDING))
        every { messageProducerPort.publishEvent(orderId = order.id!!, orderStatus = PENDING.value) } returns Unit

        val result = processOrderWithValidatedStatusUseCaseImpl.execute(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<OrderResponse>(result) },
            { assertEquals(PENDING.value, result?.status) },
            { assertEquals(result?.history?.size, 2) },
            { assertEquals(result?.history?.last()?.status, PENDING.value) },
        )

        verifyAll {
            repositoryPort.findById(orderId = orderId)
            repositoryPort.save(order = any(Order::class))
            messageProducerPort.publishEvent(orderId = order.id!!, orderStatus = PENDING.value)
        }
    }

    @Test
    fun `should return OrderResponse when order is not in validated status`() {
        val order = orderMock.copy(status = Status.RECEIVED)

        every { repositoryPort.findById(orderId = orderId) } returns order

        val result = processOrderWithValidatedStatusUseCaseImpl.execute(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<OrderResponse>(result) },
        )

        verify(exactly = 1) { repositoryPort.findById(orderId = orderId) }
        verify(exactly = 0) { repositoryPort.save(any(Order::class)) }
        verify(exactly = 0) { messageProducerPort.publishEvent(orderId = any(), orderStatus = any()) }
    }

    @Test
    fun `should return null when exception is thrown`() {
        every { repositoryPort.findById(orderId = orderId) } throws RuntimeException("Database error")

        val result = processOrderWithValidatedStatusUseCaseImpl.execute(orderId)

        assertNull(result)

        verify(exactly = 1) { repositoryPort.findById(orderId = orderId) }
        verify(exactly = 0) { repositoryPort.save(any(Order::class)) }
        verify(exactly = 0) { messageProducerPort.publishEvent(orderId = any(), orderStatus = any()) }
    }
}
