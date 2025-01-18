package com.project.working

import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import dev.langchain4j.model.openai.OpenAiTokenizer
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.spring.AiService;
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class WorkingApplication

fun main(args: Array<String>) {
    runApplication<WorkingApplication>(*args)
}

@AiService
interface Assistant {
    @SystemMessage("You are a polite assistant")
    fun chat(userMessage: String?): String?
}