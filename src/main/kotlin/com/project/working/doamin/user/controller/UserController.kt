package com.project.working.doamin.user.controller

import com.project.working.doamin.auth.jwt.JwtTokenProvider
import com.project.working.doamin.user.entity.User
import com.project.working.doamin.user.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/")
class UserController(
    private val userService: UserService
) {

    @PostMapping("signup")
    fun createUser(@RequestBody @Validated request: CreateUserRequest): ResponseEntity<Any> {
        userService.createUser(request.name, request.email, request.password)
        return ResponseEntity.ok("success")
    }

    @GetMapping("users")
    fun getUser(@RequestParam email: String) = userService.getUserByEmail(email) ?: "User not found"

    @PostMapping("login")
    fun login(@RequestBody request: LoginDto.Request): ResponseEntity<LoginDto.Response> {
        val response = userService.login(request)
        return ResponseEntity.ok(response)
    }


}

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String
)


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun createUser(name: String, email: String, password: String): User {
        // 예: 중복 이메일 체크
        val existingUser = userRepository.findByEmail(email)
        require(existingUser == null) { "이미 존재하는 이메일입니다: $email" }

        val user = User(name = name, email = email, password = passwordEncoder.encode(password))
        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginDto.Request): LoginDto.Response {
        val user = getUserByEmail(request.id)
        require(user != null) { "User not found" }
        require(verifyUserCredential(request.password, user.password)) { "Invalid password" }
        return LoginDto.Response(
            jwtTokenProvider.generateAccessTokenValue(user), user.id
        )
    }

    private fun verifyUserCredential(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
}

class LoginDto {
    data class Request(
        val id: String,
        val password: String
    )

    data class Response(val accessToken: String, val userId: Long)
}
