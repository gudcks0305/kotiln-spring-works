package com.project.working.doamin.chatbot.repository

import com.project.working.doamin.chatbot.entity.ChatFeedback
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<ChatFeedback, Long> {
    fun findByUserId(userId: Long): List<ChatFeedback>
    fun findByChatId(chatId: Long): List<ChatFeedback>
}