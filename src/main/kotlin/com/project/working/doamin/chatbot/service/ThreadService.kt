package com.project.working.doamin.chatbot.service

import com.project.working.doamin.chatbot.entity.ThreadEntity
import com.project.working.doamin.chatbot.repository.ThreadRepository
import com.project.working.doamin.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class ThreadService(
    private val threadRepository: ThreadRepository
) {
    private val THIRTY_MINUTES = Duration.parse("PT30M")

    @Transactional
    fun getOrCreateThread(user: User): ThreadEntity {
        // 사용자가 생성한 마지막 스레드를 찾고,
        // 30분 이내면 해당 스레드 반환, 아니면 새로 생성
        val userThreads = threadRepository.findByUserId(user.id!!)
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
}