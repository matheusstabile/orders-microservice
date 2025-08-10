package com.acme.insurance.core.ports.output

interface MessageProducerPort {
    fun publishEvent(
        orderId: String,
        orderStatus: String,
    )
}
