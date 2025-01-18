package com.project.working.doamin.chatbot.dto

import java.time.Instant

data class ChatFeedbackDto(
    val id: Long,
    val chatId: Long,
    val userId: Long,
    val isPositive: Boolean,
    val createdAt: Instant
) {
}