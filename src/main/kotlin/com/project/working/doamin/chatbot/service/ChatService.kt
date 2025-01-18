package com.project.working.doamin.chatbot.service

import com.project.working.doamin.chatbot.dto.ChatDto
import com.project.working.doamin.chatbot.entity.Chat
import com.project.working.doamin.chatbot.entity.ThreadEntity
import com.project.working.doamin.chatbot.repository.ChatRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    fun getChats(userId: Long, threadId: Long, pageable: Pageable): Page<Chat> {
        return chatRepository.findAllByThreadEntityId(threadId, pageable)
    }
    @Transactional(readOnly = true)
    fun getChatPageDto(userId: Long, threadId: Long, pageable: Pageable): Page<ChatDto> {
        val chats = getChats(userId, threadId, pageable)
        return chats.map { ChatDto(
            id = it.id!!,
            threadId = it.threadEntity.id!!,
            question = it.question,
            answer = it.answer
        ) }
    }
}