package com.project.working.doamin.chatbot.controller

import com.project.working.doamin.auth.dto.AuthenticatedUser
import com.project.working.doamin.chatbot.entity.FeedbackStatus
import com.project.working.doamin.chatbot.service.ChatFeedbackService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/chat/feedback")
class ChatFeedbackController(
    private val chatFeedbackService: ChatFeedbackService
) {

    @PostMapping("/chats/{chatId}")
    fun feedback(
        @RequestBody request: ChatFeedbackRequest,
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable chatId: Long
    ): ResponseEntity<Any> {
        chatFeedbackService.createFeedback(user.userId,chatId, request.isPositive)
        return ResponseEntity.ok("success")
    }

    @PatchMapping("/{feedbackId}")
    fun updateFeedbackStatus(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable feedbackId: Long,
        @RequestParam status: FeedbackStatus
    ): ResponseEntity<Any> {
        chatFeedbackService.updateFeedbackStatus(feedbackId, status)
        return ResponseEntity.ok("success")
    }

    @GetMapping("")
    fun getFeedbackList(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestParam(required = false) isPositive: Boolean?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable

    ): ResponseEntity<Any> {
        return ResponseEntity.ok(chatFeedbackService.getFeedbackDtoList(user.userId, isPositive, pageable))
    }

}

data class ChatFeedbackRequest(
    val isPositive: Boolean
)