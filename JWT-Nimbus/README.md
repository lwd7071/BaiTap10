# JWT-Nimbus

Bản chuyển đổi của demo Spring Boot 3 / Spring Security 6, sử dụng Nimbus JOSE + JWT 10.10 để ký và xác thực JWT.

## Chức năng
- Đăng ký và đăng nhập; mật khẩu mới được băm bằng BCrypt.
- Tạo token HS256 có thời hạn cấu hình.
- Bảo vệ `GET /users/me` và `GET /users` bằng `Authorization: Bearer <token>`.
- Giao diện tại `/login.html` và `/profile.html`, gọi API bằng JavaScript.
- Kết nối SQL Server.

## Chuẩn bị database
1. Mở `../JWT-DB-Seed.sql` trong SQL Server Management Studio và chạy script. Script tạo database `jwt_springboot3`, bảng `dbo.users` và tài khoản mẫu.
2. Tài khoản mẫu: **demo / JwtDemo123!**.
3. Mặc định ứng dụng kết nối `localhost:1433` với SQL Login `sa`. Nếu SQL Server của bạn khác, cấu hình biến môi trường trong PowerShell trước khi chạy:

```powershell
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=jwt_springboot3;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME = 'sa'
$env:DB_PASSWORD = 'mật-khẩu-SQL-Server-của-bạn'
$env:JWT_SECRET = 'thay-bằng-một-chuỗi-bí-mật-ngẫu-nhiên-dài-trên-32-byte'
```

Nếu dùng SQL Server Express, thay URL bằng instance/port phù hợp, ví dụ `jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=jwt_springboot3;encrypt=true;trustServerCertificate=true`. `trustServerCertificate=true` phù hợp cho môi trường học tập/local khi dùng chứng thư tự ký; triển khai thật nên cấu hình và xác thực chứng thư TLS.

## Chạy ứng dụng
Từ thư mục `JWT-Nimbus`:

```powershell
mvn spring-boot:run
```

Mở `http://localhost:8005/login.html`. Hai project đều dùng cổng 8005 nên chạy từng bản một.

## Chạy test
Chạy giai đoạn test của Maven từ thư mục project:

```powershell
mvn test
```

Project hiện chưa có automated test cases; lệnh trên chạy Maven test phase. Để kiểm tra luồng ứng dụng, có thể dùng giao diện hoặc chạy các lệnh PowerShell sau khi ứng dụng đã khởi động. Tài khoản `demo` đã được tạo bởi seed script:

```powershell
$login = Invoke-RestMethod -Uri 'http://localhost:8005/auth/login' -Method Post -ContentType 'application/json' -Body (@{ username = 'demo'; password = 'JwtDemo123!' } | ConvertTo-Json)
$token = $login.token
Invoke-RestMethod -Uri 'http://localhost:8005/users/me' -Headers @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri 'http://localhost:8005/users' -Headers @{ Authorization = "Bearer $token" }
```

Để kiểm tra đăng ký, gửi POST `/auth/register` với JSON `{"username":"newuser","password":"Newpass123!"}`, sau đó đăng nhập bằng tài khoản vừa tạo. Gọi `/users/me` không có token để xác nhận API bảo vệ trả về HTTP 401.
