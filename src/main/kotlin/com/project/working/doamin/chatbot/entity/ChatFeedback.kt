package com.project.working.doamin.chatbot.entity

import com.project.working.doamin.user.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

enum class FeedbackStatus {
    PENDING, RESOLVED
}

@Entity
@Table(name = "feedbacks")
@EntityListeners(AuditingEntityListener::class)
class ChatFeedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    val chat: Chat,

    @Comment("공감 여부 (true=공감, false=부정)")
    val isPositive: Boolean,

    @Comment("생성일시")
    @CreationTimestamp
    val createdAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: FeedbackStatus = FeedbackStatus.PENDING
)