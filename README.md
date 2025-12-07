````markdown
# 🚀 Scalable CMS RESTful API

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-brightgreen?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

> **Mô tả:** Hệ thống Backend API quản lý nội dung (Content Management System) được thiết kế theo kiến trúc Layered Architecture chuẩn mực. Dự án tập trung vào bảo mật (Security), hiệu năng (Performance) và tính toàn vẹn dữ liệu (Data Integrity) sử dụng các kỹ thuật tiên tiến trong Spring Boot 3 và Java 21.

---

## 📑 Mục lục
- [Tính năng nổi bật](#-tính-năng-nổi-bật)
- [Kiến trúc & Công nghệ](#-kiến-trúc--công-nghệ)
- [Cài đặt & Chạy dự án](#-cài-đặt--chạy-dự-án)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Liên hệ](#-liên-hệ)

---

## 🌟 Tính năng nổi bật

### 🔐 1. Advanced Security & Authorization
Hệ thống bảo mật được thiết kế sâu sắc nhằm ngăn chặn các lỗ hổng phổ biến:
- **Stateless Authentication:** Sử dụng **JWT (JSON Web Token)** với `JwtAuthenticationFilter` để xác thực người dùng mà không phụ thuộc vào Session.
- **Fine-grained Authorization:** Phân quyền chi tiết ở cấp độ Method sử dụng **Custom SpEL** (`@PreAuthorize("@resourceSecurityService.isPostAuthor(#id)")`). Điều này đảm bảo User chỉ có quyền sửa/xóa tài nguyên do chính mình tạo ra (Chống lỗ hổng IDOR).
- **Secure Error Handling:** Xử lý tập trung các lỗi 401 (Unauthorized) và 403 (Forbidden) thông qua `AuthenticationEntryPoint` và `AccessDeniedHandler`, trả về JSON chuẩn thay vì trang lỗi mặc định.

### 🏗️ 2. Robust Architecture & Data Integrity
- **Soft Delete (Xóa mềm):** Dữ liệu quan trọng (Post, User) không bị xóa vĩnh viễn khỏi Database mà chỉ được đánh dấu qua trường `deletedAt`. Các truy vấn Repository được Override để tự động lọc dữ liệu đã xóa.
- **JPA Auditing:** Tự động theo dõi lịch sử thay đổi dữ liệu (`createdBy`, `lastModifiedBy`, `createdAt`...) thông qua `AuditorAwareImpl`.
- **Pagination & Sorting:** Tối ưu hóa hiệu năng cho các API danh sách bằng cách phân trang động ngay tại tầng Database.
- **DTO Pattern & MapStruct:** Tách biệt hoàn toàn lớp Entity và lớp API Model, sử dụng MapStruct để mapping object hiệu năng cao (Compile-time).

### 🛠️ 3. DevOps & Quality Assurance
- **Dockerized:** Đóng gói ứng dụng và Database PostgreSQL bằng `docker-compose`.
- **Unit Testing:** Bộ test coverage cao cho các Business Service (`UserService`, `PostService`...) sử dụng JUnit 5 và Mockito.
- **Global Exception Handling:** Bắt và xử lý toàn bộ Runtime Exception về định dạng chuẩn cho Client.

---

## 🏗 Kiến trúc & Công nghệ

| Thành phần | Công nghệ sử dụng |
|------------|-------------------|
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.3 (Web, Data JPA, Security) |
| **Database** | PostgreSQL (Production), H2 (Dev/Test) |
| **Security** | Spring Security 6, JWT, BCrypt |
| **Tools** | Maven, Lombok, MapStruct, Docker |
| **Docs** | OpenAPI 3 (Swagger UI) |

---

## 🚀 Cài đặt & Chạy dự án

### Yêu cầu tiên quyết
- Java 21 JDK
- Maven 3.8+
- Docker & Docker Compose (Khuyên dùng)

### Cách 1: Chạy với Docker (Recommended)
Đây là cách nhanh nhất để dựng cả App và Database PostgreSQL.

```bash
# 1. Clone dự án
git clone [https://github.com/VinhDat267/simple-blog-api.git](https://github.com/VinhDat267/simple-blog-api.git)
cd simple-blog-api

# 2. Build và Run containers
docker-compose up -d --build
````

*Server sẽ khởi động tại: `http://localhost:8080`*

### Cách 2: Chạy thủ công (Local)

Sử dụng Profile `dev` chạy với H2 Database (In-memory) để test nhanh.

```bash
# Chạy ứng dụng bằng Maven Wrapper
./mvnw spring-boot:run
```

-----

## 📚 API Documentation

Hệ thống tích hợp sẵn **Swagger UI** giúp trải nghiệm và test API trực quan.
Sau khi khởi động server, truy cập:

👉 **[http://localhost:8080/swagger-ui.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui.html)**

**Các endpoints chính:**

  - `POST /api/v1/auth/login`: Đăng nhập lấy Token.
  - `POST /api/v1/users/register`: Đăng ký tài khoản mới.
  - `GET /api/v1/posts`: Lấy danh sách bài viết (Hỗ trợ phân trang).
  - `POST /api/v1/posts`: Tạo bài viết (Yêu cầu Login).
  - `DELETE /api/v1/posts/{id}`: Xóa mềm bài viết (Chỉ tác giả mới xóa được).

-----

## 🧪 Testing

Dự án bao gồm bộ Unit Test đảm bảo tính đúng đắn của logic nghiệp vụ.

Để chạy toàn bộ test cases:

```bash
./mvnw test
```

-----

## 📂 Cấu trúc dự án

```
src/main/java/com/example/blogapi
├── config          # Cấu hình (Security, OpenAPI, Auditing)
├── controller      # REST Controllers (API Layer)
├── dto             # Data Transfer Objects (Request/Response)
├── entity          # Database Entities (JPA)
├── exception       # Global Exception Handling
├── mapper          # MapStruct Interfaces
├── repository      # Data Access Layer (JPA Repositories)
├── security        # JWT Auth, Custom Filters, UserDetails
├── service         # Business Logic Layer
└── util            # Utility Classes
```

-----

## 🤝 Liên hệ

**Nguyen Dat Vinh**

  - 📧 Email: datvinh548@gmail.com
  - 🐱 GitHub: [VinhDat267](https://www.google.com/search?q=https://github.com/VinhDat267)
 

-----


```
```
