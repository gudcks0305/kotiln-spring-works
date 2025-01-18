#실행 방법 
- postgres docker 실행
- OPEN_API_KEY 발급
- ex : export OPEN_API_KEY=1234
- docker compose up -d
- ./gradlew bootRun


# 유스케이스 

가입 
```
POST http://localhost:8080/api/v1/signup
Content-Type: application/json

{
  "name": "test",
  "email": "test@fun-utils",
  "password": "password"
}
```

로그인
```
POST http://localhost:8080/api/v1/login
Content-Type: application/json

{
  "id": "test@fun-utils",
  "password": "password"
}
```

챗봇 요청 
```
POST http://localhost:8080/api/v1/chat/ask
Content-Type: application/json
Authorization: Bearer {token}

{
  "question": "안녕",
  "model": "gpt-4o-mini",
  "isStreaming": false
}
```

# 유스케이스: 채팅봇 컨트롤러

## 목적
사용자가 채팅봇과 상호작용하며 질문을 요청하거나, 특정 스레드에 대한 대화 목록을 조회 및 삭제할 수 있도록 지원합니다.

## 주요 액터
1. **사용자 (AuthenticatedUser)**: 채팅봇에 질문하고 대화 스레드를 관리할 권한을 가진 사용자.
2. **채팅봇 시스템**: 질문에 대한 응답을 생성하고, 스트리밍 데이터를 제공합니다.

---

## 유스케이스 설명

### 1. 채팅봇 질문 요청
- **시나리오**:  
  사용자는 채팅봇에게 질문을 던지고, 실시간 스트리밍 또는 일반 응답으로 결과를 받을 수 있습니다.
- **엔드포인트**:  
  `POST /api/v1/chat/ask`


- **처리 흐름**:
    1. 사용자 인증 정보를 기반으로 `userId`를 확인.
    2. 요청이 스트리밍인 경우, Flux를 사용하여 SSE로 데이터를 스트리밍.
    3. 일반 응답인 경우, ChatResponse를 반환.

---

### 2. 스레드 목록 조회
- **시나리오**:  
  사용자는 자신이 생성한 채팅 스레드 목록을 조회할 수 있습니다.
- **엔드포인트**:  
  `GET /api/v1/chat/threads`
- **입력 데이터**:
    - 페이징 정보: `size`, `sort`, `direction`.

- **처리 흐름**:
    1. 사용자 인증 정보를 기반으로 `userId`를 확인.
    2. 서비스 레이어에서 사용자별 스레드 목록 조회 및 페이징 처리.
    3. HTTP 200 상태와 함께 스레드 목록 반환.

---

### 3. 스레드의 대화 목록 조회
- **시나리오**:  
  사용자는 특정 스레드 내의 대화 내용을 조회할 수 있습니다.
- **엔드포인트**:  
  `GET /api/v1/chat/threads/{threadId}/chats`
- **입력 데이터**:
    - `threadId`: 조회할 스레드의 고유 식별자.
    - 페이징 정보: `size`, `sort`, `direction`.

- **처리 흐름**:
    1. 사용자 인증 정보를 기반으로 `userId`를 확인.
    2. 서비스 레이어에서 스레드 ID와 사용자 ID에 해당하는 대화 목록 조회.
    3. HTTP 200 상태와 함께 대화 목록 반환.

---

### 4. 스레드 삭제
- **시나리오**:  
  사용자는 특정 스레드를 삭제할 수 있습니다.
- **엔드포인트**:  
  `DELETE /api/v1/chat/threads/{threadId}`
- **입력 데이터**:
    - `threadId`: 삭제할 스레드의 고유 식별자.
- **출력 데이터**:  
  HTTP 204 상태.

- **처리 흐름**:
    1. 사용자 인증 정보를 기반으로 `userId`를 확인.
    2. 서비스 레이어에서 스레드 ID와 사용자 ID를 기반으로 삭제 작업 수행.
    3. 성공적으로 삭제된 경우 HTTP 204 상태 반환.

---

## 유스케이스 다이어그램 (선택)

```plaintext
사용자          시스템
  |               |
  |  질문 요청      |
  +-------------->|
  |               |
  |  스트리밍/일반 응답 |
  <--------------+
  |               |
  |  스레드 목록 조회 요청 |
  +-------------->|
  |               |
  |  스레드 목록 반환   |
  <--------------+
  |               |
  |  대화 목록 조회 요청 |
  +-------------->|
  |               |
  |  대화 목록 반환   |
  <--------------+
  |               |
  |  스레드 삭제 요청   |
  +-------------->|
  |               |
  |  삭제 완료 응답    |
  <--------------+
```

