package controller;

import dao.UserDao;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "AdminServlet", urlPatterns = {"/AdminServlet"})
public class AdminServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String action = request.getParameter("action");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Bạn cần đăng nhập!\"}");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (!currentUser.isAdmin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Bạn không có quyền thực hiện thao tác này!\"}");
            return;
        }

        // =========================================================================
        // Action 1: CẬP NHẬT HỌ TÊN NGƯỜI DÙNG (updateUser)
        // =========================================================================
        if ("updateUser".equals(action)) {
            String idParam = request.getParameter("id");
            String fullName = request.getParameter("fullName");

            if (idParam == null || fullName == null || fullName.trim().isEmpty()) {
                response.getWriter().write("{\"success\":false,\"message\":\"Thông tin nhập vào không hợp lệ!\"}");
                return;
            }

            try {
                int targetId = Integer.parseInt(idParam);
                User targetUser = userDao.getById(targetId);

                if (targetUser == null) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Tài khoản không tồn tại!\"}");
                    return;
                }

                // Thực hiện cập nhật trong CSDL
                boolean updated = userDao.updateFullName(targetId, fullName.trim());
                if (updated) {
                    // Nếu admin tự sửa tên chính mình, cập nhật luôn tên hiển thị trong Session hiện tại
                    if (targetId == currentUser.getId()) {
                        currentUser.setFullName(fullName.trim());
                        session.setAttribute("user", currentUser);
                    }
                    response.getWriter().write("{\"success\":true,\"message\":\"Cập nhật họ tên thành công!\"}");
                } else {
                    response.getWriter().write("{\"success\":false,\"message\":\"Cập nhật thất bại, vui lòng thử lại!\"}");
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\":false,\"message\":\"ID không hợp lệ!\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.getWriter().write("{\"success\":false,\"message\":\"Lỗi hệ thống!\"}");
            }
            return;
        }

        // =========================================================================
        // Action 2: XÓA NGƯỜI DÙNG (deleteUser)
        // =========================================================================
        if ("deleteUser".equals(action)) {
            String idParam = request.getParameter("id");
            if (idParam == null) {
                response.getWriter().write("{\"success\":false,\"message\":\"Thiếu ID tài khoản!\"}");
                return;
            }

            try {
                int targetId = Integer.parseInt(idParam);

                // Không cho admin tự xóa chính mình
                if (targetId == currentUser.getId()) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Bạn không thể tự xóa chính tài khoản của mình!\"}");
                    return;
                }

                User targetUser = userDao.getById(targetId);
                if (targetUser == null) {
                    response.getWriter().write("{\"success\":false,\"message\":\"Tài khoản không tồn tại!\"}");
                    return;
                }

                // Không cho xóa bất kỳ tài khoản admin nào khác
                if (targetUser.isAdmin()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"success\":false,\"message\":\"Không thể xóa tài khoản quản trị viên (admin)!\"}");
                    return;
                }

                // Thực hiện xóa tài khoản
                boolean deleted = userDao.deleteUserAccount(targetId);
                if (deleted) {
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
            return;
        }

        // Trường hợp tham số action không đúng
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}