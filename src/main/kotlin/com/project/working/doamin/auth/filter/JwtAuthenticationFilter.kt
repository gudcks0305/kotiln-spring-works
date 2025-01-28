package com.project.working.doamin.auth.filter

import com.project.working.doamin.auth.dto.AuthenticatedUser
import com.project.working.doamin.auth.jwt.JwtTokenProvider
import com.project.working.doamin.user.dto.UserDto
import com.project.working.doamin.user.entity.User
import com.project.working.doamin.user.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userQueryRepository: UserRepository
) : OncePerRequestFilter() {
    private val repository = RequestAttributeSecurityContextRepository();

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        if (request.requestURI.contains("/api/v1/login") || request.requestURI.contains("/api/v1/signup")) {
            filterChain.doFilter(request, response)
            return
        }
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)

        try {
            extractToken(authorization)
                .ifPresentOrElse(
                    { jwtToken: String? ->
                        jwtTokenProvider.validateToken(jwtToken!!)
                        val authentication =
                            getAuthentication(jwtTokenProvider.getSubject(jwtToken))
                        SecurityContextHolder.getContext().authentication = authentication
                        // https://github.com/spring-projects/spring-security/issues/12758
                        this.repository.saveContext(SecurityContextHolder.getContext(), request, response)
                    },
                    {})

            filterChain.doFilter(request, response)

        } catch (e: Exception) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            throw e
        }
    }

    private fun extractToken(authorization: String): Optional<String> { // resolve AccessToken
        if (StringUtils.hasText(authorization) && Pattern.matches("^Bearer .*", authorization)) {
            val value = authorization.replace("^Bearer( )*".toRegex(), "")

            return if (StringUtils.hasText(value)) Optional.of(value) else Optional.empty()
        }

        return Optional.empty()
    }

    private fun getAuthentication(subject: String): Authentication {
        val userId: Long
        try {
            userId = subject.toLong()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid JWT Token")
        }

        val user: UserDto = userQueryRepository.findUserDtoById(userId)
        val authenticatedUser: AuthenticatedUser = AuthenticatedUser(user)

        return UsernamePasswordAuthenticationToken(
            authenticatedUser, null, authenticatedUser.getAuthorities()
        )
    }


}
