# 🛒 Shopery Backend

Backend service for **Shopery**, a full-featured e-commerce platform built with **Spring Boot**.

---

## 🚀 Tech Stack

- Java 21
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Redis
- WebSocket (STOMP)
- AWS S3
- Spring Mail + Thymeleaf
- Swagger (OpenAPI)
- Resilience4j (Rate Limiting)
- Claude AI API

---

## ✨ Features

### 🔐 Authentication & Security
- User & Admin login
- Registration & email verification
- JWT-based authentication
- Password reset / forgot password
- Refresh token support
- Rate limiting on auth endpoints

---

### 👤 User Features
- Profile management
- Address management
- Shop creation
- Product CRUD
- Cart & Wishlist
- Orders
- Blogs
- Support tickets
- Profile photo upload

---

### 🌍 Public APIs
- Browse products
- Browse shops
- Browse blogs
- Product filtering (price, category, keyword)
- Top discounts

---

### 🛠️ Admin Features
- Manage users
- Manage shops
- Handle support tickets
- Approve/reject workflows

---

### 💬 Chat System
- Real-time messaging (WebSocket)
- AI Chat (Claude API)
- Chat history

---

### ☁️ Infrastructure Features
- AWS S3 file storage
- Email notifications
- Async processing
- Scheduling
- Auditing

---

## 📁 Project Structure

```
src/main/java/az/shopery
├── configuration
├── controller
├── handler
├── listener
├── mapper
├── model
│   ├── dto
│   ├── entity
│   └── event
├── repository
├── service
│   └── impl
└── utils

src/main/resources
├── application.yaml
├── application-local.yaml
├── db/changelog
└── templates
```

---

## 🔗 API Routes

```
/api/v1/auth
/api/v1/users/me
/api/v1/products
/api/v1/shops
/api/v1/blogs
/api/v1/dropdowns
/api/v1/chat
/api/v1/admins
/ws
```

---

## ⚙️ Environment Variables

```env
PORT=8080

DB_URL=jdbc:postgresql://localhost:5432/shopery_db
DB_USERNAME=shopery_user
DB_PASSWORD=shopery_pass

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET_KEY=

FRONTEND_BASE_URL=

CLAUDE_API_KEY=

AWS_REGION=
AWS_S3_BUCKET_NAME=
```

---

## 🐳 Run with Docker

Start services:

```bash
docker compose up -d
```

---

## ▶️ Run Application

```bash
./gradlew bootRun
```

---

## 📖 Swagger

```
http://localhost:8080/swagger-ui.html
```

---

## 🗄️ Database

- Managed via Liquibase
- Versioned migrations (`v-01` → `v-15`)
- `ddl-auto: none`

---

## 🧪 WebSocket

```
ws://localhost:8080/ws
```

Send messages via:

```
/app/chat.send
```

---

## 📦 Build Docker Image

```bash
docker build -t shopery-backend .
docker run -p 8080:8080 --env-file .env shopery-backend
```

---

## ⚡ Notes

- Uses stateless JWT authentication
- Redis used for caching/session support
- AI chat requires **PREMIUM** subscription
- Email templates are inside `/resources/templates`
