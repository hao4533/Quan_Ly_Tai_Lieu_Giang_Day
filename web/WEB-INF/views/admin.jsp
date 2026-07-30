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
                <h2 class="content-title">Danh sách người dùng (<%= userList != null ? userList.size() : 0%>)</h2>

                <% if (userList != null && !userList.isEmpty()) { %>
                <table class="user-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Họ tên</th>
                            <th>Email</th>
                            <th>Vai trò</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
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
                <p class="empty-note">Chưa có người dùng nào trong hệ thống.</p>
                <% }%>
            </main>
        </div>

        <script>
            const contextPath = "${pageContext.request.contextPath}";

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