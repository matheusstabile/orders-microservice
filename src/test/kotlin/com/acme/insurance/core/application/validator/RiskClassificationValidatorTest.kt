package com.acme.insurance.core.application.validator

import com.acme.insurance.core.domain.model.order.Category
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.domain.model.riskclassification.Classification
import com.acme.insurance.util.Mocks
import io.mockk.clearAllMocks
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class RiskClassificationValidatorTest {
    private val validator = RiskClassificationValidator()
    private val orderMock = Mocks.createOrder()

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @ParameterizedTest
    @ValueSource(ints = [499_999, 500_000, 500_001])
    fun `should validate REGULAR client with LIFE insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.LIFE, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.REGULAR)
        val expectedStatus = if (insuredAmount <= 500_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [499_999, 500_000, 500_001])
    fun `should validate REGULAR client with RESIDENTIAL insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.RESIDENTIAL, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.REGULAR)
        val expectedStatus = if (insuredAmount <= 500_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [349_999, 350_000, 350_001])
    fun `should validate REGULAR client with AUTO insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.AUTO, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.REGULAR)
        val expectedStatus = if (insuredAmount <= 350_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [254_999, 255_000, 255_001])
    fun `should validate REGULAR client with OTHER insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.OTHER, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.REGULAR)
        val expectedStatus = if (insuredAmount <= 255_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [249_999, 250_000, 250_001])
    fun `should validate HIGH_RISK client with AUTO insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.AUTO, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.HIGH_RISK)
        val expectedStatus = if (insuredAmount <= 250_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [149_999, 150_000, 150_001])
    fun `should validate HIGH_RISK client with RESIDENTIAL insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.RESIDENTIAL, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.HIGH_RISK)
        val expectedStatus = if (insuredAmount <= 150_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [124_999, 125_000, 125_001])
    fun `should validate HIGH_RISK client with LIFE insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.LIFE, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.HIGH_RISK)
        val expectedStatus = if (insuredAmount <= 125_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [124_999, 125_000, 125_001])
    fun `should validate HIGH_RISK client with OTHER insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.OTHER, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.HIGH_RISK)
        val expectedStatus = if (insuredAmount <= 125_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [799_998, 799_999, 800_000])
    fun `should validate PREFERENTIAL client with LIFE insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.LIFE, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.PREFERENTIAL)
        val expectedStatus = if (insuredAmount < 800_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [449_998, 449_999, 450_000])
    fun `should validate PREFERENTIAL client with AUTO insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.AUTO, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.PREFERENTIAL)
        val expectedStatus = if (insuredAmount < 450_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [449_998, 449_999, 450_000])
    fun `should validate PREFERENTIAL client with RESIDENTIAL insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.RESIDENTIAL, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.PREFERENTIAL)
        val expectedStatus = if (insuredAmount < 450_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [374_999, 375_000, 375_001])
    fun `should validate PREFERENTIAL client with OTHER insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.OTHER, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.PREFERENTIAL)
        val expectedStatus = if (insuredAmount <= 375_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [199_999, 200_000, 200_001])
    fun `should validate NO_INFORMATION client with LIFE insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.LIFE, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.NO_INFORMATION)
        val expectedStatus = if (insuredAmount <= 200_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [199_999, 200_000, 200_001])
    fun `should validate NO_INFORMATION client with RESIDENTIAL insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.RESIDENTIAL, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.NO_INFORMATION)
        val expectedStatus = if (insuredAmount <= 200_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [74_999, 75_000, 75_001])
    fun `should validate NO_INFORMATION client with AUTO insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.AUTO, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.NO_INFORMATION)
        val expectedStatus = if (insuredAmount <= 75_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }

    @ParameterizedTest
    @ValueSource(ints = [54_999, 55_000, 55_001])
    fun `should validate NO_INFORMATION client with OTHER insurance under, at, and above limit`(insuredAmount: Int) {
        val order = orderMock.copy(category = Category.OTHER, insuredAmount = BigDecimal(insuredAmount))
        val result = validator.validateRiskClassification(order, Classification.NO_INFORMATION)
        val expectedStatus = if (insuredAmount <= 55_000) Status.VALIDATED else Status.REJECTED
        assertEquals(expectedStatus, result.status)
        assertEquals(2, result.history.size)
        assertEquals(expectedStatus, result.history.last().status)
    }
}
