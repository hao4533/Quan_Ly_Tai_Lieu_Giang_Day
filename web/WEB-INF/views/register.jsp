<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng Ký Tài Khoản</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style_login_register.css">
        <style>
            .error-msg {
                color: #da1212;
                background-color: #fce8e6;
                padding: 10px;
                border-radius: 4px;
                font-size: 14px;
                margin-bottom: 15px;
                text-align: center;
            }
            /* Khung kiểm tra điều kiện mật khẩu */
            .password-rules {
                margin-top: 8px;
                padding: 10px 12px;
                background: #f8f9fa;
                border-radius: 6px;
                border: 1px solid #dee2e6;
                font-size: 13px;
            }
            .rule {
                display: flex;
                align-items: center;
                gap: 8px;
                margin: 4px 0;
                color: #888;
                transition: color 0.2s;
            }
            .rule.ok {
                color: #137333;
            }
            .rule.fail {
                color: #da1212;
            }
            .rule-icon::before {
                content: '○';
            }
            .rule.ok .rule-icon::before {
                content: '✓';
            }
            .rule.fail .rule-icon::before {
                content: '✗';
            }
        </style>
    </head>
    <body>

        <div class="container">
            <h2>Đăng Ký</h2>

            <%-- Hiển thị thông báo lỗi từ server nếu có --%>
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
                           oninput="checkPasswordRules(this.value)"
                           required>
                    <%-- Hiển thị các điều kiện mật khẩu real-time --%>
                    <div class="password-rules" id="password-rules">
                        <div class="rule" id="rule-length">
                            <span class="rule-icon"></span> Ít nhất 6 ký tự
                        </div>
                        <div class="rule" id="rule-upper">
                            <span class="rule-icon"></span> Có ít nhất 1 chữ cái viết hoa (A-Z)
                        </div>
                        <div class="rule" id="rule-letter">
                            <span class="rule-icon"></span> Có chữ cái (a-z hoặc A-Z)
                        </div>
                        <div class="rule" id="rule-digit">
                            <span class="rule-icon"></span> Có ít nhất 1 chữ số (0-9)
                        </div>
                    </div>
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
            // Kiểm tra từng điều kiện mật khẩu real-time khi người dùng gõ
            function checkPasswordRules(password) {
                setRule('rule-length', password.length >= 6);
                setRule('rule-upper', /[A-Z]/.test(password));
                setRule('rule-letter', /[a-zA-Z]/.test(password));
                setRule('rule-digit', /[0-9]/.test(password));
                checkConfirm(); // cập nhật lại xác nhận mật khẩu
            }

            function setRule(id, passed) {
                const el = document.getElementById(id);
                el.classList.remove('ok', 'fail');
                if (passed) {
                    el.classList.add('ok');
                } else {
                    el.classList.add('fail');
                }
            }

            // Kiểm tra mật khẩu xác nhận có khớp không
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

            // Chặn submit nếu điều kiện chưa thỏa mãn (bảo vệ lớp frontend)
            function validateForm() {
                const pw = document.getElementById('reg-password').value;
                const cpw = document.getElementById('reg-confirm-password').value;

                if (pw.length < 6) {
                    alert('Mật khẩu phải có ít nhất 6 ký tự!');
                    return false;
                }
                if (!/[A-Z]/.test(pw)) {
                    alert('Mật khẩu phải có ít nhất 1 chữ cái viết hoa!');
                    return false;
                }
                if (!/[a-zA-Z]/.test(pw) || !/[0-9]/.test(pw)) {
                    alert('Mật khẩu phải bao gồm cả chữ cái và chữ số!');
                    return false;
                }
                if (pw !== cpw) {
                    alert('Mật khẩu xác nhận không khớp!');
                    return false;
                }
                return true; // cho phép submit
            }
        </script>

    </body>
</html>
F