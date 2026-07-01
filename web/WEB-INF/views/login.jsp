<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng Nhập Hệ Thống</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style_login_register.css">        
    </head>
    <body>

        <div class="container">
            <h2>Đăng Nhập</h2>

            <% if (request.getParameter("msg") != null && "success".equals(request.getParameter("msg"))) { %>
            <div class="success-msg">Đăng ký tài khoản thành công! Mời bạn đăng nhập.</div>
            <% } %>

            <% if (request.getAttribute("error") != null) {%>
            <div class="error-msg"><%= request.getAttribute("error")%></div>
            <% }%>

            <form action="${pageContext.request.contextPath}/login" method="POST">
                <div class="input-group">
                    <label for="login-username">Email</label>
                    <input type="text" id="login-email" name="email" placeholder="Nhập tài khoản..." required>
                </div>
                <div class="input-group">
                    <label for="login-password">Mật khẩu</label>
                    <input type="password" id="login-password" name="password" placeholder="Nhập mật khẩu..." required>
                </div>
                <div class="form-options">
                    <a href="#">Quên mật khẩu?</a>
                </div>
                <button type="submit" class="btn-submit">Đăng Nhập</button>
            </form>

            <div class="switch-page">
                Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
            </div>
        </div>

    </body>
</html>