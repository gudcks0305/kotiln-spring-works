package com.project.working.doamin.user.entity

import com.project.working.doamin.auth.enums.AuthorityEnums
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    val password: String,

    @Comment("생성일시")
    @CreatedDate
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
        )
    var createdAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: AuthorityEnums = AuthorityEnums.ROLE_USER

) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as User
        return id == that.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}