---

## 서비스 레이어 의존성
1. **`ChatbotService.askChatBot`**
    - 입력: `userId`, `ChatbotRequest`, `isStreaming`.
    - 출력: `ChatResponse` 또는 `Flux`.
2. **`ThreadService.getThreadDtos`**
    - 입력: `userId`, `Pageable`.
    - 출력: 스레드 DTO 리스트.
3. **`ChatService.getChatPageDto`**
    - 입력: `userId`, `threadId`, `Pageable`.
    - 출력: 대화 DTO 리스트.
4. **`ThreadService.deleteThread`**
    - 입력: `userId`, `threadId`.
    - 출력: 성공 여부.

---



# 유스케이스: 채팅 피드백 관리

## 목적
사용자가 채팅에 대한 피드백을 남기고, 해당 피드백의 상태를 업데이트하거나, 피드백 목록을 조회할 수 있도록 지원하는 기능을 제공합니다.

## 주요 액터
1. **사용자 (AuthenticatedUser)**: 피드백을 생성, 조회, 업데이트할 권한을 가진 사용자.
2. **관리자**: 피드백 상태를 관리하는 권한을 가진 사용자 (예: 승인, 거절 등).

---

## 유스케이스 설명

### 1. 피드백 생성
- **시나리오**:  
  사용자는 특정 채팅(`chatId`)에 대해 긍정적 또는 부정적 피드백을 남길 수 있습니다.
- **엔드포인트**:  
  `POST /api/v1/chat/feedback/chats/{chatId}`
- **입력 데이터**:
  ```json
  {
    "isPositive": true
  }
  ```
- **출력 데이터**:  
  성공 시 `"success"` 메시지 반환.

- **처리 흐름**:
    1. 사용자 인증 정보를 기반으로 `userId`를 확인.
    2. 서비스 레이어에서 피드백 생성 로직 실행.
    3. 성공적으로 생성된 경우 HTTP 200 상태와 성공 메시지 반환.

---

### 2. 피드백 상태 업데이트
- **시나리오**:  
  관리자는 기존 피드백의 상태를 업데이트할 수 있습니다. (예: `APPROVED`, `REJECTED` 등)
- **엔드포인트**:  
  `PATCH /api/v1/chat/feedback/{feedbackId}`
- **입력 데이터**:
    - `feedbackId`: 업데이트할 피드백의 고유 식별자.
    - `status`: 상태 (`APPROVED`, `REJECTED`, `PENDING`).
- **출력 데이터**:  
  성공 시 `"success"` 메시지 반환.

- **처리 흐름**:
    1. 사용자 인증 정보를 기반으로 요청을 검증.
    2. 서비스 레이어에서 피드백 상태 업데이트 로직 실행.
    3. 성공적으로 업데이트된 경우 HTTP 200 상태와 성공 메시지 반환.

---

### 3. 피드백 목록 조회
- **시나리오**:  
  사용자는 자신이 생성한 피드백 목록을 조회하거나, 특정 조건(긍정적/부정적)에 따라 필터링할 수 있습니다.
- **엔드포인트**:  
  `GET /api/v1/chat/feedback`
- **입력 데이터**:
    - `isPositive` (선택): `true` 또는 `false`.
    - 페이징 정보: `size`, `sort`, `direction`.

- **처리 흐름**:
    1. 사용자 인증 정보를 기반으로 `userId`를 확인.
    2. 서비스 레이어에서 피드백 리스트를 필터링 및 페이징 처리.
    3. HTTP 200 상태와 함께 피드백 목록 반환.

---

## 유스케이스 다이어그램 (선택)

```plaintext
사용자          시스템
  |               |
  |  피드백 생성 요청  |  
  +-------------->|
  |               |  
  |  성공 메시지 반환  |
  <--------------+
  |               |
  |  피드백 상태 업데이트 요청  |  
  +-------------->|
  |               |  
  |  성공 메시지 반환  |
  <--------------+
  |               |
  |  피드백 목록 조회 요청  |  
  +-------------->|
  |               |  
  |  피드백 목록 반환  |
  <--------------+
```

---

## 서비스 레이어 의존성
1. **`ChatFeedbackService.createFeedback`**
    - 입력: `userId`, `chatId`, `isPositive`.
    - 출력: 성공 여부.
2. **`ChatFeedbackService.updateFeedbackStatus`**
    - 입력: `feedbackId`, `status`.
    - 출력: 성공 여부.
3. **`ChatFeedbackService.getFeedbackDtoList`**
    - 입력: `userId`, `isPositive`, `Pageable`.
    - 출력: 피드백 DTO 리스트.

---