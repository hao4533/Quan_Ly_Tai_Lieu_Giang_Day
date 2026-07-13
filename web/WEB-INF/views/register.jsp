<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng Ký Tài Khoản</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style_login_register.css">
    </head>
    <body>

        <div class="container">
            <h2>Đăng Ký</h2>

            <% if (request.getAttribute("error") != null) { %>
            <div class="error-msg"><%= request.getAttribute("error") %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/register" method="POST" onsubmit="return validateForm()">
                <div class="input-group">
                    <label for="reg-username">Tên tài khoản</label>
                    <input type="text" id="reg-username" name="username"
                           placeholder="Nhập tên tài khoản..."
                           value="${not empty oldUsername ? oldUsername : ''}"
                           required>
                </div>
                <div class="input-group">
                    <label for="reg-email">Địa chỉ Email</label>
                    <input type="email" id="reg-email" name="email"
                           placeholder="Nhập email của bạn..."
                           value="${not empty oldEmail ? oldEmail : ''}"
                           required>
                </div>
                <div class="input-group">
                    <label for="reg-password">Mật khẩu</label>
                    <input type="password" id="reg-password" name="password"
                           placeholder="Tạo mật khẩu..."
                           oninput="checkConfirm()"
                           required>
                </div>
                <div class="input-group">
                    <label for="reg-confirm-password">Xác nhận mật khẩu</label>
                    <input type="password" id="reg-confirm-password" name="confirmPassword"
                           placeholder="Nhập lại mật khẩu..."
                           oninput="checkConfirm()"
                           required>
                    <div id="confirm-msg" style="font-size:13px; margin-top:6px;"></div>
                </div>
                <div class="form-options">
                    <div class="checkbox-group">
                        <input type="checkbox" id="terms" name="terms" required>
                        <label for="terms">Tôi đồng ý với các điều khoản dịch vụ</label>
                    </div>
                </div>
                <button type="submit" class="btn-submit">Đăng Ký</button>
            </form>

            <div class="switch-page">
                Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập tại đây</a>
            </div>
        </div>

        <script>
            function checkConfirm() {
                const pw  = document.getElementById('reg-password').value;
                const cpw = document.getElementById('reg-confirm-password').value;
                const msg = document.getElementById('confirm-msg');
                if (cpw === '') {
                    msg.textContent = '';
                    return;
                }
                if (pw === cpw) {
                    msg.style.color = '#137333';
                    msg.textContent = '✓ Mật khẩu khớp';
                } else {
                    msg.style.color = '#da1212';
                    msg.textContent = '✗ Mật khẩu không khớp';
                }
            }

            function validateForm() {
                const pw = document.getElementById('reg-password').value;
                const cpw = document.getElementById('reg-confirm-password').value;

                if (pw !== cpw) {
                    alert('Mật khẩu xác nhận không khớp!');
                    return false;
                }
                return true;
            }
        </script>

    </body>
</html>