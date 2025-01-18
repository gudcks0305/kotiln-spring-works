package com.project.working

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class WorkingApplication

fun main(args: Array<String>) {
    runApplication<WorkingApplication>(*args)
}

