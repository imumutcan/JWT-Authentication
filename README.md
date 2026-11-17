# JWT Authentication API

Spring Boot ve Spring Security kullanılarak geliştirilmiş, **Access Token (JWT)** ve **Refresh Token Rotation** mekanizmalarına sahip RESTful kimlik doğrulama ve yetkilendirme servisi.

## Özellikler

- **Kullanıcı Kaydı ve Girişi:** BCrypt ile güvenli parola hash'leme ve doğrulama.
- **Stateless JWT Kimlik Doğrulama:** `HMAC-SHA256` ile imzalanmış, rol (`roles`) bilgilerini içeren Access Token üretimi.
- **Refresh Token Rotation:** Her yenileme (`/refresh`) işleminde eski refresh token'ın geçersiz kılınıp yeni bir refresh token üretilmesi.
- **Otomatik Temizleme (Scheduled Purge):** Süresi dolmuş refresh token'ların veritabanından periyodik olarak silinmesi.
- **Güvenli Çıkış (Logout):** Aktif refresh token'ın veritabanından silinerek oturumun sonlandırılması.
- **Merkezi Hata Yönetimi:** Doğrulama (Validation), kimlik doğrulama ve yetkilendirme hataları için standart JSON hata yanıtları (`GlobalExceptionHandler`).

## Teknolojiler

- **Java 17**
- **Spring Boot** (WebMVC, Data JPA, Security, Validation)
- **JJWT (0.12.3)** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **MySQL** (`mysql-connector-j`)
- **Lombok**
- **Maven**

## Proje Yapısı

```text
src/main/java/com/umut/project/database/jwtauthapi
├── config/
│   ├── AppConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── UserController.java
├── dto/
│   └── AuthDto.java
├── entity/
│   ├── RefreshToken.java
│   ├── Role.java
│   └── User.java
├── exception/
│   ├── BaseException.java
│   ├── EmailAlreadyExistsException.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidTokenException.java
│   ├── ResourceAlreadyExistsException.java
│   ├── ResourceNotFoundException.java
│   ├── UserAlreadyExistsException.java
│   └── UserNotFoundException.java
├── filter/
│   └── JwtAuthenticationFilter.java
├── repository/
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
├── service/
│   ├── AuthService.java
│   ├── CustomUserDetailsService.java
│   ├── JwtService.java
│   └── RefreshTokenService.java
└── JwtAuthApiApplication.java
```

## Kurulum ve Çalıştırma

### 1. Ortam Değişkenleri

Uygulama yapılandırması `src/main/resources/application.properties` üzerinden ortam değişkenleriyle yönetilir:

| Değişken | Açıklama | Varsayılan Değer |
|---|---|---|
| `DB_URL` | MySQL JDBC bağlantı adresi | `jdbc:mysql://localhost:3306/jwt_auth_db?...` |
| `DB_USERNAME` | Veritabanı kullanıcı adı | `root` |
| `DB_PASSWORD` | Veritabanı parolası | *(boş)* |
| `JWT_SECRET_KEY` | Base64 formatında 256-bit gizli anahtar | *(varsayılan geliştirme anahtarı)* |
| `JWT_ACCESS_TOKEN_EXPIRATION_MS` | Access token geçerlilik süresi (ms) | `900000` (15 dakika) |
| `JWT_REFRESH_TOKEN_EXPIRATION_MS` | Refresh token geçerlilik süresi (ms) | `604800000` (7 gün) |

### 2. Veritabanını Hazırlama

MySQL üzerinde `jwt_auth_db` veritabanını oluşturun:

```sql
CREATE DATABASE jwt_auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Uygulamayı Başlatma

```bash
./mvnw clean spring-boot:run
```

Uygulama varsayılan olarak `http://localhost:8080` portunda çalışır.

## API Endpoint'leri

| Metot | Endpoint | Yetki | Açıklama |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Açık (`permitAll`) | Yeni kullanıcı kaydı oluşturur |
| `POST` | `/api/v1/auth/login` | Açık (`permitAll`) | Giriş yapar, Access ve Refresh Token döner |
| `POST` | `/api/v1/auth/refresh` | Açık (`permitAll`) | Refresh token'ı yeniler ve yeni Access Token döner |
| `POST` | `/api/v1/auth/logout` | `Bearer Token` | Refresh token'ı silerek oturumu sonlandırır |
| `GET` | `/api/v1/auth/info` | `Bearer Token` | Giriş yapmış kullanıcının bilgilerini döner |

### Örnek İstekler

#### 1. Kayıt Ol (`POST /api/v1/auth/register`)

**İstek:**
```json
{
  "username": "umutcan123",
  "password": "StrongPassword123!",
  "email": "umutcan@example.com"
}
```

**Yanıt (`201 Created`):**
```json
{
  "username": "umutcan123",
  "id": 1
}
```

#### 2. Giriş Yap (`POST /api/v1/auth/login`)

**İstek:**
```json
{
  "username": "umutcan123",
  "password": "StrongPassword123!"
}
```

**Yanıt (`200 OK`):**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "umutcan123",
  "message": "Login is successful!"
}
```

#### 3. Token Yenile (`POST /api/v1/auth/refresh`)

**İstek:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Yanıt (`200 OK`):**
```json
{
  "refreshToken": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

#### 4. Kullanıcı Bilgisi (`GET /api/v1/auth/info`)

**Header:**
```http
Authorization: Bearer <accessToken>
```

**Yanıt (`200 OK`):**
```json
{
  "id": 1,
  "username": "umutcan123",
  "email": "umutcan@example.com",
  "roles": [
    "ROLE_USER"
  ]
}
```

#### 5. Çıkış Yap (`POST /api/v1/auth/logout`)

**Header:**
```http
Authorization: Bearer <accessToken>
```

**İstek:**
```json
{
  "refreshToken": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
}
```

**Yanıt:** `204 No Content`
