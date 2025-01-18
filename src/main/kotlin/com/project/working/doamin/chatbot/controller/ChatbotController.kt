package com.project.working.doamin.chatbot.controller

import com.project.working.doamin.auth.dto.AuthenticatedUser
import com.project.working.doamin.chatbot.service.ChatService
import com.project.working.doamin.chatbot.service.ChatbotService
import com.project.working.doamin.chatbot.service.ThreadService
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.StreamingChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers


@RestController
@RequestMapping("/api/v1/chat")
class ChatbotController(
    private val chatbotService: ChatbotService,
    private val threadService: ThreadService,
    private val chatService: ChatService
) {

    @PostMapping("/ask")
    fun askChatBot(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestBody request: ChatbotRequest
    ): Any {
        // Streaming 의 경우 현재 시큐리티 문제로 후순위 처리

        return if (request.isStreaming) {
            val streamingResponse = chatbotService.askChatBot(user.userId, request, true) as Flux<*>
            val emitter = SseEmitter()
            val result= ""
            streamingResponse
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    { response ->
                        response as ChatResponse
                        emitter.send(response.result.output.content, MediaType.APPLICATION_JSON)
                    },
                    { emitter.complete() },
                    { emitter.complete() }
                )
            emitter
        } else {
            val response = chatbotService.askChatBot(user.userId, request, false) as ChatResponse
            ResponseEntity.ok(response)
        }
    }

    @GetMapping("/threads")
    fun getThreads(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Any> {
        val threads = threadService.getThreadDtos(user.userId, pageable)
        return ResponseEntity.ok(threads)
    }

    @GetMapping("/threads/{threadId}/chats")
    fun getThreadChats(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable threadId: Long,
        @PageableDefault(
            size = 10,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Any> {
        val chats = chatService.getChatPageDto(user.userId, threadId, pageable)
        return ResponseEntity.ok(chats)
    }

    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable threadId: Long
    ): ResponseEntity<Any> {
        threadService.deleteThread(user.userId, threadId)
        return ResponseEntity.noContent().build()
    }

}

data class ChatbotRequest(
    val question: String,
    val model: String?,
    val isStreaming: Boolean
)