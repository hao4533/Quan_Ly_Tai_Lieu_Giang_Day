package controller;

import dao.ShareDao;
import dao.DocumentDao;
import model.Share;
import model.Document;
import service.EmailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.UUID;

@WebServlet(name = "ShareServlet", urlPatterns = {"/ShareServlet", "/share"})
public class ShareServlet extends HttpServlet {

    private final EmailService emailService = new EmailService();
    private final ShareDao shareDao = new ShareDao();
    private final DocumentDao documentDao = new DocumentDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Chuyển hướng nếu truy cập trực tiếp bằng phương thức GET
        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Cấu hình mã hóa UTF-8 và kiểu phản hồi JSON
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {
            // 1. Lấy dữ liệu từ AJAX Form
            String recipientEmail = request.getParameter("recipientEmail");
            String documentIdStr = request.getParameter("documentId");

            // Validate dữ liệu đầu vào
            if (recipientEmail == null || recipientEmail.trim().isEmpty() ||
                documentIdStr == null || documentIdStr.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"Vui lòng nhập đầy đủ Email và chọn tài liệu!\"}");
                return;
            }

            recipientEmail = recipientEmail.trim();
            int documentId = Integer.parseInt(documentIdStr.trim());

            // 2. Tra cứu thông tin tài liệu trong CSDL
            Document doc = documentDao.getById(documentId);
            if (doc == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"error\",\"message\":\"Tài liệu không tồn tại trên hệ thống!\"}");
                return;
            }

            // 3. Tạo Token duy nhất cho liên kết chia sẻ
            String token = UUID.randomUUID().toString();

            // 4. Lưu thông tin chia sẻ vào CSDL (Share Table)
            Share share = new Share();
            share.setDocumentId(documentId);
            share.setToken(token);
            share.setRecipientEmail(recipientEmail);
            // Thiết lập thời hạn truy cập (Ví dụ: Hết hạn sau 7 ngày)
            long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000;
            share.setExpireAt(new Timestamp(System.currentTimeMillis() + sevenDaysInMillis));

            boolean isSaved = shareDao.insert(share); // Giả định ShareDao có hàm insert()
            if (!isSaved) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Không thể tạo liên kết chia sẻ trong CSDL!\"}");
                return;
            }

            // 5. Tạo đường dẫn liên kết dạng /preview?token=...
            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
            String shareLink = baseUrl + "/preview?token=" + token;

            // 6. Thực thi gửi Email qua Resend SDK
            boolean isSent = emailService.processShareDocument(recipientEmail, doc.getOriginal_name(), shareLink);

            // 7. Phản hồi JSON về cho Modal JSP
            if (isSent) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"Đã gửi email chia sẻ thành công tới " + recipientEmail + "!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Gửi email thất bại! Vui lòng kiểm tra API Key hoặc cấu hình máy chủ.\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"Mã tài liệu không hợp lệ!\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}