<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%@ page import="model.Folder" %>
<%@ page import="model.Document" %>
<%@ page import="dao.FolderDao" %>
<%@ page import="dao.DocumentDao" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String fullName = currentUser.getFullName();
    int userId = currentUser.getId();

    FolderDao folderDao = new FolderDao();
    List<Folder> folderList = folderDao.getRootFolderByUserId(userId);

    DocumentDao documentDao = new DocumentDao();
    List<Document> documentList = documentDao.getRootDocumentsByUserId(userId);
    // Lấy danh sách tài liệu được chia sẻ với người dùng
    List<Document> sharedDocumentList = documentDao.getSharedDocumentsByUserId(userId);
%>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>EduDoc</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style_dashboard.css">        
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
                        <!-- Cập nhật thêm tính năng chuyển đổi tab -->
                        <li class="menu-item active" id="tab-my-drive" onclick="switchTab('my-drive')">
                            <a href="#"><i class="bi bi-hdd-network"></i> Dữ liệu của tôi</a>
                        </li>
                        <li class="menu-item" id="tab-shared" onclick="switchTab('shared')">
                            <a href="#"><i class="bi bi-people"></i> Đã được chia sẻ</a>
                        </li>
                    </ul>
                </div>
            </aside>

            <main class="content-container">
                <!-- Section 1: Dữ liệu tài khoản của tôi -->
                <div id="section-my-drive">
                    <h2 class="content-title">Dữ liệu tài khoản</h2>
                    <div class="section-title">Tập tin</div>
                    <div class="grid-container">
                        <%
                            if (documentList != null && !documentList.isEmpty()) {
                                for (Document doc : documentList) {
                                    String iconClass = "bi-file-earmark-text-fill";
                                    String ext = doc.getFile_extension();
                                    if (ext != null) {
                                        ext = ext.toLowerCase();
                                        if (ext.contains("doc")) iconClass = "bi-file-earmark-word-fill";
                                        else if (ext.contains("xls")) iconClass = "bi-file-earmark-excel-fill";
                                        else if (ext.contains("pdf")) iconClass = "bi-file-earmark-pdf-fill";
                                        else if (ext.contains("png") || ext.contains("jpg") || ext.contains("jpeg"))
                                            iconClass = "bi-file-earmark-image-fill";
                                    }
                        %>
                        <div class="file-card" 
                             id="file-card-<%= doc.getId()%>"
                             style="cursor: pointer; position: relative;" 
                             title="Nhấp để xem và chỉnh sửa trực tuyến"
                             onclick="window.location.href = '${pageContext.request.contextPath}/ViewOnlineServlet?id=<%= doc.getId()%>'">

                            <button type="button" class="file-menu-btn"
                                    onclick="event.stopPropagation(); toggleFileMenu(<%= doc.getId()%>)">
                                <i class="bi bi-three-dots-vertical"></i>
                            </button>
                            <div class="file-menu-dropdown" id="file-menu-<%= doc.getId()%>" onclick="event.stopPropagation()">
                                <button type="button" class="file-menu-item" 
                                        onclick="shareFile(<%= doc.getId()%>, '<%= doc.getOriginal_name().replace("'", "\\'")%>')">
                                    <i class="bi bi-people-fill"></i> Chia sẻ
                                </button>
                                <button type="button" class="file-menu-item file-menu-item-danger"
                                        onclick="deleteFile(<%= doc.getId()%>, '<%= doc.getOriginal_name().replace("'", "\\'")%>')">
                                    <i class="bi bi-trash3"></i> Xóa
                                </button>
                            </div>

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
                </div>

                <!-- Section 2: Tài liệu đã được chia sẻ với tôi -->
                <div id="section-shared" style="display: none;">
                    <h2 class="content-title">Tài liệu đã được chia sẻ</h2>
                    <div class="section-title">Tập tin</div>
                    <div class="grid-container">
                        <%
                            if (sharedDocumentList != null && !sharedDocumentList.isEmpty()) {
                                for (Document sharedDoc : sharedDocumentList) {
                                    String sharedIconClass = "bi-file-earmark-text-fill";
                                    String sharedExt = sharedDoc.getFile_extension();
                                    if (sharedExt != null) {
                                        sharedExt = sharedExt.toLowerCase();
                                        if (sharedExt.contains("doc")) sharedIconClass = "bi-file-earmark-word-fill";
                                        else if (sharedExt.contains("xls")) sharedIconClass = "bi-file-earmark-excel-fill";
                                        else if (sharedExt.contains("pdf")) sharedIconClass = "bi-file-earmark-pdf-fill";
                                        else if (sharedExt.contains("png") || sharedExt.contains("jpg") || sharedExt.contains("jpeg"))
                                            sharedIconClass = "bi-file-earmark-image-fill";
                                    }
                        %>
                        <!-- Ẩn tính năng 3 chấm (Xóa/Chia sẻ lại) đối với tài liệu được người khác chia sẻ -->
                        <div class="file-card" 
                             style="cursor: pointer; position: relative;" 
                             title="Nhấp để xem tài liệu được chia sẻ"
                             onclick="window.location.href = '${pageContext.request.contextPath}/ViewOnlineServlet?id=<%= sharedDoc.getId()%>'">

                            <div class="file-preview">
                                <i class="bi <%= sharedIconClass%>"></i>
                            </div>
                            <div class="file-info">
                                <span><%= sharedDoc.getOriginal_name()%></span>
                            </div>
                        </div>
                        <%
                                }
                            } else {
                        %>
                        <p style="color: #747775; font-size: 14px; padding-left: 10px;">Chưa có tài liệu nào được chia sẻ với bạn.</p>
                        <% }%>
                    </div>
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
                    <button class="upload-option-btn" onclick="navigateToUpload()">
                        <i class="bi bi-cloud-arrow-up-fill" style="color: #0b57d0;"></i>
                        <span>Tải tệp lên hệ thống</span>
                    </button>
                </div>
            </div>
        </div>

        <!-- NHÚNG COMPONENT MODAL CHIA SẺ VÀO DASHBOARD -->
        <jsp:include page="share-modal.jsp" />

        <script>
            const modal = document.getElementById('uploadModal');

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

            function closeUploadModal(e) {
                if (e.target === modal) {
                    toggleModal(false);
                }
            }

            function navigateToUpload() {
                const contextPath = "${pageContext.request.contextPath}";
                window.location.href = contextPath + "/upload";
            }

            // ================= MENU 3 CHẤM =================
            const contextPath = "${pageContext.request.contextPath}";

            function closeAllFileMenus() {
                document.querySelectorAll('.file-menu-dropdown.show').forEach(function (el) {
                    el.classList.remove('show');
                });
            }

            function toggleFileMenu(docId) {
                const dropdown = document.getElementById('file-menu-' + docId);
                const isOpen = dropdown.classList.contains('show');
                closeAllFileMenus();
                if (!isOpen) {
                    dropdown.classList.add('show');
                }
            }

            document.addEventListener('click', function () {
                closeAllFileMenus();
            });

            // ================= MỞ MODAL CHIA SẺ =================
            function shareFile(docId, docName) {
                closeAllFileMenus();
                openShareModal(docId, docName);
            }

            // ================= XÓA FILE =================
            function deleteFile(docId, fileName) {
                closeAllFileMenus();
                if (!confirm('Bạn có chắc muốn xóa "' + fileName + '"? Hành động này không thể hoàn tác.')) {
                    return;
                }

                fetch(contextPath + '/DocumentServlet?action=delete&id=' + docId)
                        .then(function (res) {
                            return res.json();
                        })
                        .then(function (data) {
                            if (data.success) {
                                const card = document.getElementById('file-card-' + docId);
                                if (card)
                                    card.remove();
                            } else {
                                alert(data.message || 'Xóa thất bại, vui lòng thử lại!');
                            }
                        })
                        .catch(function () {
                            alert('Lỗi kết nối, vui lòng thử lại!');
                        });
            }

            // ================= HIỂN THỊ MENU NGƯỜI DÙNG =================
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

            // ================= CHUYỂN TAB TÀI LIỆU =================
            function switchTab(tabName) {
                // Ẩn nội dung
                document.getElementById('section-my-drive').style.display = 'none';
                document.getElementById('section-shared').style.display = 'none';
                
                // Xóa trạng thái active trên sidebar
                document.getElementById('tab-my-drive').classList.remove('active');
                document.getElementById('tab-shared').classList.remove('active');
                
                // Kích hoạt tab được chọn
                if(tabName === 'my-drive') {
                    document.getElementById('section-my-drive').style.display = 'block';
                    document.getElementById('tab-my-drive').classList.add('active');
                } else if(tabName === 'shared') {
                    document.getElementById('section-shared').style.display = 'block';
                    document.getElementById('tab-shared').classList.add('active');
                }
            }
        </script>
    </body>
</html>