<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Document" %>
<%@ page import="model.User" %>
<%
    Document doc = (Document) request.getAttribute("document");
    if (doc == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu trong Attribute!");
        return;
    }

    Boolean isOwnerObj = (Boolean) request.getAttribute("isOwner");
    boolean isOwner = (isOwnerObj != null && isOwnerObj);
    String shareToken = (String) request.getAttribute("shareToken");

    // =========================================================================
    // 🆕 QUYỀN download / edit / print
    // - Chủ sở hữu: luôn full quyền.
    // - Được chia sẻ nội bộ (ViewOnlineServlet set sẵn canDownload/canEdit/canPrint
    //   dựa theo cấu hình chia sẻ thực tế): dùng đúng giá trị đó.
    // - Luồng share qua email/token (không set 3 attribute này): GIỮ NGUYÊN hành vi
    //   cũ trước đây (download=true, edit=false, print=true) để không ảnh hưởng
    //   tính năng chia sẻ qua email đang hoạt động.
    // =========================================================================
    Boolean canDownloadObj = (Boolean) request.getAttribute("canDownload");
    Boolean canEditObj = (Boolean) request.getAttribute("canEdit");
    Boolean canPrintObj = (Boolean) request.getAttribute("canPrint");

    boolean canDownload = isOwner ? true : (canDownloadObj != null ? canDownloadObj : true);
    boolean canEdit = isOwner ? true : (canEditObj != null ? canEditObj : false);
    boolean canPrint = isOwner ? true : (canPrintObj != null ? canPrintObj : true);

    // IP / Host cấu hình
    String hostIP = "10.13.131.216"; // IP máy host chạy Docker & Glassfish
    int glassfishPort = request.getServerPort();

    // 1. URL nạp API OnlyOffice
    String onlyOfficeApiUrl = "http://" + hostIP + ":8089/web-apps/apps/api/documents/api.js";

    // 2. URL để Docker gọi ngược lại GlassFish lấy file binary
    String dockerToHost = "host.docker.internal";
    String serverPathForDocker = request.getScheme() + "://" + dockerToHost + ":" + glassfishPort + request.getContextPath();

    String fileUrl = "";
    if (shareToken != null && !shareToken.trim().isEmpty() && !"null".equalsIgnoreCase(shareToken.trim())) {
        fileUrl = serverPathForDocker + "/ViewOnlineServlet?action=download_by_token&token=" + shareToken;
    } else {
        fileUrl = serverPathForDocker + "/ViewOnlineServlet?action=download&id=" + doc.getId();
    }

    String callbackUrl = serverPathForDocker + "/ViewOnlineServlet?id=" + doc.getId();

    // Định dạng file
    String fileExt = (doc.getFile_extension() != null && !doc.getFile_extension().isEmpty()) 
                        ? doc.getFile_extension().replace(".", "").toLowerCase().trim() 
                        : "docx";
    
    String documentType = "word";
    if (fileExt.equals("xlsx") || fileExt.equals("xls")) {
        documentType = "cell";
    } else if (fileExt.equals("pptx") || fileExt.equals("ppt")) {
        documentType = "slide";
    }

    // Key độc nhất để OnlyOffice nhận diện cache (nếu sửa file key sẽ thay đổi)
    String documentKey = "DocKey_" + doc.getId() + "_" + (doc.getUpdated_at() != null ? doc.getUpdated_at().hashCode() : System.currentTimeMillis());

    User currentUser = (User) session.getAttribute("user");
    String userName = (currentUser != null && currentUser.getFullName() != null) 
                        ? currentUser.getFullName() 
                        : (isOwner ? "Giảng viên" : "Người học");
    String userId = (currentUser != null) ? String.valueOf(currentUser.getId()) : "guest";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Xem tài liệu - <%= doc.getOriginal_name() %></title>
    <style>
        html, body {
            width: 100%;
            height: 100%;
            margin: 0;
            padding: 0;
            overflow: hidden;
        }
        #placeholder {
            width: 100%;
            height: 100%;
        }
    </style>
    <!-- Script API OnlyOffice -->
    <script type="text/javascript" src="<%= onlyOfficeApiUrl %>"></script>
</head>
<body>
    <div id="placeholder"></div>

    <script type="text/javascript">
        document.addEventListener("DOMContentLoaded", function() {
            if (typeof DocsAPI === "undefined") {
                console.error("❌ Không thể nạp DocsAPI từ OnlyOffice! Kiểm tra lại container Docker.");
                document.getElementById("placeholder").innerHTML = "<h3 style='color:red; text-align:center; margin-top:50px;'>Không thể kết nối đến máy chủ xem tài liệu (OnlyOffice)!</h3>";
                return;
            }

            var config = {
                "document": {
                    "fileType": "<%= fileExt %>",
                    "key": "<%= documentKey %>",
                    "title": "<%= doc.getOriginal_name() %>",
                    "url": "<%= fileUrl %>",
                    "permissions": {
                        "download": <%= canDownload %>,
                        "edit": <%= canEdit %>,
                        "print": <%= canPrint %>
                    }
                },
                "documentType": "<%= documentType %>",
                "editorConfig": {
                    "mode": "<%= canEdit ? "edit" : "view" %>",
                    "lang": "vi",
                    "callbackUrl": "<%= callbackUrl %>",
                    "user": {
                        "id": "<%= userId %>",
                        "name": "<%= userName %>"
                    },
                    "customization": {
                        "autosave": true,
                        "forcesave": true
                    }
                },
                "width": "100%",
                "height": "100%"
            };

            // Khởi tạo Editor
            var docEditor = new DocsAPI.DocEditor("placeholder", config);
        });
    </script>
</body>
</html>
