package com.project.working.doamin.auth.jwt

import com.project.working.doamin.user.entity.User
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import jakarta.annotation.PostConstruct
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {
    @Value("\${security.access-token.expiry-in-milli}")
    private val tokenExpiryInMilli: Long = 0

    @Value("\${security.access-token.jwt-secret-key}")
    private val signatureSecretKey: String? = null

    private var key: SecretKey? = null

    @PostConstruct
    fun init() {
        this.key = Keys.hmacShaKeyFor(signatureSecretKey!!.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateAccessTokenValue(user: User): String {
        return Jwts.builder()
            .subject(java.lang.String.valueOf(user.id))
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + tokenExpiryInMilli))
            .signWith(key)
            .compact()
    }

    fun validateToken(jwtToken: String) {
        try {
            val expiration = parseClaimsJws(jwtToken).payload.expiration

            if (expiration.before(Date())) {
                throw IllegalArgumentException("Expired JWT Token")
            }
        } catch (e: SecurityException) {
            throw IllegalArgumentException("Expired JWT Token")
        } catch (e: MalformedJwtException) {
            throw IllegalArgumentException("Expired JWT Token")
        } catch (e: ExpiredJwtException) {
            throw IllegalArgumentException("Expired JWT Token")
        } catch (e: UnsupportedJwtException) {
            throw IllegalArgumentException("Expired JWT Token")
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Expired JWT Token")
        }
    }

    private fun parseClaimsJws(jwtToken: String): Jws<Claims> {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtToken)
    }

    fun getClaimValue(jwtToken: String, claimName: String): Optional<Any> {
        return Optional.ofNullable(parseClaimsJws(jwtToken).payload[claimName])
    }

    fun getSubject(jwtToken: String): String {
        val jws = parseClaimsJws(jwtToken)

        return jws.payload.subject
    }
}
