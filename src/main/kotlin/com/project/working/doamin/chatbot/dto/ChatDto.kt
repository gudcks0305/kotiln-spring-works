package com.project.working.doamin.chatbot.dto

data class ChatDto(
    val id: Long,
    val threadId: Long,
    val question: String,
    val answer: String
)