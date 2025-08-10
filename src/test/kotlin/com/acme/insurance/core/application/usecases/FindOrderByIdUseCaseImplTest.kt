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

@ExtendWith(MockKExtension::class)
class FindOrderByIdUseCaseImplTest {
    @MockK
    lateinit var repository: RepositoryPort

    @InjectMockKs
    lateinit var findOrderByIdUseCaseImpl: FindOrderByIdUseCaseImpl

    private var orderMock: Order = Mocks.createOrder()
    private var orderId: String = "orderId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should find order by id successfully`() {
        every { repository.findById(orderId) } returns (orderMock)

        val result = findOrderByIdUseCaseImpl.execute(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<OrderResponse>(result) },
            { assertEquals(orderId, result.id) },
        )
    }

    @Test
    fun `should throw DomainException when order not found`() {
        every { repository.findById(orderId) } throws (NoSuchElementException("Order not found"))

        val exception =
            assertThrows<DomainException> {
                findOrderByIdUseCaseImpl.execute(orderId)
            }

        assertAll(
            { assertNotNull(exception) },
            { assertEquals("Error retrieving order with id $orderId", exception.message) },
            { assertEquals("Order not found", exception.cause?.message) },
        )
    }
}
