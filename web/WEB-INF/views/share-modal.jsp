<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Permission" %>
<%@ page import="dao.UserDao" %>
<%@ page import="dao.PermissionDao" %>
<%@ page import="java.util.List" %>
<%
    // 🆕 Danh sách người dùng có thể chọn để chia sẻ nội bộ:
    // chỉ role = 'user', loại trừ chính người đang đăng nhập.
    User shareModalCurrentUser = (User) session.getAttribute("user");
    List<User> shareableUserList = null;
    if (shareModalCurrentUser != null) {
        shareableUserList = new UserDao().getShareableUsers(shareModalCurrentUser.getId());
    }

    // 🆕 Danh sách quyền (download/edit/print) lấy động từ CSDL thay vì hard-code
    List<Permission> shareablePermissionList = new PermissionDao().getAll();
%>

<!-- Modal Chia sẻ -->
<div class="modal-overlay" id="shareModal" onclick="closeShareModal(event)">
    <div class="modal-box" onclick="event.stopPropagation()">
        <div class="modal-header">
            <h3 class="modal-title">Chia sẻ tài liệu</h3>
            <button class="modal-close" onclick="closeShareModal()">✕</button>
        </div>
        
        <div class="modal-body" style="padding: 15px 0;">
            <p style="margin-bottom: 15px; color: #444;">
                Đang chia sẻ: <strong id="shareFileName" style="color: #0b57d0;"></strong>
            </p>
            
            <!-- Chọn phương thức chia sẻ -->
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 8px; font-weight: 500;">
                    Phương thức chia sẻ:
                </label>
                <div style="display: flex; gap: 15px;">
                    <label style="cursor: pointer;">
                        <input type="radio" name="shareType" value="internal" checked onchange="toggleShareMethod()"> Người dùng trong hệ thống
                    </label>
                    <label style="cursor: pointer;">
                        <input type="radio" name="shareType" value="email" onchange="toggleShareMethod()"> Qua Email
                    </label>
                </div>
            </div>

            <!-- 🆕 Chọn người dùng trong hệ thống theo Email (Hiển thị mặc định) -->
            <div id="divUserId" style="margin-bottom: 15px;">
                <label for="targetUserId" style="display: block; margin-bottom: 8px; font-weight: 500;">
                    Chọn người dùng nhận chia sẻ (Email):
                </label>
                <select id="targetUserId"
                        style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 6px; outline: none; background: white;">
                    <option value="">-- Chọn người dùng --</option>
                    <%
                        if (shareableUserList != null) {
                            for (User u : shareableUserList) {
                    %>
                    <option value="<%= u.getId() %>"><%= u.getEmail() %><%= (u.getFullName() != null && !u.getFullName().trim().isEmpty()) ? " (" + u.getFullName() + ")" : "" %></option>
                    <%
                            }
                        }
                    %>
                </select>
                <%
                    if (shareableUserList == null || shareableUserList.isEmpty()) {
                %>
                <p style="color: #d93025; font-size: 13px; margin-top: 6px;">Hiện chưa có người dùng nào khác trong hệ thống để chia sẻ.</p>
                <%
                    }
                %>
            </div>

            <!-- Nhập Email (Bị ẩn mặc định) -->
            <div id="divEmail" style="margin-bottom: 15px; display: none;">
                <label for="targetEmail" style="display: block; margin-bottom: 8px; font-weight: 500;">
                    Email người nhận chia sẻ:
                </label>
                <input type="email" id="targetEmail" 
                       placeholder="Nhập địa chỉ Email..." 
                       style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 6px; outline: none;">
            </div>
            
            <!-- 🆕 Danh sách quyền truy cập (load động từ DB: download/edit/print - giống OnlyOffice) -->
            <div id="divPermissions" style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 8px; font-weight: 500;">
                    Cấp quyền truy cập:
                </label>
                <div style="display: flex; gap: 15px; flex-wrap: wrap;">
                    <%
                        if (shareablePermissionList != null) {
                            for (Permission p : shareablePermissionList) {
                                String rawName = p.getPermission() == null ? "" : p.getPermission().trim();
                                String displayLabel;
                                String lowerName = rawName.toLowerCase();
                                if (lowerName.equals("download")) {
                                    displayLabel = "Tải xuống (Download)";
                                } else if (lowerName.equals("edit")) {
                                    displayLabel = "Chỉnh sửa (Edit)";
                                } else if (lowerName.equals("print")) {
                                    displayLabel = "In (Print)";
                                } else {
                                    displayLabel = rawName;
                                }
                    %>
                    <label style="cursor: pointer;">
                        <input type="checkbox" name="permissions" value="<%= p.getId() %>"> <%= displayLabel %>
                    </label>
                    <%
                            }
                        }
                    %>
                </div>
            </div>
            
            <div id="shareMessage" style="font-size: 14px; margin-top: 10px;"></div>
        </div>
        
        <div class="modal-footer" style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 15px;">
            <button onclick="closeShareModal()" style="padding: 8px 16px; border: 1px solid #ccc; background: white; border-radius: 6px; cursor: pointer;">
                Hủy
            </button>
            <button onclick="submitShare()" id="btnSubmitShare" style="padding: 8px 16px; border: none; background: #0b57d0; color: white; border-radius: 6px; cursor: pointer;">
                Xác nhận chia sẻ
            </button>
        </div>
    </div>
