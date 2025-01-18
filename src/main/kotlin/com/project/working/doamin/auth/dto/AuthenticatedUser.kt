package com.project.working.doamin.auth.dto

import com.project.working.doamin.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.List

class AuthenticatedUser(user: User) : UserDetails {
    @Transient
    private val user: User = user

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(GrantedAuthority { user.role.name })
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return user.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    val userId: Long
        get() = user.id

}
