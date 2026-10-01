# Tài liệu API JWT

Tài liệu tương tác được sinh tự động từ controller và annotation OpenAPI trong từng project.

## Mở Swagger UI

Khởi động một trong hai project tại cổng `8005`, sau đó mở:

- JJWT: <http://localhost:8005/swagger-ui/index.html>
- Nimbus: <http://localhost:8005/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8005/v3/api-docs>
- OpenAPI YAML: <http://localhost:8005/v3/api-docs.yaml>

Hai project chạy cùng cổng nên chỉ khởi động một project tại một thời điểm. Chọn thư mục `JWT-JJWT` hoặc `JWT-Nimbus`, rồi chạy `mvn spring-boot:run`.

## Xác thực trong Swagger UI

1. Gọi `POST /auth/login` với username và password hợp lệ.
2. Sao chép giá trị `data.token` trong response.
3. Nhấn **Authorize**, dán token vào ô Bearer JWT rồi xác nhận.
4. Gọi `GET /users/me` hoặc `GET /users`.

Không cần tự thêm tiền tố `Bearer` trong ô Authorize; Swagger UI dùng scheme HTTP Bearer đã khai báo trong OpenAPI.

## API hiện có

| Method | Path | Xác thực | Mô tả |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Không | Tạo tài khoản với role `USER` |
| `POST` | `/auth/login` | Không | Đăng nhập và nhận JWT |
| `GET` | `/users/me` | Bearer JWT | Đọc hồ sơ của token hiện tại |
| `GET` | `/users` | Bearer JWT | Đọc danh sách người dùng |

### Đăng ký — `POST /auth/register`

Request:

```json
{
  "username": "student_1",
  "password": "StrongPass123"
}
```

Username được trim và chuẩn hóa thành chữ thường; dài 3–30 ký tự, chỉ gồm chữ ASCII, số, dấu chấm và gạch dưới. Password dài 8–72 ký tự, phải chứa chữ và số, không được trim và phải nằm trong giới hạn 72 byte BCrypt hỗ trợ.

### Đăng nhập — `POST /auth/login`

Request:

```json
{
  "username": "demo",
  "password": "JwtDemo123!"
}
```

Response thành công trả JWT trong `data.token`, kiểu token trong `data.tokenType` và thời điểm hết hạn trong `data.expiresAt` (Unix time milliseconds).

### Response envelope

Thành công:

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

Lỗi:

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": null,
  "errors": [
    { "field": "username", "message": "Username không hợp lệ" }
  ]
}
```

HTTP status chính: `400` input không hợp lệ, `401` đăng nhập/token không hợp lệ, `403` không đủ quyền, `409` username đã tồn tại.

Lỗi không dự kiến trả `500` cùng thông báo chung; stack trace và chi tiết cơ sở dữ liệu chỉ nằm trong log máy chủ. Mỗi response có header `X-Correlation-ID`; dùng ID này để tìm các dòng log liên quan. Có thể gửi header này trong request bằng mã gồm 1–64 ký tự chữ/số, dấu chấm, gạch dưới hoặc gạch ngang; ID không hợp lệ sẽ được thay bằng UUID do server tạo.

Log console theo định dạng Logback của Spring Boot gồm timestamp, mức log, correlation ID, method, path và logger. Request thành công được ghi `INFO`, request lỗi `4xx` và token không hợp lệ ở `WARN`, lỗi máy chủ `5xx` ở `ERROR`. Không ghi thông tin xác thực hay nội dung body. ELK/Loki chưa được cấu hình trong project này.

## Dùng JWT trực tiếp

Với API cần xác thực, gửi header:

```http
Authorization: Bearer <token>
```

Tài khoản seed cho môi trường local: `demo / JwtDemo123!` (sau khi chạy `JWT-DB-Seed.sql`).
