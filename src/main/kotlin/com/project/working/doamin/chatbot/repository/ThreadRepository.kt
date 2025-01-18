package com.project.working.doamin.chatbot.repository

import com.project.working.doamin.chatbot.entity.ThreadEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ThreadRepository : JpaRepository<ThreadEntity, Long> {
    fun findByUserId(userId: Long): List<ThreadEntity>
    fun findByUserId(userId: Long, pageable: Pageable): Page<ThreadEntity>
}