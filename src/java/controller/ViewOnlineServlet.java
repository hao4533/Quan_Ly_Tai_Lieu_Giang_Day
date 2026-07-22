package controller;

import dao.DocumentDao;
import model.Document;
import model.User;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Scanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ViewOnlineServlet", urlPatterns = {"/ViewOnlineServlet"})
public class ViewOnlineServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    // Hàm tiện ích lấy đường dẫn thực tế của thư mục uploads trong WebApp
    private String getUploadPath() {
        return getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID tài liệu!");
            return;
        }

        int docId = Integer.parseInt(idParam);
        DocumentDao docDao = new DocumentDao();
        Document doc = docDao.getById(docId);

        if (doc == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu!");
            return;
        }

        // 1. XỬ LÝ DOWNLOAD CHO ONLYOFFICE SERVER TẢI FILE
        if ("download".equals(action)) {
            // Sửa: Lấy file đúng thư mục uploads trong WebApp
            File file = new File(getUploadPath(), doc.getPhysical_path());
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File vật lý không tồn tại!");
                return;
            }

            response.setContentType(getServletContext().getMimeType(file.getName()));
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + doc.getOriginal_name() + "\"");

            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)); OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[1024 * 4];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return;
        }

        // 2. HIỂN THỊ GIAO DIỆN PREVIEW
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (doc.getUser_id() != currentUser.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập tài liệu này!");
            return;
        }

        request.setAttribute("document", doc);
        request.getRequestDispatcher("/WEB-INF/views/preview.jsp").forward(request, response);
    }

    // 3. XỬ LÝ CALLBACK TỪ ONLYOFFICE DOCUMENT SERVER KHI LƯU FILE
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        try {
            // Đọc dữ liệu JSON gửi từ OnlyOffice Server
            Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";

            // Tìm giá trị "status" và "url" từ chuỗi JSON thô
            int status = -1;
            String downloadUrl = null;

            if (body.contains("\"status\":")) {
                String temp = body.substring(body.indexOf("\"status\":") + 9);
                if (temp.contains(",")) {
                    temp = temp.substring(0, temp.indexOf(","));
                }
                if (temp.contains("}")) {
                    temp = temp.substring(0, temp.indexOf("}"));
                }
                status = Integer.parseInt(temp.trim());
            }

            if (body.contains("\"url\":")) {
                String temp = body.substring(body.indexOf("\"url\":") + 6);
                temp = temp.substring(temp.indexOf("\"") + 1);
                downloadUrl = temp.substring(0, temp.indexOf("\"")).replace("\\/", "/");
            }

            String idParam = request.getParameter("id");
            if (idParam != null && (status == 2 || status == 3)) { // Trạng thái 2 hoặc 3: Sẵn sàng lưu trữ
                int docId = Integer.parseInt(idParam);
                DocumentDao docDao = new DocumentDao();
                Document doc = docDao.getById(docId);

                if (doc != null && downloadUrl != null) {
                    // Sửa: Lưu file đè lại đúng vị trí trong thư mục uploads của WebApp
                    File file = new File(getUploadPath(), doc.getPhysical_path());

                    // Tải file đã chỉnh sửa từ OnlyOffice Server và ghi đè
                    try (InputStream in = new URL(downloadUrl).openStream(); FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024 * 4];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }

                    // Cập nhật lại dung lượng mới và thời gian cập nhật vào Database
                    doc.setFile_size_bytes(file.length());
                    doc.setUpdated_at(LocalDateTime.now());
                    docDao.update(doc);
                }
            }

            // Phản hồi bắt buộc của OnlyOffice để xác nhận xử lý thành công
            out.write("{\"error\":0}");
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"error\":1, \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
