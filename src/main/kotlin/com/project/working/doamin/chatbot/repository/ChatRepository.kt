package com.project.working.doamin.chatbot.repository

import com.project.working.doamin.chatbot.entity.Chat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findByThreadEntityId(threadId: Long): List<Chat>
}