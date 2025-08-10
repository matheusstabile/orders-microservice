package com.acme.insurance.adapters.input.web

import com.acme.insurance.core.application.converter.toCreateOrderResponse
import com.acme.insurance.core.application.converter.toDomain
import com.acme.insurance.core.application.converter.toOrderResponse
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.ports.input.CancelOrderRequestUseCase
import com.acme.insurance.core.ports.input.CreateOrderUseCase
import com.acme.insurance.core.ports.input.FindAllOrdersByCustomerIdUseCase
import com.acme.insurance.core.ports.input.FindOrderByIdUseCase
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
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class OrderControllerTest {
    @MockK
    lateinit var createOrderUseCase: CreateOrderUseCase

    @MockK
    lateinit var findOrderByIdUseCase: FindOrderByIdUseCase

    @MockK
    lateinit var cancelOrderRequestUseCase: CancelOrderRequestUseCase

    @MockK
    lateinit var findAllOrdersByCustomerIdUseCase: FindAllOrdersByCustomerIdUseCase

    @InjectMockKs
    private lateinit var orderController: OrderController

    private val orderId: String = "orderId"
    private val customerId: String = "orderId"

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should create order and return CREATED response`() {
        val request = Mocks.createOrderRequest()
        val response = request.toDomain().copy(id = orderId).toCreateOrderResponse()

        every { createOrderUseCase.execute(request) } returns response

        val result = orderController.createOrder(request)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(HttpStatus.CREATED, result.statusCode) },
            { assertEquals(orderId, result.body?.id) },
        )

        verify(exactly = 1) { createOrderUseCase.execute(request) }
    }

    @Test
    fun `should find order by id and return OK response`() {
        val response =
            Mocks
                .createOrderRequest()
                .toDomain()
                .copy(id = orderId)
                .toOrderResponse()
        every { findOrderByIdUseCase.execute(orderId) } returns response

        val result = orderController.findOrderById(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(HttpStatus.OK, result.statusCode) },
            { assertEquals(orderId, result.body?.id) },
            { assertEquals(Status.RECEIVED.value, result.body?.status) },
        )

        verify(exactly = 1) { findOrderByIdUseCase.execute(orderId) }
    }

    @Test
    fun `should cancel order and return OK response`() {
        val response =
            Mocks
                .createOrderRequest()
                .toDomain()
                .copy(id = orderId, status = Status.CANCELLED)
                .toOrderResponse()
        every { findOrderByIdUseCase.execute(orderId) } returns response
        every { cancelOrderRequestUseCase.execute(orderId) } returns response

        val result = orderController.cancelOrder(orderId)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(HttpStatus.OK, result.statusCode) },
            { assertEquals(orderId, result.body?.id) },
            { assertEquals(Status.CANCELLED.value, result.body?.status) },
        )

        verify(exactly = 1) { cancelOrderRequestUseCase.execute(orderId) }
    }

    @Test
    fun `should find all orders by customer id and return OK response when not empty`() {
        val orders = listOf(Mocks.createOrder().copy(customerId = customerId)).map { it.toOrderResponse() }
        every { findAllOrdersByCustomerIdUseCase.execute(customerId) } returns orders

        val result = orderController.findAllOrdersByCustomerId(customerId)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(HttpStatus.OK, result.statusCode) },
            { assertEquals(customerId, result.body?.first()?.customerId) },
            { assertEquals(1, result.body?.size) },
        )
        verify(exactly = 1) { findAllOrdersByCustomerIdUseCase.execute(customerId) }
    }

    @Test
    fun `should find all orders by customer id and return NO_CONTENT when empty`() {
        every { findAllOrdersByCustomerIdUseCase.execute(customerId) } returns emptyList()

        val result = orderController.findAllOrdersByCustomerId(customerId)

        assertAll(
            {},
            { assertNull(result.body) },
            { assertEquals(HttpStatus.NO_CONTENT, result.statusCode) },
        )

        verify(exactly = 1) { findAllOrdersByCustomerIdUseCase.execute(customerId) }
    }
}
