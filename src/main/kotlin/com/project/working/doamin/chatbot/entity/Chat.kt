package com.project.working.doamin.chatbot.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "chats")
@EntityListeners(AuditingEntityListener::class)
class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    val threadEntity: ThreadEntity,

    @Comment("질문")
    @Column(nullable = false, length = 2000)
    val question: String,

    @Comment("답변")
    @Column(nullable = false, length = 5000)
    val answer: String,

    @Comment("생성일시")
    @CreationTimestamp
    val createdAt: Instant = Instant.now(),

    )