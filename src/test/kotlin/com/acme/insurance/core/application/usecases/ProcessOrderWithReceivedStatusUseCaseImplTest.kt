package com.acme.insurance.core.application.usecases

import com.acme.insurance.core.application.dto.OrderResponse
import com.acme.insurance.core.application.validator.RiskClassificationValidator
import com.acme.insurance.core.domain.model.order.Category
import com.acme.insurance.core.domain.model.order.History
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.domain.model.order.Status.VALIDATED
import com.acme.insurance.core.domain.model.riskclassification.RiskClassification
import com.acme.insurance.core.ports.output.MessageProducerPort
import com.acme.insurance.core.ports.output.RepositoryPort
import com.acme.insurance.core.ports.output.RiskClassificationPort
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
import java.math.BigDecimal
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class ProcessOrderWithReceivedStatusUseCaseImplTest {
    @MockK
    lateinit var repositoryPort: RepositoryPort

    @MockK
    lateinit var riskClassificationPort: RiskClassificationPort

    @MockK
    lateinit var messageProducerPort: MessageProducerPort

    @MockK
    lateinit var riskClassificationValidator: RiskClassificationValidator

    @InjectMockKs
    lateinit var processOrderWithReceivedStatusUseCaseImpl: ProcessOrderWithReceivedStatusUseCaseImpl

    private var orderMock: Order = Mocks.createOrder()
    private var orderId: String = "orderId"
    private var riskClassificationMock: RiskClassification = Mocks.createRiskClassification()

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should process order with received status successfully`() {
        val order = orderMock.copy(insuredAmount = BigDecimal.valueOf(200_000), category = Category.LIFE)

        every { repositoryPort.findById(orderId = orderId) } returns order
        every { riskClassificationPort.classifyRisk(order = order) } returns riskClassificationMock
        every {
            riskClassificationValidator.validateRiskClassification(
                order = order,
                classification = riskClassificationMock.classification,
            )
        } returns order
        every { repositoryPort.save(order = any(Order::class)) } returns
            order.copy(
                status = VALIDATED,
                history = order.history + History(status = VALIDATED, timestamp = order.createdAt),
            )
        every {
            messageProducerPort.publishEvent(
                orderId = orderId,
                orderStatus = VALIDATED.value,
            )
        } returns Unit

        val result = processOrderWithReceivedStatusUseCaseImpl.execute(orderId = orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<OrderResponse>(result) },
            { assertEquals(VALIDATED.value, result?.status) },
            { assertEquals(result?.history?.size, 2) },
            { assertEquals(result?.history?.last()?.status, VALIDATED.value) },
        )

        verify(exactly = 1) { repositoryPort.findById(orderId = orderId) }
        verify(exactly = 1) { riskClassificationPort.classifyRisk(order = order) }
        verify(exactly = 1) {
            riskClassificationValidator.validateRiskClassification(
                order = order,
                classification = riskClassificationMock.classification,
            )
        }
        verify(exactly = 1) { repositoryPort.save(order = any(Order::class)) }
        verify(exactly = 1) { messageProducerPort.publishEvent(orderId = orderId, orderStatus = VALIDATED.value) }
    }

    @Test
    fun `should return OrderResponse when order is not in RECEIVED status`() {
        val order = orderMock.copy(status = Status.REJECTED)

        every { repositoryPort.findById(orderId) } returns order

        val result = processOrderWithReceivedStatusUseCaseImpl.execute(orderId = orderId)

        assertAll(
            { assertNotNull(result) },
            { assertInstanceOf<OrderResponse>(result) },
        )

        verify(exactly = 1) { repositoryPort.findById(orderId = orderId) }
        verify(exactly = 0) { riskClassificationPort.classifyRisk(order = any()) }
        verify(exactly = 0) {
            riskClassificationValidator.validateRiskClassification(
                order = any(),
                classification = any(),
            )
        }
        verify(exactly = 0) { repositoryPort.save(order = any(Order::class)) }
        verify(exactly = 0) { messageProducerPort.publishEvent(orderId = orderId, orderStatus = any()) }
    }

    @Test
    fun `should return null when an exception occurs during processing`() {
        every { repositoryPort.findById(orderId) } throws RuntimeException("Database error")

        val result = processOrderWithReceivedStatusUseCaseImpl.execute(orderId = orderId)

        assertEquals(null, result)

        verify(exactly = 1) { repositoryPort.findById(orderId = orderId) }
        verify(exactly = 0) { riskClassificationPort.classifyRisk(order = any()) }
        verify(exactly = 0) {
            riskClassificationValidator.validateRiskClassification(
                order = any(),
                classification = any(),
            )
        }
        verify(exactly = 0) { repositoryPort.save(order = any(Order::class)) }
        verify(exactly = 0) { messageProducerPort.publishEvent(orderId = orderId, orderStatus = any()) }
    }
}
