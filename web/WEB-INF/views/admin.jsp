<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="dao.UserDao" %>
<%@ page import="java.util.List" %>
<%@ page import="jakarta.servlet.http.HttpServletResponse" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    if (!currentUser.isAdmin()) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang quản trị!");
        return;
    }

    UserDao userDao = new UserDao();
    List<User> userList = userDao.getAll();
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>EduDoc - Quản trị hệ thống</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style_admin.css">        
        <style>
            .content-title-bar {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 16px;
            }
            .btn-add-user {
                background-color: #0d6efd;
                color: #fff;
                border: none;
                padding: 8px 16px;
                border-radius: 6px;
                cursor: pointer;
                font-size: 14px;
                display: inline-flex;
                align-items: center;
                gap: 6px;
            }
            .btn-add-user:hover {
                background-color: #0b5ed7;
            }
            .modal-overlay {
                display: none;
                position: fixed;
                top: 0; left: 0; right: 0; bottom: 0;
                background: rgba(0,0,0,0.45);
                z-index: 1000;
                align-items: center;
                justify-content: center;
            }
            .modal-overlay.show {
                display: flex;
            }
            .modal-box {
                background: #fff;
                width: 100%;
                max-width: 420px;
                border-radius: 10px;
                padding: 24px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            }
            .modal-box h3 {
                margin-top: 0;
                margin-bottom: 18px;
            }
            .form-group {
                margin-bottom: 14px;
            }
            .form-group label {
                display: block;
                margin-bottom: 6px;
                font-size: 14px;
                font-weight: 600;
                color: #333;
            }
            .form-group input {
                width: 100%;
                padding: 9px 10px;
                border: 1px solid #ccc;
                border-radius: 6px;
                font-size: 14px;
                box-sizing: border-box;
            }
            .modal-actions {
                display: flex;
                justify-content: flex-end;
                gap: 10px;
                margin-top: 20px;
            }
            .btn-modal-cancel {
                background: #e9ecef;
                border: none;
                padding: 8px 16px;
                border-radius: 6px;
                cursor: pointer;
            }
            .btn-modal-submit {
                background: #0d6efd;
                color: #fff;
                border: none;
                padding: 8px 16px;
                border-radius: 6px;
                cursor: pointer;
            }
            .modal-error {
                color: #dc3545;
                font-size: 13px;
                margin-top: -6px;
                margin-bottom: 10px;
                display: none;
            }
        </style>
    </head>
    <body>
        <header class="header">
            <div class="brand">
                <i class="bi bi-shield-lock-fill"></i>
                <span>EduDoc <span class="badge-admin">ADMIN</span></span>
            </div>
            <div class="user-profile" onclick="toggleUserMenu()">
                <img src="https://github.com/mdo.png" alt="User Avatar">
                <span id="header-admin-name"><%= currentUser.getFullName()%></span>
                <i class="bi bi-chevron-down" style="font-size:12px; color:#666;"></i>
                <div class="user-dropdown" id="userDropdown">
                    <a href="${pageContext.request.contextPath}/logout" class="dropdown-item">
                        <i class="bi bi-box-arrow-right"></i> Đăng xuất
                    </a>
                </div>
            </div>
        </header>

        <div class="main-layout">
            <aside class="sidebar">
                <ul class="menu-list">
                    <li class="menu-item active">
                        <a href="${pageContext.request.contextPath}/admin"><i class="bi bi-people-fill"></i> Người dùng</a>
                    </li>
                </ul>
            </aside>

            <main class="content-container">
                <div class="content-title-bar">
                    <h2 class="content-title" id="userCountTitle">Danh sách người dùng (<%= userList != null ? userList.size() : 0%>)</h2>
                    <button type="button" class="btn-add-user" onclick="openCreateUserModal()">
                        <i class="bi bi-person-plus-fill"></i> Thêm người dùng
                    </button>
                </div>

                <% if (userList != null && !userList.isEmpty()) { %>
                <table class="user-table" id="userTable">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Họ tên</th>
                            <th>Email</th>
                            <th>Vai trò</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="userTableBody">
                        <% for (User u : userList) {%>
                        <tr id="user-row-<%= u.getId()%>">
                            <td><%= u.getId()%></td>
                            <!-- Cột hiển thị Họ Tên -->
                            <td id="name-td-<%= u.getId()%>">
                                <span id="name-text-<%= u.getId()%>"><%= u.getFullName()%></span>
                            </td>
                            <td><%= u.getEmail()%></td>
                            <td>
                                <% if (u.isAdmin()) { %>
                                <span class="role-tag admin">Admin</span>
                                <% } else { %>
                                <span class="role-tag user">User</span>
                                <% } %>
                            </td>
                            <td id="action-td-<%= u.getId()%>">
                                <button type="button" class="btn-edit-user"
                                        onclick="enableEditUser(<%= u.getId()%>)">
                                    <i class="bi bi-pencil-square"></i> Sửa
                                </button>
                                <% if (u.isAdmin()) { %>
                                <span class="locked-note" title="Không thể xóa tài khoản admin"><i class="bi bi-lock-fill"></i> Được bảo vệ</span>
                                <% } else {%>
                                <button type="button" class="btn-delete-user"
                                        onclick="deleteUser(<%= u.getId()%>, '<%= u.getFullName().replace("'", "\\'")%>')">
                                    <i class="bi bi-trash3"></i> Xóa
                                </button>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
                <% } else { %>
                <p class="empty-note" id="emptyNote">Chưa có người dùng nào trong hệ thống.</p>
                <% }%>
            </main>
        </div>

        <!-- Modal Thêm người dùng mới -->
        <div class="modal-overlay" id="createUserModal">
            <div class="modal-box">
                <h3><i class="bi bi-person-plus-fill"></i> Thêm người dùng mới</h3>
                <p class="modal-error" id="createUserError"></p>
                <div class="form-group">
                    <label for="newUserFullName">Họ tên</label>
                    <input type="text" id="newUserFullName" placeholder="Nguyễn Văn A">
                </div>
                <div class="form-group">
                    <label for="newUserEmail">Email</label>
                    <input type="email" id="newUserEmail" placeholder="user@example.com">
                </div>
                <div class="form-group">
                    <label for="newUserPassword">Mật khẩu</label>
                    <input type="password" id="newUserPassword" placeholder="Nhập mật khẩu">
                </div>
                <div class="modal-actions">
                    <button type="button" class="btn-modal-cancel" onclick="closeCreateUserModal()">Hủy</button>
                    <button type="button" class="btn-modal-submit" id="createUserSubmitBtn" onclick="submitCreateUser()">Tạo tài khoản</button>
                </div>
            </div>
        </div>

        <script>
            const contextPath = "${pageContext.request.contextPath}";

            // ============ Thêm người dùng mới ============
            function openCreateUserModal() {
                document.getElementById('newUserFullName').value = '';
                document.getElementById('newUserEmail').value = '';
                document.getElementById('newUserPassword').value = '';
                document.getElementById('createUserError').style.display = 'none';
                document.getElementById('createUserModal').classList.add('show');
            }

            function closeCreateUserModal() {
                document.getElementById('createUserModal').classList.remove('show');
            }

            function escapeHtml(str) {
                const div = document.createElement('div');
                div.innerText = str;
                return div.innerHTML;
            }

            function submitCreateUser() {
                const fullName = document.getElementById('newUserFullName').value.trim();
                const email = document.getElementById('newUserEmail').value.trim();
                const password = document.getElementById('newUserPassword').value;
                const errorEl = document.getElementById('createUserError');
                const submitBtn = document.getElementById('createUserSubmitBtn');

                errorEl.style.display = 'none';

                if (!fullName || !email || !password) {
                    errorEl.textContent = 'Vui lòng nhập đầy đủ Họ tên, Email và Mật khẩu!';
                    errorEl.style.display = 'block';
                    return;
                }

                submitBtn.disabled = true;
                submitBtn.textContent = 'Đang tạo...';

                const params = new URLSearchParams();
                params.append('action', 'createUser');
                params.append('fullName', fullName);
                params.append('email', email);
                params.append('password', password);

                fetch(contextPath + '/AdminServlet', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: params.toString()
                })
                        .then(res => res.json())
                        .then(data => {
                            submitBtn.disabled = false;
                            submitBtn.textContent = 'Tạo tài khoản';

                            if (data.success) {
                                addUserRowToTable(data.user);
                                closeCreateUserModal();
                            } else {
                                errorEl.textContent = data.message || 'Tạo tài khoản thất bại!';
                                errorEl.style.display = 'block';
                            }
                        })
                        .catch(() => {
                            submitBtn.disabled = false;
                            submitBtn.textContent = 'Tạo tài khoản';
                            errorEl.textContent = 'Lỗi kết nối máy chủ!';
                            errorEl.style.display = 'block';
                        });
            }

            // Thêm dòng mới vào bảng người dùng ngay sau khi tạo thành công, không cần tải lại trang
            function addUserRowToTable(user) {
                // Nếu bảng chưa tồn tại (trước đó danh sách rỗng) -> tải lại trang cho đơn giản và chắc chắn đúng
                let tbody = document.getElementById('userTableBody');
                if (!tbody) {
                    window.location.reload();
                    return;
                }

                const safeName = escapeHtml(user.fullName);
                const safeEmail = escapeHtml(user.email);
                const userId = user.id;

                const tr = document.createElement('tr');
                tr.id = 'user-row-' + userId;
                tr.innerHTML = `
                    <td>\${userId}</td>
                    <td id="name-td-\${userId}"><span id="name-text-\${userId}">\${safeName}</span></td>
                    <td>\${safeEmail}</td>
                    <td><span class="role-tag user">User</span></td>
                    <td id="action-td-\${userId}">
                        <button type="button" class="btn-edit-user" onclick="enableEditUser(\${userId})">
                            <i class="bi bi-pencil-square"></i> Sửa
                        </button>
                        <button type="button" class="btn-delete-user" onclick="deleteUser(\${userId}, '\${safeName.replace(/'/g, "\\\\'")}')">
                            <i class="bi bi-trash3"></i> Xóa
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);

                // Cập nhật số đếm ở tiêu đề
                const titleEl = document.getElementById('userCountTitle');
                const currentCount = tbody.querySelectorAll('tr').length;
                titleEl.textContent = 'Danh sách người dùng (' + currentCount + ')';
            }

            // Chuyển dòng sang chế độ chỉnh sửa tên
            function enableEditUser(userId) {
                const nameSpan = document.getElementById('name-text-' + userId);
                const currentName = nameSpan.innerText.trim();

                const nameTd = document.getElementById('name-td-' + userId);
                nameTd.innerHTML = `<input type="text" id="input-name-\${userId}" class="input-edit-name" value="\${currentName}">`;

                const actionTd = document.getElementById('action-td-' + userId);
                actionTd.setAttribute('data-old-action', actionTd.innerHTML); // Lưu giao diện cũ để Hủy
                actionTd.innerHTML = `
                    <button type="button" class="btn-save-user" onclick="saveUser(\${userId})">
                        <i class="bi bi-check-lg"></i> Lưu
                    </button>
                    <button type="button" class="btn-cancel-edit" onclick="cancelEdit(\${userId}, '\${currentName.replace(/'/g, "\\'")}')">
                        Hủy
                    </button>
                `;
            }

            // Hủy chỉnh sửa
            function cancelEdit(userId, oldName) {
                const nameTd = document.getElementById('name-td-' + userId);
                nameTd.innerHTML = `<span id="name-text-\${userId}">\${oldName}</span>`;

                const actionTd = document.getElementById('action-td-' + userId);
                const oldActionHtml = actionTd.getAttribute('data-old-action');
                if (oldActionHtml) {
                    actionTd.innerHTML = oldActionHtml;
                }
            }

            // Lưu tên mới qua AJAX
            function saveUser(userId) {
                const input = document.getElementById('input-name-' + userId);
                const newName = input.value.trim();

                if (!newName) {
                    alert('Họ tên không được để trống!');
                    return;
                }

                const params = new URLSearchParams();
                params.append('action', 'updateUser');
                params.append('id', userId);
                params.append('fullName', newName);

                fetch(contextPath + '/AdminServlet?' + params.toString())
                        .then(res => res.json())
                        .then(data => {
                            if (data.success) {
                                // Cập nhật hiển thị tên mới trên bảng
                                const nameTd = document.getElementById('name-td-' + userId);
                                nameTd.innerHTML = `<span id="name-text-\${userId}">\${newName}</span>`;

                                // Đổi lại các nút bấm Thao tác
                                const actionTd = document.getElementById('action-td-' + userId);
                                const isProtected = actionTd.getAttribute('data-old-action').includes('bi-lock-fill');
                                
                                actionTd.innerHTML = `
                                    <button type="button" class="btn-edit-user" onclick="enableEditUser(\${userId})">
                                        <i class="bi bi-pencil-square"></i> Sửa
                                    </button>
                                    ` + (isProtected ? 
                                        `<span class="locked-note" title="Không thể xóa tài khoản admin"><i class="bi bi-lock-fill"></i> Được bảo vệ</span>` : 
                                        `<button type="button" class="btn-delete-user" onclick="deleteUser(\${userId}, '\${newName.replace(/'/g, "\\'")}')">
                                            <i class="bi bi-trash3"></i> Xóa
                                        </button>`
                                    );

                                alert('Cập nhật tên thành công!');
                            } else {
                                alert(data.message || 'Cập nhật thất bại!');
                            }
                        })
                        .catch(() => {
                            alert('Lỗi kết nối máy chủ!');
                        });
            }

            function deleteUser(userId, userName) {
                if (!confirm('Bạn có chắc muốn xóa tài khoản "' + userName + '"? Hành động này không thể hoàn tác.')) {
                    return;
                }

                fetch(contextPath + '/AdminServlet?action=deleteUser&id=' + userId)
                        .then(function (res) {
                            return res.json();
                        })
                        .then(function (data) {
                            if (data.success) {
                                const row = document.getElementById('user-row-' + userId);
                                if (row)
                                    row.remove();
                            } else {
                                alert(data.message || 'Xóa thất bại, vui lòng thử lại!');
                            }
                        })
                        .catch(function () {
                            alert('Lỗi kết nối, vui lòng thử lại!');
                        });
            }

            function toggleUserMenu() {
                document.getElementById('userDropdown').classList.toggle('show');
            }
            document.addEventListener('click', function (e) {
                const profile = document.querySelector('.user-profile');
                if (profile && !profile.contains(e.target)) {
                    const dd = document.getElementById('userDropdown');
                    if (dd)
                        dd.classList.remove('show');
                }
            });
        </script>
    </body>
</html>