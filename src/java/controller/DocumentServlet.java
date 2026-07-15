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

    // ĐỊNH NGHĨA THƯ MỤC LƯU TRỮ CỐ ĐỊNH AN TOÀN TRÊN Ổ D
    private static final String UPLOAD_DIR = "D:/CTU/CT224 -- J2EE/Quan_Ly_Tai_Lieu_Giang_Day";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User currentUser = (User) session.getAttribute("user");

        // Tạo thư mục ngoài ổ D nếu chưa tồn tại
        File uploadFolder = new File(UPLOAD_DIR, "uploads");
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

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

                // Tạo tên file ngẫu nhiên duy nhất bằng UUID để tránh trùng tên file trên đĩa cứng
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // Định vị file cứng ngoài ổ D (Giữ nguyên dòng này của bạn)
                File finalPhysicalFile = new File(uploadFolder, uniqueFileName);

                try {
                    // ====================================================================
                    // THAY THẾ DÒNG: part.write(finalPhysicalFile.getAbsolutePath()); (Dòng 71)
                    // BẰNG LỆNH SAO CHÉP STREAM DƯỚI ĐÂY:
                    // ====================================================================
                    try (InputStream input = part.getInputStream()) {
                        java.nio.file.Files.copy(
                                input,
                                finalPhysicalFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                    }
                    // ====================================================================

                    // 2. Chuẩn bị đối tượng lưu thông tin vào Database (Giữ nguyên phần dưới)
                    Document doc = new Document();
                    doc.setOriginal_name(originalName);
                    doc.setPhysical_path("uploads/" + uniqueFileName);
                    doc.setFile_extension(fileExtension.toLowerCase());
                    doc.setFile_size_bytes(fileSizeBytes);
                    doc.setFolder_id(0);
                    doc.setUser_id(currentUser.getId());
                    doc.setUpdated_at(LocalDateTime.now());

                    boolean isInserted = docDao.insert(doc);
                    if (!isInserted) {
                        hasError = true;
                        finalPhysicalFile.delete(); // Rollback file nếu DB lỗi
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hasError = true;
                }
            }
        }
    }
}
