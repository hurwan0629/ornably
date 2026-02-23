# BugSandwich - Ornably

![ORNABLY](https://i.pinimg.com/736x/84/a1/56/84a156ca7db6566b341cdff0e7900f1c.jpg)

> React + Spring Boot 기반 REST 아키텍처를 적용한 온라인 쇼핑몰 서비스  
> 사용자 인증, 결제, 관리자 기능 및 협업 중심 설계 경험을 목표로 개발

---

# ⚠ 중요

프로젝트 실행 전 `application.properties` 환경 설정이 필요합니다.

Redis, OAuth2, 결제 API, 이메일 API 등의 민감 정보는 개인 환경에 맞게 설정해야 합니다.

---

## 📖 목차

1. 프로젝트 소개  
2. 기술 스택  
3. 시스템 구조  
4. 주요 기능  
5. 실행 방법  
6. API 및 참고 자료  
7. 트러블 슈팅  
8. 개선 및 향후 계획  

---

## 📝 프로젝트 소개

- **팀명**: 버그 샌드위치  
- **개발 인원**: 4명  
- **프로젝트 개요**:  
  다양한 상품을 판매하고 관리할 수 있는 온라인 쇼핑몰 서비스 구현

### 👥 팀원 역할

- 김유진 — DB 이관 및 Repository 관리  
- 변희인 — Controller 구조 설계 및 구현  
- 정송이 — Spring Security 기반 인증 / 인가 로직 구현  
- 허완 — 프론트엔드 구현 및 사용자 권한 흐름 관리  

---

## 🛠 기술 스택

### Frontend
- React <img src="https://i.namu.wiki/i/pX2jEU81wh0bEgYc4debA8wX5HCOKPHF0K0mPpqY-wK-dJraaUUqR7CwBodmbFrbbFP3hWnSr4RKtun-am04UA.webp" width="20" height="20">

### Backend
- Java 17 <img src="https://education.oracle.com/file/general/p-80-java.png" width="30" height="30">
- Spring Boot 3.2.2 <img src="https://blog.kakaocdn.net/dna/c7hcwQ/btru8Hqw7v1/AAAAAAAAAAAAAAAAAAAAACVdL4zXgcw0hj6e-4Mx1lLyLmDDHKIXQL_anz10XkFt/img.png?credential=yqXZFxpELC7KVnFOS48ylbz2pIh7yKj8&expires=1772290799&allow_ip=&allow_referer=&signature=WdKBkgwIdNWPuaTpPQT9aHT5vtc%3D" width="20" height="20">
- MyBatis <img src="https://downloads.marketplace.jetbrains.com/files/22457/575976/icon/default.png" width="20" height="20">
- Spring Security<img src="https://blog.kakaocdn.net/dna/b5sGlw/btrSI8ZXQDq/AAAAAAAAAAAAAAAAAAAAAI2KXHb6WvryGUHnOLRssm5PM5XMWE_I2eBT6a3A2lAd/img.png?credential=yqXZFxpELC7KVnFOS48ylbz2pIh7yKj8&expires=1772290799&allow_ip=&allow_referer=&signature=tryDANTmxAZztWHeZGPUOWfNZPA%3D" width="20" height="20">

### Database
- MySQL 8.0.43 <img src="https://blog.kakaocdn.net/dna/lH9mc/btsA7L7TJKn/AAAAAAAAAAAAAAAAAAAAAMlBqiP5I9Y70V_iZcIM547LhpXU1-qC1awzk4UWAdGA/img.png?credential=yqXZFxpELC7KVnFOS48ylbz2pIh7yKj8&expires=1772290799&allow_ip=&allow_referer=&signature=EVNbrwSeny92InYVksdr1gcJKmg%3D" width="20" height="20">
- Redis (TTL 기반 인증 처리) <img src="https://img.icons8.com/color/512/redis.png" width="20" height="20">

### 외부 API
- PortOne <img src="https://res.cloudinary.com/postman/image/upload/t_user_profile_300/v1694934472/user/qx9ci1e09v1amyvickpz" width="20" height="20"> (결제) 
- Solapi <img src="https://solapi-content.s3.ap-northeast-2.amazonaws.com/images/2023/10/favicon_60-1.png" width="20" height="20">(문자)
- Brevo <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQzivCFLO2kUG8Sse_uHUd7PMTKfLzg4yinbg&s" width="20" height="20"> (이메일)
- OAuth2 <img src="https://w7.pngwing.com/pngs/172/389/png-transparent-oauth-hd-logo-thumbnail.png" width="20" height="20">(Google, Kakao, Naver)

---

## 🧩 시스템 구조

- React - Spring - MySQL 기반 MVC 구조
- REST API 기반 프론트 / 백엔드 역할 분리
- Redis TTL을 활용한 인증번호 관리
- Brevo HTML 이메일 발송
- React Context 기반 전역 사용자 권한 흐름 제어
- Spring Security Context + DB 기반 사용자 상태 관리
- 로컬 / 소셜 사용자 통합 인증 처리

---

## ✨ 주요 기능

- 회원가입 ~ 탈퇴 사용자 라이프사이클 관리
- OAuth2 기반 소셜 로그인
- 상품 구매 및 주문 처리
- 관리자 대시보드
- 상품 / 회원 / 이벤트 관리 기능
- 이메일 인증 및 SMS 인증

---

## 🚀 실행 방법

1. Redis 서버 실행
2. MySQL 데이터베이스 생성
3. `application.properties` 작성
4. Spring Boot 서버 실행
5. React 개발 서버 실행
6. 관리자 계정은 DB 직접 생성 (Bcrypt 인코딩 필요)
<details>
<summary>properties 포멧</summary>

```properties
server.port=8088

logging.level.org.springframework.web.servlet.view=TRACE

spring.application.name=Ornably

spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=30MB

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379


# Server config
resource.path=[리소스 폴더 절대 경로]
resource.review.prefix=/images/review/
resource.item.prefix=/images/item/
resource.event.prefix=/images/event/
resource.url-prefix=/images
server.origin=http://localhost:8088

# Brevo Email API Key
brevo.api-key=
brevo.sender.email=

# PortOne API key
portone.v2.api-secret=
portone.v2.base-url=https://api.portone.io

# SOLAPI API
SOLAPI_API_KEY=
SOLAPI_API_SECRET=
SOLAPI_FROM_NUMBER=

# MySQL properties
spring.datasource.driver-class-name=
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

# MyBatis properties
mybatis.mapper-locations=classpath*:mapper/**/*.xml
# mybatis.type-aliases-package=bugsandwich.ornably
map-underscore-to-camel-case=true

# [Google] - OAuth2
spring.security.oauth2.client.registration.google.client-id=
spring.security.oauth2.client.registration.google.client-secret=
spring.security.oauth2.client.registration.google.scope=profile,email

# [Kakao] - OAuth2
spring.security.oauth2.client.registration.kakao.client-id=
spring.security.oauth2.client.registration.kakao.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.kakao.redirect-uri=http://localhost:8088/login/oauth2/code/kakao
spring.security.oauth2.client.registration.kakao.client-name=Kakao

spring.security.oauth2.client.provider.kakao.authorization-uri=https://kauth.kakao.com/oauth/authorize
spring.security.oauth2.client.provider.kakao.token-uri=https://kauth.kakao.com/oauth/token
spring.security.oauth2.client.provider.kakao.user-info-uri=https://kapi.kakao.com/v2/user/me
spring.security.oauth2.client.provider.kakao.user-name-attribute=id

# [Naver] - OAUTH2
spring.security.oauth2.client.registration.naver.client-id=
spring.security.oauth2.client.registration.naver.client-secret=
spring.security.oauth2.client.registration.naver.client-name=Naver
spring.security.oauth2.client.registration.naver.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.naver.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.naver.scope=name,email,profile_image

spring.security.oauth2.client.provider.naver.authorization-uri=https://nid.naver.com/oauth2.0/authorize
spring.security.oauth2.client.provider.naver.token-uri=https://nid.naver.com/oauth2.0/token
spring.security.oauth2.client.provider.naver.user-info-uri=https://openapi.naver.com/v1/nid/me
spring.security.oauth2.client.provider.naver.user-name-attribute=response
```

</details>

---

## 📚 API 및 참고 자료

- 리액트 프로젝트  
  https://github.com/hurwan0629/ornably-react

- API 문서  
  https://www.notion.so/api-2f4117a41d4d80e98205d3fd3d191265

- Notion 협업 문서  
  https://www.notion.so/2e3117a41d4d8048b1a6dda54bb58bc1

- Figma 설계  
  https://www.figma.com/board/ImBdRo8Nf8m1LQT7i9ZUeJ

- Google Drive  
  https://drive.google.com/drive/folders/1otMiYu7O6GQic8VTZvWgIQlrMMghpmDJ

---

## 🔧 개선 및 향후 계획

- 로컬 / 소셜 회원 계정 연동 구조 개선
- 전화번호 기반 계정 통합 전략 검토
- ExceptionListener 기반 공통 예외 처리 확장
- 인증 및 권한 로직 구조 리팩토링

---

## 📌 블로그

- 김유진: [유진개발일기](https://bobaejin.tistory.com/)
- 변희인: [태어난 김에 개발 일주](https://blog.naver.com/qusakfdl111)
- 정송이: [꼬부기의 코딩수련소](https://kkobug2.tistory.com/)
- 허완: [태어났더니 개발이 너무 좋은 건에 대하여](https://www.notion.so/272117a41d4d80b1b0faffde0630da77?v=272117a41d4d8049b8f8000cbceb88a3)