</div>

<script>
    let currentShareDocId = null;

    // Đổi hiển thị input dựa theo phương thức chọn
    function toggleShareMethod() {
        const shareType = document.querySelector('input[name="shareType"]:checked').value;
        if (shareType === 'internal') {
            document.getElementById('divUserId').style.display = 'block';
            document.getElementById('divEmail').style.display = 'none';
            document.getElementById('divPermissions').style.display = 'block'; 
        } else {
            document.getElementById('divUserId').style.display = 'none';
            document.getElementById('divEmail').style.display = 'block';
            document.getElementById('divPermissions').style.display = 'none'; 
        }
    }

    // Mở modal
    function openShareModal(docId, docName) {
        currentShareDocId = docId;
        document.getElementById('shareFileName').innerText = docName;
        document.getElementById('targetUserId').value = '';
        document.getElementById('targetEmail').value = '';
        document.getElementById('shareMessage').innerText = '';
        
        // Reset về phương thức chia sẻ qua người dùng trong hệ thống
        document.querySelector('input[name="shareType"][value="internal"]').checked = true;
        toggleShareMethod();
        
        // Bỏ chọn tất cả checkbox quyền
        document.querySelectorAll('input[name="permissions"]').forEach(cb => cb.checked = false);
        
        document.getElementById('shareModal').classList.add('active'); 
    }

    // Đóng modal
    function closeShareModal(e) {
        const shareModal = document.getElementById('shareModal');
        if (!e || e.target === shareModal) {
            shareModal.classList.remove('active');
            currentShareDocId = null;
        }
    }

    // Hàm tiện ích hiển thị lỗi
    function showErrorMessage(msgDiv, btnSubmit, message) {
        msgDiv.style.color = 'red';
        msgDiv.innerText = message;
        btnSubmit.disabled = false;
        btnSubmit.innerText = 'Xác nhận chia sẻ';
    }

    // Gửi yêu cầu chia sẻ lên Server
    function submitShare() {
        const shareType = document.querySelector('input[name="shareType"]:checked').value;
        const msgDiv = document.getElementById('shareMessage');
        const btnSubmit = document.getElementById('btnSubmitShare');
        
        // Cập nhật trạng thái UI
        btnSubmit.disabled = true;
        btnSubmit.innerText = 'Đang xử lý...';
        msgDiv.innerText = '';

        const formData = new URLSearchParams();
        let fetchEndpoint = '';
        
        // KIỂM TRA PHƯƠNG THỨC CHIA SẺ
        if (shareType === 'internal') {
            const targetUserId = document.getElementById('targetUserId').value.trim();
            const checkedPermissions = document.querySelectorAll('input[name="permissions"]:checked');
            
            if (!targetUserId) {
                return showErrorMessage(msgDiv, btnSubmit, 'Vui lòng chọn người dùng nhận chia sẻ!');
            }
            if (checkedPermissions.length === 0) {
                return showErrorMessage(msgDiv, btnSubmit, 'Vui lòng chọn ít nhất một quyền truy cập!');
            }

            formData.append('documentId', currentShareDocId);
            formData.append('targetUserId', targetUserId);
            checkedPermissions.forEach(cb => {
                formData.append('permissions', cb.value); 
            });
            fetchEndpoint = '${pageContext.request.contextPath}/share-internal'; 
            
        } else if (shareType === 'email') {
            const targetEmail = document.getElementById('targetEmail').value.trim();
            if (!targetEmail) {
                return showErrorMessage(msgDiv, btnSubmit, 'Vui lòng nhập Email người nhận!');
            }
            
            // ĐÃ SỬA: Cập nhật tên tham số và URL cho khớp với ShareServlet
            formData.append('documentId', currentShareDocId); 
            formData.append('recipientEmail', targetEmail); 
            fetchEndpoint = '${pageContext.request.contextPath}/share'; 
        }

        // Gọi API gửi dữ liệu
        fetch(fetchEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData.toString()
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success' || data.success === true) {
                msgDiv.style.color = 'green';
                msgDiv.innerText = data.message || 'Chia sẻ tài liệu thành công!';
                setTimeout(() => {
                    closeShareModal();
                }, 1500);
            } else {
                showErrorMessage(msgDiv, btnSubmit, data.message || 'Lỗi khi chia sẻ tài liệu!');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showErrorMessage(msgDiv, btnSubmit, 'Lỗi kết nối máy chủ!');
        });
    }
</script>
