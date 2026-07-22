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

@WebServlet(name = "DocumentServlet", urlPatterns = {"/DocumentServlet"})
@MultipartConfig(
        maxFileSize = 1024 * 1024 * 50, // Giới hạn 50MB
        maxRequestSize = 1024 * 1024 * 100 // Giới hạn 100MB
)
public class DocumentServlet extends HttpServlet {

    // 🔴 ĐƯỜNG DẪN CỐ ĐỊNH NGOÀI Ổ ĐĨA
    private static final String EXTERNAL_STORAGE_DIR = "D:" + File.separator + "app_data" + File.separator + "uploads";

    private String getStoragePath() {
        File folder = new File(EXTERNAL_STORAGE_DIR);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return EXTERNAL_STORAGE_DIR;
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
                // Xóa file vật lý tại thư mục ngoài ổ đĩa
                File physicalFile = new File(getStoragePath(), doc.getPhysical_path());
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

        File uploadFolder = new File(getStoragePath());

        DocumentDao docDao = new DocumentDao();
        Collection<Part> parts = request.getParts();

        for (Part part : parts) {
            if ("files".equals(part.getName()) && part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty()) {
                String originalName = part.getSubmittedFileName();
                long fileSizeBytes = part.getSize();

                String fileExtension = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex >= 0) {
                    fileExtension = originalName.substring(dotIndex);
                }

                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // Định vị file vật lý ở thư mục ngoài ổ đĩa
                File finalPhysicalFile = new File(uploadFolder, uniqueFileName);

                try {
                    try (InputStream input = part.getInputStream()) {
                        java.nio.file.Files.copy(
                                input,
                                finalPhysicalFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                    }

                    System.out.println("===> [PRODUCTION STORAGE] File đã lưu an toàn tại: " + finalPhysicalFile.getAbsolutePath());

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
                        finalPhysicalFile.delete(); // Rollback nếu DB lỗi
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}