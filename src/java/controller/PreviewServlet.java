package controller;

import dao.DocumentDao;
import dao.ShareDao;
import model.Document;
import model.Share;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;

@WebServlet(name = "PreviewServlet", urlPatterns = {"/preview"})
public class PreviewServlet extends HttpServlet {

    private final ShareDao shareDao = new ShareDao();
    private final DocumentDao documentDao = new DocumentDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token == null || token.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số token chia sẻ!");
            return;
        }

        // 1. Kiểm tra Token trong DB
        Share share = shareDao.getByToken(token);
        if (share == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Liên kết chia sẻ không tồn tại!");
            return;
        }

        // 2. Kiểm tra Hạn sử dụng của Link
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (share.getExpireAt() != null && share.getExpireAt().before(now)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Liên kết chia sẻ này đã hết hạn!");
            return;
        }

        // 3. Lấy thông tin tài liệu
        Document doc = documentDao.getById(share.getDocumentId());
        if (doc == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu không còn tồn tại!");
            return;
        }

        // 4. KIỂM TRA QUYỀN SỞ HỮU (OWNERSHIP CHECK)
        HttpSession session = request.getSession(false);
        boolean isOwner = false;

        System.out.println("================ DEBUG PREVIEW ================");
        if (session != null && session.getAttribute("user") != null) {
            User currentUser = (User) session.getAttribute("user");
            System.out.println("👉 ID User Dang Dang Nhap: " + currentUser.getId());
            System.out.println("👉 ID User Tao Document:   " + doc.getUser_id());

            if (currentUser.getId() == doc.getUser_id()) {
                isOwner = true;
            }
        } else {
            System.out.println("👉 Session không có User (Người học truy cập qua link)");
        }
        System.out.println("👉 Ket qua isOwner: " + isOwner);
        System.out.println("===============================================");

        // Pass các thông tin sang JSP
        request.setAttribute("document", doc);
        request.setAttribute("shareToken", token);
        request.setAttribute("isOwner", isOwner);

        request.getRequestDispatcher("/WEB-INF/views/preview.jsp").forward(request, response);
    }
}