package com.project.working.doamin.chatbot.controller

import com.project.working.doamin.auth.dto.AuthenticatedUser
import com.project.working.doamin.chatbot.service.ChatbotService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/chat")
class ChatbotController(
    private val chatbotService: ChatbotService
) {

    @PostMapping("/ask")
    fun askChatBot(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestBody request: ChatbotRequest
    ): Any {
        return chatbotService.askChatBot(user.userId, request)
    }
}

data class ChatbotRequest(
    val question: String,
    val model: String?,
    val isStreaming: Boolean
)