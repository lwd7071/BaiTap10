# Bài tập JWT — Spring Boot 3

Hai project triển khai cùng một demo đăng ký, đăng nhập và xác thực API bằng JWT. Phần nghiệp vụ và API được giữ giống nhau để dễ so sánh; khác biệt chính là thư viện ký/xác thực token.

| Project | Thư viện JWT |
| --- | --- |
| `JWT-JJWT` | JJWT 0.12.6 |
| `JWT-Nimbus` | Nimbus JOSE + JWT 10.10 |

## Chức năng chung

- Đăng ký tài khoản, lưu mật khẩu dưới dạng BCrypt; tài khoản mới có quyền `USER`.
- Đăng nhập bằng username/password, cấp JWT HS256 có thời hạn cấu hình.
- Bảo vệ `GET /users/me` và `GET /users` bằng header `Authorization: Bearer <token>`.
- Giao diện Thymeleaf tại `/login` và `/user/profile`; jQuery AJAX gọi API và giữ Bearer token trong `localStorage`.
- Dùng SQL Server và bảng `dbo.users`. Chạy [JWT-DB-Seed.sql](JWT-DB-Seed.sql) trong SQL Server Management Studio để tạo database `jwt_springboot3` cùng tài khoản mẫu **demo / JwtDemo123!**.

## API response thống nhất

Mọi endpoint API trả cùng envelope; dữ liệu thành công nằm trong `data`, kể cả danh sách người dùng:

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "<jwt>",
    "tokenType": "Bearer",
    "expiresAt": 1790000000000
  },
  "errors": []
}
```

Khi lỗi, `success` là `false`, `data` là `null`, còn `errors` chứa `field` và `message`. Status chính: `400` input không hợp lệ, `401` sai thông tin đăng nhập hoặc token, `403` không đủ quyền, `409` username bị trùng.

Exception trong API được ánh xạ tập trung về envelope này; lỗi máy chủ ngoài dự kiến trả thông báo chung, không gửi stack trace hay nội dung database về client. Mỗi request được log với timestamp, mức độ, HTTP method, path, status và correlation ID. ID được trả trong header `X-Correlation-ID` (client có thể gửi ID hợp lệ để tiện tra cứu). Không ghi password, token JWT hay nội dung request vào log. Log console dùng Logback có sẵn của Spring Boot; chưa cấu hình dịch vụ thu thập log ngoài.

## API Docs

Cả hai project sinh OpenAPI tự động và cung cấp Swagger UI để xem/thử endpoint. Khi ứng dụng đang chạy, mở `http://localhost:8005/swagger-ui/index.html`; hướng dẫn gọi API và xác thực Bearer nằm trong [docs/API.md](docs/API.md). Tệp OpenAPI JSON được cung cấp tại `/v3/api-docs`.

## Quy tắc input

- Username đăng ký được trim, chuyển thành chữ thường, dài 3–30 ký tự; chỉ chấp nhận chữ ASCII, số, dấu chấm và gạch dưới.
- Password đăng ký dài 8–72 ký tự, có ít nhất một chữ và một số, không bị trim và không vượt 72 byte UTF-8 để tránh BCrypt cắt chuỗi.
- Đăng nhập yêu cầu username/password không rỗng; không áp lại độ mạnh password của bước đăng ký.

## Chạy và kiểm tra

Trước tiên chạy seed SQL. Sau đó, trong PowerShell, vào thư mục của phiên bản muốn chạy và khởi động ứng dụng:

```powershell
cd JWT-JJWT
mvn spring-boot:run
```

Đổi `JWT-JJWT` thành `JWT-Nimbus` để chạy phiên bản Nimbus. Hai ứng dụng cùng dùng cổng `8005`, nên chạy từng phiên bản một. Nếu SQL Server không dùng cấu hình mặc định, xem README riêng của project để đặt `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` và `JWT_SECRET`.

Chạy automated API và trang-template tests trong thư mục project:

```powershell
mvn test
```

Test dùng H2 tạm thời, không cần SQL Server. Các test bao gồm validation và chuẩn hóa username, username trùng, đăng nhập sai, response lỗi 401/403, gọi API được bảo vệ bằng token hợp lệ, và render các trang cùng tài nguyên UI.

### Kiểm thử trên trình duyệt

Cần cài Node.js và Chromium cho Playwright. Từ thư mục gốc repo:

```powershell
cd e2e
npm ci
npx playwright install chromium
npm test
```

Playwright chạy từng project tuần tự trên cổng `8005`, khởi động với profile Maven `e2e` và H2 riêng. Không kết nối hoặc thay đổi SQL Server. Test bao phủ đăng ký, lỗi đăng nhập/username trùng, Bearer token, hồ sơ, danh sách người dùng, đăng xuất, token thiếu/sai, màn hình nhỏ và reduced motion.

## Hướng dẫn riêng từng bản

- [JWT-JJWT — cấu hình SQL Server và thử API](JWT-JJWT/README.md)
- [JWT-Nimbus — cấu hình SQL Server và thử API](JWT-Nimbus/README.md)
