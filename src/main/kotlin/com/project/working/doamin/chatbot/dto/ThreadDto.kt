package com.project.working.doamin.chatbot.dto

import java.time.Instant

data class ThreadDto(
    val id: Long,
    val userId: Long,
    val createdAt: Instant,
    val lastChatAt: Instant
)