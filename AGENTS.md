# CHAT 백엔드 — AI 협업 컨텍스트

## 서비스 개요
위치 기반 즉석 모임 매칭 + 실시간 채팅 서비스 (취업 포트폴리오용)
참고 모델: nomadtable 앱 (여행자끼리 즉석 모임)

## 스택
- Java 17 / Spring Boot 3.x
- MySQL / Flyway
- WebSocket (STOMP, 단일 서버)
- Spring Security (OAuth2 소셜 로그인 + JWT)

## 아키텍처 규칙
- 계층형: Controller → Service(Interface + Impl) → Repository
- DTO ↔ Entity 분리 (XxxRequest / XxxResponse 따로)
- 전역 예외처리: @RestControllerAdvice + BusinessException(ErrorCode)
- @Transactional은 ServiceImpl에만
- 정적 팩토리: Entity.create() 패턴
- @NoArgsConstructor(access = PROTECTED)
- 와일드카드 import 금지
- 인터페이스 기반 서비스: XxxService 인터페이스 + XxxServiceImpl

## 핵심 기능 범위 (3개만)
1. 회원가입/인증 (소셜 로그인: Google, Kakao, Apple + JWT)
2. 위치 기반 모임 생성 & 조회 (Haversine 반경 검색)
3. 모임별 실시간 채팅 (WebSocket + STOMP)

## Out of scope — 제안하지 말 것
- Redis Pub/Sub (향후 확장 과제로만 언급)
- S3 파일 업로드
- 결제 기능
- 추천 알고리즘
- Spring Batch
- Kafka

## 설계 결정 (확정)
- 자정 만료: @Scheduled 배치로 status EXPIRED 처리 (데이터 일관성)
- 위치 검색: lat/lng 컬럼 + Haversine 공식 네이티브 쿼리 (인덱스 + 직접 구현)
- 채팅: 단순 WebSocket 먼저 → Redis Pub/Sub은 향후 개선 스토리
- 아키텍처: 계층형 (헥사고날은 이 규모에서 과한 추상화)

## 문서 관리
- 설계 결정이 나오면 docs/DEV_LOG.md 업데이트 제안할 것
- AI 제안 vs 내 수정 있으면 docs/AI_LOG.md 한 줄 추가 제안할 것
- 기술 어필 포인트 나오면 docs/TECH_EXPERIENCE.md 추가 제안할 것

## 관련 프로젝트
- 프론트엔드: C:\Users\lee\Desktop\project\FE (React + PWA)
- 백엔드 저장소: CHAT (현재)
