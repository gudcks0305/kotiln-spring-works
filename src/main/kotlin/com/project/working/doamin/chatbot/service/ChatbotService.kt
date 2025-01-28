package com.project.working.doamin.chatbot.service

import com.project.working.doamin.chatbot.controller.ChatbotRequest
import com.project.working.doamin.chatbot.entity.Chat
import com.project.working.doamin.user.repository.UserRepository
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.StreamingChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Transactional
@Service
class ChatbotService(
    private val chatModel: ChatModel,
    private val streamingChatModel : StreamingChatModel,
    private val chatService: ChatService,
    private val threadService: ThreadService,
    private val userRepository: UserRepository
) {

    fun ask(
        chatHistory : List<Chat>,
        question: String,
        model: String?,
    ): ChatResponse {
        val createMessageByChatHistory = createMessageByChatHistory(chatHistory)
        createMessageByChatHistory.add(UserMessage(question))

        val response = chatModel.call(
            Prompt(
                createMessageByChatHistory,
                OpenAiChatOptions.builder().withModel(model)
                    .withTemperature(0.4)
                    .build()
            )
        )
        return response
    }

    fun askStreaming(
        chatHistory : List<Chat>,
        question: String,
        model: String?,
    ): Flux<ChatResponse> {
        val createMessageByChatHistory = createMessageByChatHistory(chatHistory)
        createMessageByChatHistory.add(UserMessage(question))

        val response = streamingChatModel.stream(
            Prompt(
                createMessageByChatHistory,
                OpenAiChatOptions.builder().withModel(model).withStreamUsage(true)
                    .withTemperature(0.4)
                    .build()
            )
        )
        return response
    }


    fun createMessageByChatHistory(
        chatHistory: List<Chat>
    ):  MutableList<Message> {
        val messages : MutableList<Message> = mutableListOf()
        chatHistory.sortedByDescending { it.createdAt }
            .forEach {
                messages.add(
                    UserMessage(it.question)
                )
                messages.add(
                    AssistantMessage(it.answer)
                )
            }
        return messages
    }

    fun askChatBot(
        userId: Long,
        request: ChatbotRequest,
        isStreaming: Boolean
    ): Any {
        val user = userRepository.findById(userId).orElseThrow()
        val thread = threadService.getOrCreateThread(user)
        val chatHistory = chatService.getChatsByThread(thread.id!!)

        return if (isStreaming) {
            val resultBuilder = StringBuilder()
            askStreaming(chatHistory, request.question, request.model)
                .publishOn(Schedulers.boundedElastic())

        } else {
            val response = ask(chatHistory, request.question, request.model)
            chatService.createChat(thread, request.question, response.result.output.content)
            response
        }
    }



}
