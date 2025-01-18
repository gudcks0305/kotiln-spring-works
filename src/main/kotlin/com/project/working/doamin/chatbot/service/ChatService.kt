package com.project.working.doamin.chatbot.service

import com.project.working.doamin.chatbot.entity.Chat
import com.project.working.doamin.chatbot.entity.ThreadEntity
import com.project.working.doamin.chatbot.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadService: ThreadService
) {
    @Transactional
    fun createChat(thread: ThreadEntity, question: String, answer: String): Chat {
        val newChat = chatRepository.save(
            Chat(threadEntity = thread, question = question, answer = answer)
        )
        threadService.updateThreadLastChat(thread) // 스레드 마지막 대화 시점 갱신
        return newChat
    }

    @Transactional(readOnly = true)
    fun getChatsByThread(threadId: Long): List<Chat> {
        return chatRepository.findByThreadEntityId(threadId)
    }
}