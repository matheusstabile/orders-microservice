package com.acme.insurance.adapters.output.web

import com.acme.insurance.adapters.output.web.dto.RiskClassificationRequest
import com.acme.insurance.adapters.output.web.dto.RiskClassificationResponse
import com.acme.insurance.core.domain.model.order.Order
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class RiskClassificationAdapterTest {
    @MockK
    lateinit var restTemplate: RestTemplate

    @InjectMockKs
    lateinit var adapter: RiskClassificationAdapter

    private val orderId: String = "orderId"
    private val riskClassificationResponse = Mocks.createRiskClassificationResponse()
    private var orderMock: Order = Mocks.createOrder().copy(id = orderId)

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return risk classification when response is 2xx and body is not null`() {
        every {
            restTemplate.postForEntity(
                any<String>(),
                any(RiskClassificationRequest::class),
                RiskClassificationResponse::class.java,
            )
        } returns ResponseEntity(riskClassificationResponse, HttpStatus.OK)

        val result = adapter.classifyRisk(orderMock)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(riskClassificationResponse.customerId, result.customerId) },
            { assertEquals(riskClassificationResponse.orderId, result.orderId) },
        )
        verify(exactly = 1) { restTemplate.postForEntity(any<String>(), any(), RiskClassificationResponse::class.java) }
    }

    @Test
    fun `should throw when response is 2xx but body is null`() {
        every {
            restTemplate.postForEntity(
                any<String>(),
                any(),
                RiskClassificationResponse::class.java,
            )
        } returns ResponseEntity(null, HttpStatus.OK)

        val exception =
            assertThrows<RuntimeException> {
                adapter.classifyRisk(orderMock)
            }
        assertEquals("Response body is null", exception.message)
        verify(exactly = 1) { restTemplate.postForEntity(any<String>(), any(), RiskClassificationResponse::class.java) }
    }

    @Test
    fun `should throw when response is not 2xx`() {
        every {
            restTemplate.postForEntity(
                any<String>(),
                any(),
                RiskClassificationResponse::class.java,
            )
        } returns ResponseEntity(null, HttpStatus.BAD_REQUEST)

        val exception =
            assertThrows<RuntimeException> {
                adapter.classifyRisk(orderMock)
            }

        assertEquals("Failed to retrieve risk classification: 400 BAD_REQUEST", exception.message)

        verify(exactly = 1) { restTemplate.postForEntity(any<String>(), any(), RiskClassificationResponse::class.java) }
    }
}
