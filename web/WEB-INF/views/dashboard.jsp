<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Folder" %>
<%@ page import="model.Document" %>
<%@ page import="dao.FolderDao" %>
<%@ page import="dao.DocumentDao" %>
<%@ page import="java.util.List" %>
<%
    // 1. Kiểm tra session đăng nhập bảo vệ trang chủ công khai
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String fullName = currentUser.getFullName();
    int userId = currentUser.getId();

    // 2. Lấy danh sách Thư mục động của User từ Database
    FolderDao folderDao = new FolderDao();
    List<Folder> folderList = folderDao.getRootFolderByUserId(userId);

    // 3. Lấy danh sách Tập tin động của User từ Database
    DocumentDao documentDao = new DocumentDao();
    List<Document> documentList = documentDao.getRootDocumentsByUserId(userId);
%>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>EduDoc</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style_dashboard.css">        

        <style>
            /* ================= CSS THUẦN CHO MODAL UPLOAD ================= */
            .modal-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.4); /* Làm tối nền phía sau */
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9999;
                opacity: 0;
                pointer-events: none;
                transition: opacity 0.2s ease;
            }
            /* Khi active class được thêm vào qua JS */
            .modal-overlay.active {
                opacity: 1;
                pointer-events: auto;
            }
            .modal-box {
                background-color: #ffffff;
                width: 100%;
                max-width: 450px;
                padding: 24px;
                border-radius: 16px;
                box-shadow: 0 4px 24px rgba(0,0,0,0.15);
                transform: scale(0.9);
                transition: transform 0.2s ease;
            }
            .modal-overlay.active .modal-box {
                transform: scale(1);
            }
            .modal-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
            }
            .modal-title {
                font-size: 18px;
                font-weight: 500;
                color: #1f1f1f;
            }
            .modal-close {
                background: none;
                border: none;
                font-size: 20px;
                cursor: pointer;
                color: #5f6368;
                padding: 4px;
            }
            .modal-close:hover {
                color: #1f1f1f;
            }

            /* Khu vực chọn tính năng bên trong Modal */
            .upload-options {
                display: flex;
                flex-direction: column;
                gap: 12px;
            }
            .upload-option-btn {
                display: flex;
                align-items: center;
                gap: 16px;
                width: 100%;
                padding: 14px 16px;
                background-color: #f0f4f9;
                border: 1px solid transparent;
                border-radius: 10px;
                font-size: 14px;
                font-weight: 500;
                color: #444746;
                cursor: pointer;
                text-align: left;
                transition: all 0.2s;
            }
            .upload-option-btn:hover {
                background-color: #e1e7ef;
                border-color: #a8c7fa;
                color: #0b57d0;
            }
            .upload-option-btn i {
                font-size: 20px;
            }

            /* ===== USER DROPDOWN LOGOUT ===== */
            .user-profile {
                display: flex;
                align-items: center;
                gap: 8px;
            }
            .user-profile img {
                width: 34px;
                height: 34px;
                border-radius: 50%;
                object-fit: cover;
            }
            .user-name {
                font-size: 14px;
                font-weight: 500;
                color: #1f1f1f;
                max-width: 130px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
            .user-dropdown {
                display: none;
                position: absolute;
                top: calc(100% + 10px);
                right: 0;
                background: #fff;
                border: 1px solid #e0e0e0;
                border-radius: 12px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.12);
                min-width: 220px;
                padding: 12px;
                z-index: 9999;
            }
            .user-dropdown.show {
                display: block;
            }
            .dropdown-info {
                display: flex;
                align-items: center;
                gap: 10px;
                padding: 4px 0 8px;
            }
            .dropdown-info img {
                width: 40px;
                height: 40px;
                border-radius: 50%;
            }
            .dropdown-name {
                font-weight: 600;
                font-size: 14px;
                color: #1f1f1f;
            }
            .dropdown-item {
                display: flex;
                align-items: center;
                gap: 10px;
                padding: 9px 10px;
                border-radius: 8px;
                color: #444;
                text-decoration: none;
                font-size: 14px;
                transition: background 0.15s;
            }
            .dropdown-item:hover {
                background: #f5f5f5;
                color: #d32f2f;
            }
        </style>
    </head>
    <body>
        <header class="header">
            <div class="brand">
                <i class="bi bi-folder-fill"></i>
                <span>EduDoc</span>
            </div>
            <div class="search-container">
                <i class="bi bi-search"></i>
                <input type="text" class="search-input" placeholder="Tìm trong Drive">
            </div>
            <div class="user-profile" onclick="toggleUserMenu()" style="cursor:pointer; position:relative;">
                <img src="https://github.com/mdo.png" alt="User Avatar">
                <span class="user-name"><%= fullName%></span>
                <i class="bi bi-chevron-down" style="font-size:12px; color:#666;"></i>

                <!-- Dropdown menu -->
                <div class="user-dropdown" id="userDropdown">
                    <div class="dropdown-info">
                        <img src="https://github.com/mdo.png" alt="Avatar">
                        <div>
                            <div class="dropdown-name"><%= fullName%></div>
                        </div>
                    </div>
                    <hr style="margin: 8px 0; border-color: #f0f0f0;">
                    <a href="${pageContext.request.contextPath}/logout" class="dropdown-item">
                        <i class="bi bi-box-arrow-right"></i> Đăng xuất
                    </a>
                </div>
            </div>
        </header>

        <div class="main-layout">

            <aside class="sidebar">
                <div>
                    <button class="btn-new" onclick="openUploadModal()">
                        <i class="bi bi-plus-lg" style="font-size: 18px;"></i> Mới
                    </button>
                    <ul class="menu-list">
                        <li class="menu-item active">
                            <a href="#"><i class="bi bi-hdd-network"></i> Dữ liệu của tôi</a>
                        </li>
                        <!--                        <li class="menu-item">
                                                    <a href="#"><i class="bi bi-trash3"></i> Thùng rác</a>
                                                </li>-->
                    </ul>
                </div>
            </aside>

            <main class="content-container">
                <h2 class="content-title">Dữ liệu tài khoản</h2>

                <!-- ================= KHU VỰC THƯ MỤC ================= -->
                <div class="section-title">Thư mục</div>
                <div class="grid-container">
                    <%
                        if (folderList != null && !folderList.isEmpty()) {
                            for (Folder f : folderList) {
                    %>
                    <div class="folder-card">
                        <i class="bi bi-folder-fill" style="color: #f4b400;"></i>
                        <span><%= f.getName()%></span>
                    </div>
                    <%
                        }
                    } else {
                    %>
                    <p style="color: #747775; font-size: 14px; padding-left: 10px;">Chưa có thư mục nào ở đây.</p>
                    <% } %>
                </div>
                <!-- ================= KHU VỰC TẬP TIN (FILE) ================= -->
                <div class="section-title">Tập tin</div>
                <div class="grid-container">
                    <%
                        if (documentList != null && !documentList.isEmpty()) {
                            for (Document doc : documentList) {
                                // Định dạng biểu tượng Icon dựa theo đuôi định dạng file
                                String iconClass = "bi-file-earmark-text-fill";
                                String ext = doc.getFile_extension();
                                if (ext.contains("doc"))
                                    iconClass = "bi-file-earmark-word-fill";
                                else if (ext.contains("xls"))
                                    iconClass = "bi-file-earmark-excel-fill";
                                else if (ext.contains("pdf"))
                                    iconClass = "bi-file-earmark-pdf-fill";
                                else if (ext.contains("png") || ext.contains("jpg") || ext.contains("jpeg"))
                                    iconClass = "bi-file-earmark-image-fill";
                    %>
                    <!-- Tìm khối hiển thị file-card trong dashboard.jsp -->
                    <div class="file-card" 
                         style="cursor: pointer;" 
                         title="Nhấp để xem và chỉnh sửa trực tuyến"
                         onclick="window.location.href = '${pageContext.request.contextPath}/ViewOnlineServlet?id=<%= doc.getId()%>'">
                        <div class="file-preview">
                            <i class="bi <%= iconClass%>"></i>
                        </div>
                        <div class="file-info">
                            <span><%= doc.getOriginal_name()%></span>
                        </div>
                    </div>
                    <%
                        }
                    } else {
                    %>
                    <p style="color: #747775; font-size: 14px; padding-left: 10px;">Chưa có tập tin nào được tải lên.</p>
                    <% }%>
                </div>
            </main>
        </div>

        <div class="modal-overlay" id="uploadModal" onclick="closeUploadModal(event)">
            <div class="modal-box" onclick="event.stopPropagation()">
                <div class="modal-header">
                    <h3 class="modal-title">Tạo mới hoặc Tải lên</h3>
                    <button class="modal-close" onclick="toggleModal(false)">✕</button>
                </div>
                <div class="upload-options">
                    <button class="upload-option-btn">
                        <i class="bi bi-folder-plus" style="color: #5f6368;"></i>
                        <span>Tạo thư mục mới</span>
                    </button>
                    <button class="upload-option-btn" onclick="navigateToUpload()">
                        <i class="bi bi-cloud-arrow-up-fill" style="color: #0b57d0;"></i>
                        <span>Tải tệp lên hệ thống</span>
                    </button>
                </div>
            </div>
        </div>

        <script>
            const modal = document.getElementById('uploadModal');

            // Hàm dùng chung để ẩn/hiện Modal bằng cách bật tắt class "active"
            function toggleModal(isOpen) {
                if (isOpen) {
                    modal.classList.add('active');
                } else {
                    modal.classList.remove('active');
                }
            }

            function openUploadModal() {
                toggleModal(true);
            }

            // Đóng modal khi nhấn ra vùng trống bên ngoài hộp thoại màu trắng
            Mizuho = (e) => {
            }
            function closeUploadModal(e) {
                if (e.target === modal) {
                    toggleModal(false);
                }
            }

            // Chuyển hướng sang trang upload.jsp (đi qua Servlet quản lý Auth/Document)
            function navigateToUpload() {
                const contextPath = "${pageContext.request.contextPath}";
                window.location.href = contextPath + "/upload";
                // Lưu ý: Nếu bạn đã bảo mật upload.jsp trong WEB-INF, hãy sửa URL này thành đường dẫn dẫn tới Servlet Upload của bạn (ví dụ: contextPath + "/upload")
            }
        </script>

        <script>
            // Toggle dropdown user menu
            function toggleUserMenu() {
                document.getElementById('userDropdown').classList.toggle('show');
            }
            // Bấm ra ngoài thì đóng dropdown
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