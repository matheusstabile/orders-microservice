package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.exception.DomainException
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.domain.model.order.Status.CANCELLED
import com.acme.insurance.core.ports.output.RepositoryPort
import com.acme.insurance.util.Mocks
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class CancelOrderUseCaseImplTest {
    @MockK
    lateinit var repositoryPort: RepositoryPort

    @InjectMockKs
    lateinit var cancelOrderUseCaseImpl: CancelOrderUseCaseImpl

    lateinit var orderMock: Order
    lateinit var orderId: String

    @BeforeEach
    fun setUp() {
        orderMock = Mocks.createOrder()
        orderId = "orderId"
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should cancel order successfully`() {
        every { repositoryPort.findById(orderId) } returns orderMock
        every { repositoryPort.save(any(Order::class)) } returns orderMock.copy(status = CANCELLED)

        val result = cancelOrderUseCaseImpl.execute(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<OrderResponse>(result) },
            { assertEquals(orderId, result.id) },
            { assertEquals(CANCELLED.value, result.status) },
        )

        verify(exactly = 1) { repositoryPort.findById(orderId) }
        verify(exactly = 1) { repositoryPort.save(any(Order::class)) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["APPROVED", "REJECTED"])
    fun `should throw DomainException when order status is APPROVED or REJECTED`(invalidStatus: Status) {
        orderMock = orderMock.copy(status = invalidStatus)
        every { repositoryPort.findById(orderId) } returns orderMock

        val exception = assertThrows<DomainException> { cancelOrderUseCaseImpl.execute(orderId) }

        assertEquals(
            "Error cancelling order with id orderId",
            exception.message,
        )
        assertEquals(
            "Order with id orderId cannot be cancelled because it is in $invalidStatus state",
            exception.cause?.message,
        )

        verify(exactly = 1) { repositoryPort.findById(orderId) }
        verify(exactly = 0) { repositoryPort.save(any(Order::class)) }
    }
}
