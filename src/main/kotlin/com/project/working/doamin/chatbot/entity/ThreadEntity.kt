package com.project.working.doamin.chatbot.entity

import com.project.working.doamin.user.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "threads")
@EntityListeners(AuditingEntityListener::class)
class ThreadEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Comment("생성일시")
    @CreationTimestamp
    val createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    @Comment("해당 스레드 마지막 대화 시점")
    var lastChatAt: Instant = Instant.now()

)