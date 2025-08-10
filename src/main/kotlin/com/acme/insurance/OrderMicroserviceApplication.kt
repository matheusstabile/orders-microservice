package com.acme.insurance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PolicyLifecycleServiceApplication

fun main(args: Array<String>) {
	runApplication<PolicyLifecycleServiceApplication>(*args)
}
