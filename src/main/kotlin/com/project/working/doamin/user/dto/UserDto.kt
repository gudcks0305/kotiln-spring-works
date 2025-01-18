package com.project.working.doamin.user.dto

import com.project.working.doamin.auth.enums.AuthorityEnums
import java.time.Instant

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val role: AuthorityEnums,
    val createdAt: Instant,
    val password : String
)
