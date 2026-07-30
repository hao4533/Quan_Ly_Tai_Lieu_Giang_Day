package controller;

import service.InternalShareService;
import dao.DocumentDao;
import dao.UserDao;
import model.Document;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "InternalShareServlet", urlPatterns = {"/share-internal"})
public class InternalShareServlet extends HttpServlet {

    // Gọi thông qua Service thay vì DAO trực tiếp
    private final InternalShareService shareService = new InternalShareService();
    private final DocumentDao documentDao = new DocumentDao(); // DocumentDao có thể có DocumentService bọc ngoài nếu bạn đã tạo
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {
            // 1. Kiểm tra session đăng nhập
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"error\",\"message\":\"Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!\"}");
                return;
            }
            User currentUser = (User) session.getAttribute("user");

            // 2. Lấy dữ liệu từ form
            String documentIdStr = request.getParameter("documentId");
            String targetUserIdStr = request.getParameter("targetUserId");
            String[] permissionIdsStr = request.getParameterValues("permissions");

            // 3. Validate dữ liệu đầu vào
            if (documentIdStr == null || documentIdStr.trim().isEmpty() ||
                targetUserIdStr == null || targetUserIdStr.trim().isEmpty() ||
                permissionIdsStr == null || permissionIdsStr.length == 0) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"Vui lòng chọn người dùng và ít nhất một quyền truy cập!\"}");
                return;
            }

            int documentId = Integer.parseInt(documentIdStr.trim());
            int targetUserId = Integer.parseInt(targetUserIdStr.trim());

            // 4. Không cho phép tự chia sẻ tài liệu cho chính mình
            if (targetUserId == currentUser.getId()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"Không thể tự chia sẻ tài liệu cho chính mình!\"}");
                return;
            }

            // 5. Kiểm tra tài liệu tồn tại
            Document doc = documentDao.getById(documentId);
            if (doc == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"error\",\"message\":\"Tài liệu không tồn tại trên hệ thống!\"}");
                return;
            }

            // 6. Kiểm tra người dùng nhận chia sẻ có tồn tại trong hệ thống hay không
            User targetUser = userDao.getById(targetUserId);
            if (targetUser == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"error\",\"message\":\"Người dùng nhận chia sẻ không tồn tại trên hệ thống!\"}");
                return;
            }

            // 7. Chuyển giao toàn bộ logic nghiệp vụ cho Service xử lý
            boolean isSaved = shareService.shareDocument(documentId, targetUserId, currentUser.getId(), permissionIdsStr);

            // 8. Phản hồi JSON
            if (isSaved) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"status\":\"success\",\"message\":\"Chia sẻ tài liệu nội bộ thành công!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Không thể lưu cấu hình chia sẻ vào cơ sở dữ liệu!\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"Dữ liệu đầu vào không hợp lệ!\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}");
        } finally {
            out.flush(); 
        }
    }
}
