# JWT-Nimbus

Spring Boot 3 / Spring Security 6 demo using Nimbus JOSE + JWT 10.10.

# BACKEND

```powershell
mvn spring-boot:run
```

Mở `http://localhost:8005/login`. Trang hồ sơ là `http://localhost:8005/user/profile`. Hai project cùng dùng cổng `8005`; chỉ chạy một bản tại một thời điểm.

Swagger UI: `http://localhost:8005/swagger-ui/index.html`; OpenAPI JSON: `http://localhost:8005/v3/api-docs`. Xem [hướng dẫn API chung](../docs/API.md).

# API response

Mọi endpoint API dùng cùng cấu trúc:

```json
{"success":true,"message":"...","data":{},"errors":[]}
```

Khi có lỗi, `success` là `false`, `data` là `null`, `errors` chứa `field` và `message`. Danh sách `/users` được trả trong `data`.

# Input validation

- Username đăng ký: trim, chuyển thành chữ thường; từ 3–30 ký tự, chỉ gồm chữ ASCII, số, dấu chấm và gạch dưới.
- Password đăng ký: 8–72 ký tự, có ít nhất một chữ cái và một chữ số; không trim và tối đa 72 byte UTF-8 cho BCrypt.
- Đăng nhập kiểm tra username/password không rỗng và tối đa 72 byte cho password, không áp lại chính sách độ mạnh khi đăng ký.

# Chạy test

Automated API tests dùng H2 tạm thời, không cần khởi động SQL Server:

```powershell
mvn test
```

Test bao phủ đăng ký hợp lệ/không hợp lệ, chuẩn hóa username, giới hạn BCrypt, username trùng, đăng nhập sai, token thiếu/sai, API hồ sơ/danh sách với token hợp lệ, trang Thymeleaf cùng tài liệu OpenAPI/Swagger UI.

# Kiểm thử trình duyệt

Từ thư mục gốc repository, cài dependency một lần và chạy Playwright cho cả JJWT và Nimbus:

```powershell
cd e2e
npm ci
npx playwright install chromium
npm test
```

E2E khởi chạy project này bằng `-Pe2e` với database H2 riêng, không dùng SQL Server. Để chỉ chạy Nimbus: `$env:JWT_VARIANT='nimbus'; npm run test:variant`.

# Thử API thủ công

Sau khi chạy ứng dụng và nạp seed database, đăng nhập tài khoản mẫu rồi gọi hai endpoint được bảo vệ:

```powershell
$login = Invoke-RestMethod -Uri 'http://localhost:8005/auth/login' -Method Post -ContentType 'application/json' -Body (@{ username = 'demo'; password = 'JwtDemo123!' } | ConvertTo-Json)
$token = $login.data.token
$profile = Invoke-RestMethod -Uri 'http://localhost:8005/users/me' -Headers @{ Authorization = "Bearer $token" }
$profile.data
$users = Invoke-RestMethod -Uri 'http://localhost:8005/users' -Headers @{ Authorization = "Bearer $token" }
$users.data
```

Đăng ký tài khoản mới qua `POST /auth/register` với JSON `{"username":"newuser","password":"Newpass123!"}`. Gọi `/users/me` không có token để xem response lỗi 401 theo cùng envelope.
