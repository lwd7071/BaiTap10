/* Demo seed for both JWT-JJWT and JWT-Nimbus. Run in SQL Server Management Studio. */
IF DB_ID(N'jwt_springboot3') IS NULL
    CREATE DATABASE jwt_springboot3;
GO

USE jwt_springboot3;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        username NVARCHAR(80) NOT NULL UNIQUE,
        password NVARCHAR(100) NOT NULL,
        user_role NVARCHAR(30) NOT NULL
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'demo')
BEGIN
    INSERT INTO dbo.users (username, password, user_role)
    VALUES (N'demo', N'$2a$10$CqqzuRgZxjle2ZXuyQCq/urgWWG.k6Ka2eIUp2kqVDsr2PaCEdtCS', N'USER');
END;
GO

SELECT id, username, user_role FROM dbo.users;
GO
