<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.Document" %>
<%
    Document doc = (Document) request.getAttribute("document");
    if (doc == null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }

    String myHostIP = "172.16.142.197";
    int glassfishPort = request.getServerPort();

    // Cổng chạy Docker OnlyOffice của bạn (Đồng nhất là 8089)
    String onlyOfficeHost = "http://" + myHostIP + ":8089";

    String serverPath = request.getScheme() + "://" + myHostIP + ":" + glassfishPort + request.getContextPath();

    String fileUrl = serverPath + "/ViewOnlineServlet?action=download&id=" + doc.getId();
    String callbackUrl = serverPath + "/ViewOnlineServlet?id=" + doc.getId();

    String fileExt = doc.getFile_extension().replace(".", "").toLowerCase().trim();
    String documentType = "word";
    if (fileExt.equals("xlsx") || fileExt.equals("xls")) {
        documentType = "cell";
    } else if (fileExt.equals("pptx") || fileExt.equals("ppt")) {
        documentType = "slide";
    }

    String documentKey = "DocKey_" + doc.getId() + "_" + doc.getUpdated_at().hashCode();
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><%= doc.getOriginal_name()%> - Trình đọc trực tuyến</title>
        <style>
            html, body {
                height: 100%;
                margin: 0;
                padding: 0;
                overflow: hidden;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            #header-bar {
                height: 50px;
                background-color: #f8f9fa;
                border-bottom: 1px solid #e0e0e0;
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 0 20px;
                box-sizing: border-box;
            }
            .back-btn {
                color: #0b57d0;
                text-decoration: none;
                font-weight: 500;
                font-size: 14px;
                display: flex;
                align-items: center;
                gap: 6px;
            }
            .back-btn:hover {
                text-decoration: underline;
            }
            #editor-container {
                height: calc(100% - 50px);
                width: 100%;
            }
        </style>
        <!-- Nhúng thư viện API trực tiếp từ OnlyOffice Server -->
        <script type="text/javascript" src="<%= onlyOfficeHost%>/web-apps/apps/api/documents/api.js"></script>
    </head>
    <body>

        <div id="header-bar">
            <a href="${pageContext.request.contextPath}/dashboard" class="back-btn">
                ◀ Quay lại Dashboard
            </a>
            <strong style="color: #1f1f1f;"><%= doc.getOriginal_name()%></strong>
            <div style="width: 100px;"></div>
        </div>

        <div id="editor-container"></div>

        <script type="text/javascript">
            var docEditor = new DocsAPI.DocEditor("editor-container", {
                "document": {
                    "fileType": "<%= fileExt%>",
                    "key": "<%= documentKey%>",
                    "title": "<%= doc.getOriginal_name()%>",
                    "url": "<%= fileUrl%>"
                },
                "documentType": "<%= documentType%>",
                "editorConfig": {
                    "callbackUrl": "<%= callbackUrl%>",
                    "lang": "vi",
                    "mode": "edit", // Để "view" nếu chỉ muốn xem, "edit" để cho phép sửa trực tiếp
                    "user": {
                        "id": "User_<%= doc.getUser_id()%>",
                        "name": "Giảng viên"
                    },
                    "customization": {
                        "forcesave": true // Buộc gửi cập nhật liên tục khi ấn nút Save của OnlyOffice
                    }
                },
                "height": "100%",
                "width": "100%"
            });
        </script>
    </body>
</html>