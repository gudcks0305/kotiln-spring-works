package com.project.working.doamin.user.repository

import com.project.working.doamin.user.dto.UserDto
import com.project.working.doamin.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    @Query("SELECT new com.project.working.doamin.user.dto.UserDto(u.id, u.email,u.name, u.role,u.createdAt,u.password) FROM User u WHERE u.id = :id")
    fun findUserDtoById(id: Long): UserDto
}