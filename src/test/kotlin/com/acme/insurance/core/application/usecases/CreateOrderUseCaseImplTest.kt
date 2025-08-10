package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.converter.toDomain
import com.acme.insurance.core.application.dto.CreateOrderRequest
import com.acme.insurance.core.application.dto.CreateOrderResponse
import com.acme.insurance.core.domain.exception.DomainException
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.ports.output.MessageProducerPort
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class CreateOrderUseCaseImplTest {
    @MockK
    lateinit var repositoryPort: RepositoryPort

    @MockK
    lateinit var messageProducerPort: MessageProducerPort

    @InjectMockKs
    lateinit var createOrderUseCaseImpl: CreateOrderUseCaseImpl

    private var requestMock: CreateOrderRequest = Mocks.createOrderRequest()
    private var orderId: String = "orderId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should create order successfully`() {
        val order = requestMock.toDomain().copy(id = orderId)

        every { repositoryPort.save(any(Order::class)) } returns order
        every { messageProducerPort.publishEvent(orderId = order.id!!, orderStatus = order.status.value) } returns Unit

        val result = createOrderUseCaseImpl.execute(requestMock)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<CreateOrderResponse>(result) },
            { assertEquals(order.id, result.id) },
            { assertEquals(order.createdAt, result.timestamp) },
        )

        verify(exactly = 1) { repositoryPort.save(any(Order::class)) }
        verify(exactly = 1) { messageProducerPort.publishEvent(orderId = order.id!!, orderStatus = order.status.value) }
    }

    @Test
    fun `should throw DomainException when order creation fails`() {
        every { repositoryPort.save(any(Order::class)) } throws Exception("Database error")

        val exception = assertThrows<DomainException> { createOrderUseCaseImpl.execute(requestMock) }

        assertEquals("Failed to create order for customer ${requestMock.customerId}", exception.message)
        assertEquals("Database error", exception.cause?.message)
    }
}
