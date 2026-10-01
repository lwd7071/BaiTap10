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

## Nội dung đã triển khai ở cả hai phiên bản

Hai project dùng chung API, quy tắc input, cấu trúc response và giao diện. Điểm khác nhau chủ yếu là thư viện tạo/xác minh JWT: JJWT trong `JWT-JJWT` và Nimbus trong `JWT-Nimbus`.

### 1. Response thống nhất

Mọi API đều trả cùng một cấu trúc JSON. Khi thành công, kết quả nằm trong `data`; danh sách người dùng cũng không trả thành mảng ở root:

```json
{
  "success": true,
  "message": "Lấy hồ sơ thành công",
  "data": { "id": 1, "username": "demo", "role": "USER" },
  "errors": []
}
```

Khi có lỗi, `data` bằng `null`, còn `errors` là danh sách gồm `field` và `message`. Frontend đọc cùng một envelope cho đăng nhập, đăng ký, hồ sơ, danh sách user và lỗi.

### 2. Kiểm tra dữ liệu đầu vào

Validation được khai báo trên DTO request bằng Jakarta Bean Validation, kèm validator riêng để kiểm tra giới hạn BCrypt:

- **Username đăng ký:** trim khoảng trắng ngoài, chuẩn hóa chữ thường trước khi lưu; từ 3–30 ký tự; chỉ nhận chữ ASCII, số, dấu chấm và gạch dưới.
- **Password đăng ký:** từ 8–72 ký tự, có ít nhất một chữ cái và một chữ số; không trim; kiểm tra không quá 72 byte UTF-8 để BCrypt không cắt password.
- **Đăng nhập:** username/password không được rỗng; không áp lại yêu cầu độ mạnh password đăng ký để tài khoản cũ vẫn đăng nhập được.

Lỗi validation được gom về một nơi và trả `400` cùng danh sách lỗi theo field để giao diện hiển thị.

### 3. Xử lý lỗi tập trung

`@RestControllerAdvice` xử lý lỗi validation, JSON không đọc được, username trùng, lỗi xác thực, thiếu quyền, không tìm thấy tài nguyên và exception ngoài dự kiến. Lỗi ngoài dự kiến trả `500` với thông báo an toàn; stack trace và chi tiết database chỉ ghi ở server, không gửi cho client. Error controller cũng chuyển lỗi servlet sang cùng envelope.

Các HTTP status chính: `400` dữ liệu sai, `401` thông tin đăng nhập hoặc token không hợp lệ, `403` không đủ quyền, `404` không tìm thấy tài nguyên, `409` username trùng và `500` lỗi máy chủ.

### 4. Logging và mã đối chiếu request

Hai backend dùng SLF4J/Logback có sẵn của Spring Boot. Log console có timestamp, mức log, HTTP method, path, status và correlation ID. Request thành công ghi `INFO`, request bị từ chối ghi `WARN`, lỗi máy chủ ghi `ERROR` kèm stack trace phía server.

Mỗi response có header `X-Correlation-ID`; có thể dùng mã này để tìm log của request tương ứng. Password, JWT và nội dung body không được ghi vào log. Hiện project ghi log ra console, chưa nối ELK/Grafana Loki.

### 5. API Docs và bảo vệ API

Springdoc sinh tài liệu OpenAPI tự động từ controller. Swagger UI có thể dùng để xem và thử API; endpoint `/users/me` và `/users` được mô tả cần Bearer JWT. Sau khi đăng nhập, dùng **Authorize** trong Swagger UI với token nhận từ `data.token`.

- Swagger UI: `http://localhost:8005/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8005/v3/api-docs`
- Hướng dẫn tiếng Việt: [docs/API.md](docs/API.md)

API đăng ký và đăng nhập là công khai. API hồ sơ và danh sách user cần `Authorization: Bearer <token>`. Mật khẩu được lưu bằng BCrypt; backend chạy stateless.

### 6. Giao diện và kiểm thử

Giao diện hai bản dùng Thymeleaf, jQuery AJAX và CSS responsive. Có đăng nhập/đăng ký, hiển thị lỗi theo field, hồ sơ và danh sách user, trạng thái tải/lỗi/rỗng và đăng xuất.

Automated API/page tests chạy bằng H2 riêng; Playwright kiểm tra luồng giao diện trên cả JJWT và Nimbus bằng H2 E2E. Các bước chạy nằm ở phần [Chạy và kiểm tra](#chạy-và-kiểm-tra).

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
