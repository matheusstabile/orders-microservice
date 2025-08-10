package com.acme.insurance.util

import com.acme.insurance.adapters.input.messaging.kafka.event.InsuranceSubscriptionEvent
import com.acme.insurance.adapters.input.messaging.kafka.event.OrderEvent
import com.acme.insurance.adapters.input.messaging.kafka.event.PaymentEvent
import com.acme.insurance.adapters.output.web.dto.OccurrencesResponse
import com.acme.insurance.adapters.output.web.dto.RiskClassificationResponse
import com.acme.insurance.core.application.converter.toDomain
import com.acme.insurance.core.application.dto.CreateOrderRequest
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.domain.model.riskclassification.Classification
import com.acme.insurance.core.domain.model.riskclassification.Occurrences
import com.acme.insurance.core.domain.model.riskclassification.RiskClassification
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

object Mocks {
    fun createOrderRequest() =
        CreateOrderRequest(
            customerId = "customerId",
            productId = "productId",
            category = "AUTO",
            salesChannel = "online",
            paymentMethod = "credit_card",
            totalMonthlyPremiumAmount = BigDecimal("100.00"),
            insuredAmount = BigDecimal("10000.00"),
            coverages = emptyMap(),
            assistances = emptyList(),
        )

    fun createOrder() =
        createOrderRequest()
            .toDomain()
            .copy(id = "orderId", createdAt = Instant.parse("2023-10-01T12:00:00Z"))

    fun createRiskClassification() =
        RiskClassification(
            orderId = "orderId",
            customerId = "customerId",
            analyzedAt = Instant.now(),
            classification = Classification.REGULAR,
            occurrences =
                listOf(
                    Occurrences(
                        id = "occurrenceId",
                        description = "No accidents",
                        productId = "productId",
                        type = "type",
                        createdAt = Instant.now(),
                        updatedAt = Instant.now(),
                    ),
                ),
        )

    fun createConsumerRecord(
        topic: String,
        payload: String,
        status: Status,
    ): ConsumerRecord<String, String> =
        ConsumerRecord(
            topic,
            0,
            0L,
            System.currentTimeMillis(),
            TimestampType.CREATE_TIME,
            3,
            20,
            "key",
            payload,
            RecordHeaders().apply { add("orderStatus", status.value.toByteArray()) },
            Optional.empty(),
        )

    fun createOrderEvent() = OrderEvent(id = "orderId")

    fun createPaymentEvent() = PaymentEvent(id = "orderId", orderId = "orderId")

    fun createInsuranceSubscriptionEvent() = InsuranceSubscriptionEvent(orderId = "orderId", id = "subscriptionId")

    fun createRiskClassificationResponse() =
        RiskClassificationResponse(
            orderId = "orderId",
            customerId = "customerId",
            analyzedAt = Instant.now(),
            classification = Classification.REGULAR.value,
            occurrences =
                listOf(
                    OccurrencesResponse(
                        id = "occurrenceId",
                        description = "No accidents",
                        productId = "productId",
                        type = "type",
                        createdAt = Instant.now(),
                        updatedAt = Instant.now(),
                    ),
                ),
        )
}
