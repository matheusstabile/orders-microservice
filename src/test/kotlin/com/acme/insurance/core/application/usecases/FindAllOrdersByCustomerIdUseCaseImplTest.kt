package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.domain.exception.DomainException
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.ports.output.RepositoryPort
import com.acme.insurance.util.Mocks
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class FindAllOrdersByCustomerIdUseCaseImplTest {
    @MockK
    lateinit var repositoryPort: RepositoryPort

    @InjectMockKs
    lateinit var findAllOrdersByCustomerIdUseCaseImpl: FindAllOrdersByCustomerIdUseCaseImpl

    private var orderMock: Order = Mocks.createOrder()
    private var orderId: String = "orderId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should find all orders by customer id successfully`() {
        val orders = listOf(orderMock.copy(id = orderId))

        every { repositoryPort.getAllOrdersByCustomerId(orderId) } returns orders

        val result = findAllOrdersByCustomerIdUseCaseImpl.execute(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<List<OrderResponse>>(result) },
            { assertTrue(result.isNotEmpty()) },
            { assertEquals(orders.size, result.size) },
            { assertEquals(orderId, result.first().id) },
        )
    }

    @Test
    fun `should throw DomainException when repository throws an exception`() {
        every { repositoryPort.getAllOrdersByCustomerId(orderId) }
            .throws(RuntimeException("Database error"))

        val exception =
            assertThrows<DomainException> {
                findAllOrdersByCustomerIdUseCaseImpl.execute(orderId)
            }

        assertAll(
            { assertEquals("Error retrieving orders from customer with id $orderId", exception.message) },
            { assertEquals("Database error", exception.cause?.message) },
        )
    }
}
