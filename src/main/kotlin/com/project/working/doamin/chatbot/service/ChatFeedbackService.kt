package com.project.working.doamin.chatbot.service

import com.project.working.doamin.auth.enums.AuthorityEnums
import com.project.working.doamin.chatbot.dto.ChatFeedbackDto
import com.project.working.doamin.chatbot.entity.ChatFeedback
import com.project.working.doamin.chatbot.entity.FeedbackStatus
import com.project.working.doamin.chatbot.repository.ChatRepository
import com.project.working.doamin.chatbot.repository.FeedbackRepository
import com.project.working.doamin.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class ChatFeedbackService(
    private val chatFeedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {

    //- 피드백 목록 조회
    //    - 각 사용자는 자신이 생성한 피드백만 조회 가능하며, 관리자 권한의 사용자는 모든 피드백을 조회할 수 있습니다.
    //    - 생성일시 기준으로 오름차순/내림차순 정렬이 가능하며 페이지네이션이 가능해야 합니다.
    //    - 긍정/부정 유무로 필터링할 수 있습니다.
    //- 피드백 상태 변경
    //    - 관리자는 피드백의 상태를 업데이트 할 수 있습니다.

    fun createFeedback(userId: Long, chatId: Long, isPositive: Boolean) {
        val chat = chatRepository.findById(chatId).orElseThrow { IllegalArgumentException("존재하지 않는 대화입니다.") }
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }
        if (chat.threadEntity.user.id != userId && user.role != AuthorityEnums.ROLE_ADMIN) {
            throw IllegalArgumentException("자신이 생성한 대화에만 피드백을 생성할 수 있습니다.")
        }
        chatFeedbackRepository.findByUserIdAndChatId(userId, chatId)?.let {
            throw IllegalArgumentException("이미 피드백을 생성한 대화입니다.")
        }
        chatFeedbackRepository.save(ChatFeedback(userId, user, chat, isPositive))
    }

    fun updateFeedbackStatus(feedbackId: Long, status: FeedbackStatus) {
        val feedback = chatFeedbackRepository.findById(feedbackId).orElseThrow { IllegalArgumentException("존재하지 않는 피드백입니다.") }
        val user = userRepository.findById(feedback.user.id).orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }
        if (user.role != AuthorityEnums.ROLE_ADMIN) {
            throw IllegalArgumentException("관리자만 피드백 상태를 변경할 수 있습니다.")
        }
        feedback.status = status
        chatFeedbackRepository.save(feedback)
    }

    fun getFeedbackList(userId: Long, positive: Boolean?, pageable: Pageable): Page<ChatFeedback> {
        return chatFeedbackRepository.findAllByUserIdAndPositive(userId, positive, pageable)
    }

    fun getFeedbackDtoList(userId: Long, positive: Boolean?, pageable: Pageable): Page<ChatFeedbackDto> {
        val feedbackList = chatFeedbackRepository.findAllByUserIdAndPositive(userId, positive, pageable)
        return feedbackList.map { ChatFeedbackDto(
            id = it.id!!,
            userId = it.user.id,
            chatId = it.chat.id!!,
            createdAt = it.createdAt,
            isPositive = it.isPositive
        ) }
    }

}