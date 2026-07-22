package controller;

import dao.DocumentDao;
import model.Document;
import model.User;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet(name = "DocumentServlet", urlPatterns = {"/DocumentServlet"}) // Đảm bảo trùng action của form trong upload.jsp
@MultipartConfig(
        maxFileSize = 1024 * 1024 * 50, // Giới hạn 50MB một file
        maxRequestSize = 1024 * 1024 * 100 // Giới hạn 100MB tổng lượt gửi
)
public class DocumentServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    // Hàm tiện ích để lấy đường dẫn tuyệt đối của thư mục uploads trong WebApp
    private String getUploadPath() {
        return getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
    }

    // Xử lý XÓA tài liệu
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String action = request.getParameter("action");

        if (!"delete".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Bạn cần đăng nhập!\"}");
            return;
        }
        User currentUser = (User) session.getAttribute("user");

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.getWriter().write("{\"success\":false,\"message\":\"Thiếu ID tài liệu!\"}");
            return;
        }

        try {
            int docId = Integer.parseInt(idParam);
            DocumentDao docDao = new DocumentDao();
            Document doc = docDao.getById(docId);

            if (doc == null) {
                response.getWriter().write("{\"success\":false,\"message\":\"Tài liệu không tồn tại!\"}");
                return;
            }
            if (doc.getUser_id() != currentUser.getId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"success\":false,\"message\":\"Bạn không có quyền xóa tài liệu này!\"}");
                return;
            }

            boolean deleted = docDao.deleteSecure(docId, currentUser.getId());
            if (deleted) {
                // Xóa file vật lý tương ứng trong thư mục uploads của webapp
                File physicalFile = new File(getUploadPath(), doc.getPhysical_path());
                if (physicalFile.exists()) {
                    physicalFile.delete();
                }
                response.getWriter().write("{\"success\":true}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"Xóa thất bại, vui lòng thử lại!\"}");
            }
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\":false,\"message\":\"ID không hợp lệ!\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false,\"message\":\"Lỗi hệ thống!\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User currentUser = (User) session.getAttribute("user");

        // Lấy đường dẫn thực tế của thư mục uploads trong WebApp
        File uploadFolder = new File(getUploadPath());
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs(); // Tự động tạo thư mục uploads nếu chưa có
        }

        // In đường dẫn tuyệt đối ra Console để dễ kiểm tra vị trí lưu file
        System.out.println("===> [DEBUG] Vi tri luu file thuc te: " + uploadFolder.getAbsolutePath());

        DocumentDao docDao = new DocumentDao();
        Collection<Part> parts = request.getParts();
        boolean hasError = false;

        for (Part part : parts) {
            if ("files".equals(part.getName()) && part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty()) {
                String originalName = part.getSubmittedFileName();
                long fileSizeBytes = part.getSize();

                String fileExtension = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex >= 0) {
                    fileExtension = originalName.substring(dotIndex);
                }

                // Tạo tên file duy nhất bằng UUID
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // Định vị file vật lý trong thư mục uploads
                File finalPhysicalFile = new File(uploadFolder, uniqueFileName);

                try {
                    // Sao chép stream từ Part vào file đích
                    try (InputStream input = part.getInputStream()) {
                        java.nio.file.Files.copy(
                                input,
                                finalPhysicalFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                    }

                    System.out.println("===> [DEBUG] Da luu file thanh cong: " + finalPhysicalFile.getAbsolutePath());

                    // Chuẩn bị đối tượng lưu thông tin vào Database
                    Document doc = new Document();
                    doc.setOriginal_name(originalName);
                    doc.setPhysical_path(uniqueFileName);
                    doc.setFile_extension(fileExtension.toLowerCase());
                    doc.setFile_size_bytes(fileSizeBytes);
                    doc.setFolder_id(0);
                    doc.setUser_id(currentUser.getId());
                    doc.setUpdated_at(LocalDateTime.now());

                    boolean isInserted = docDao.insert(doc);
                    if (!isInserted) {
                        hasError = true;
                        finalPhysicalFile.delete(); // Rollback file nếu lưu DB thất bại
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hasError = true;
                }
            }
        }
    }
}
