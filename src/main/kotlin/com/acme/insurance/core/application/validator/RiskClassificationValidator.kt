package com.acme.insurance.core.application.validator

import com.acme.insurance.core.domain.model.order.Category.AUTO
import com.acme.insurance.core.domain.model.order.Category.LIFE
import com.acme.insurance.core.domain.model.order.Category.OTHER
import com.acme.insurance.core.domain.model.order.Category.RESIDENTIAL
import com.acme.insurance.core.domain.model.order.History
import com.acme.insurance.core.domain.model.order.Order
import com.acme.insurance.core.domain.model.order.Status
import com.acme.insurance.core.domain.model.riskclassification.Classification
import com.acme.insurance.core.domain.model.riskclassification.Classification.HIGH_RISK
import com.acme.insurance.core.domain.model.riskclassification.Classification.NO_INFORMATION
import com.acme.insurance.core.domain.model.riskclassification.Classification.PREFERENTIAL
import com.acme.insurance.core.domain.model.riskclassification.Classification.REGULAR
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant

@Component
class RiskClassificationValidator {
    fun validateRiskClassification(
        order: Order,
        classification: Classification,
    ): Order =
        when (isWithinLimit(order = order, classification = classification)) {
            true ->
                order.copy(
                    status = Status.VALIDATED,
                    history = order.history + History(status = Status.VALIDATED, timestamp = Instant.now()),
                )

            false ->
                order.copy(
                    status = Status.REJECTED,
                    history = order.history + History(status = Status.REJECTED, timestamp = Instant.now()),
                )
        }

    private fun isWithinLimit(
        order: Order,
        classification: Classification,
    ): Boolean =
        when (classification) {
            REGULAR ->
                when (order.category) {
                    LIFE, RESIDENTIAL -> order.insuredAmount <= BigDecimal(500_000)
                    AUTO -> order.insuredAmount <= BigDecimal(350_000)
                    OTHER -> order.insuredAmount <= BigDecimal(255_000)
                }

            HIGH_RISK ->
                when (order.category) {
                    AUTO -> order.insuredAmount <= BigDecimal(250_000)
                    RESIDENTIAL -> order.insuredAmount <= BigDecimal(150_000)
                    LIFE, OTHER -> order.insuredAmount <= BigDecimal(125_000)
                }

            PREFERENTIAL ->
                when (order.category) {
                    LIFE -> order.insuredAmount < BigDecimal(800_000)
                    AUTO, RESIDENTIAL -> order.insuredAmount < BigDecimal(450_000)
                    OTHER -> order.insuredAmount <= BigDecimal(375_000)
                }

            NO_INFORMATION ->
                when (order.category) {
                    LIFE, RESIDENTIAL -> order.insuredAmount <= BigDecimal(200_000)
                    AUTO -> order.insuredAmount <= BigDecimal(75_000)
                    OTHER -> order.insuredAmount <= BigDecimal(55_000)
                }
        }
}
