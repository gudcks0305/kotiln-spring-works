package com.project.working.doamin.chatbot.service

import com.project.working.doamin.auth.enums.AuthorityEnums
import com.project.working.doamin.chatbot.dto.ThreadDto
import com.project.working.doamin.chatbot.entity.ThreadEntity
import com.project.working.doamin.chatbot.repository.ThreadRepository
import com.project.working.doamin.user.entity.User
import com.project.working.doamin.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
@Transactional
@Service
class ThreadService(
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository
) {
    private val THIRTY_MINUTES = Duration.parse("PT30M")

    @Transactional
    fun getOrCreateThread(user: User): ThreadEntity {
        // 사용자가 생성한 마지막 스레드를 찾고,
        // 30분 이내면 해당 스레드 반환, 아니면 새로 생성
        val userThreads = threadRepository.findByUserId(user.id)
            .sortedByDescending { it.lastChatAt } // 최신 스레드 먼저
        val lastThread = userThreads.firstOrNull()

        return if (lastThread != null && Duration.between(lastThread.lastChatAt, Instant.now()) < THIRTY_MINUTES) {
            lastThread
        } else {
            threadRepository.save(ThreadEntity(user = user))
        }
    }

    @Transactional
    fun updateThreadLastChat(thread: ThreadEntity) {
        thread.lastChatAt = Instant.now()
    }

    fun getThreads(userId: Long, pageable: Pageable): Page<ThreadEntity> {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with id $userId not found")
        }

        return if (user.role == AuthorityEnums.ROLE_ADMIN) {
            threadRepository.findAll(pageable)
        } else {
            threadRepository.findByUserId(userId, pageable)
        }
    }

    fun getThreadDtos(userId: Long, pageable: Pageable): Page<ThreadDto> {
        val threadEntities = getThreads(userId, pageable)
        return threadEntities.map { ThreadDto(
            id = it.id!!,
            userId = it.user.id,
            createdAt = it.createdAt,
            lastChatAt = it.lastChatAt
        ) }
    }

    fun deleteThread(
        userId: Long,
        threadId: Long
    ){
        val thread = threadRepository.findById(threadId).orElseThrow {
            IllegalArgumentException("Thread with id $threadId not found")
        }
        if (thread.user.id == userId) {
            threadRepository.delete(thread)
        }

        throw IllegalArgumentException("User with id $userId cannot delete thread with id $threadId")
    }
}