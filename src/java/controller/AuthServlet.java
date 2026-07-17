package controller;

import dao.UserDao;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(urlPatterns = {"/login", "/register", "/logout", "/dashboard", "/upload"})
public class AuthServlet extends HttpServlet {

    // Khớp chính xác 100% với tên JNDI đang chạy thực tế của bạn
    private static final String JNDI_NAME = "jdbc/UsersDB";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        HttpSession session = request.getSession(false);

        // Chặn truy cập dashboard/upload nếu chưa đăng nhập
        if ("/dashboard".equals(path) || "/upload".equals(path)) {
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }

        // Nếu đã đăng nhập rồi mà cố vào login/register -> chuyển thẳng vào dashboard
        if ("/login".equals(path) || "/register".equals(path)) {
            if (session != null && session.getAttribute("user") != null) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
        }

        switch (path) {
            case "/login":
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                break;
            case "/register":
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                break;
            case "/dashboard":
                request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                break;
            case "/upload":
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                break;
            case "/logout":
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        // 🌟 SỬA LỖI: Khởi tạo đối tượng userDao tại đây để các khối lệnh bên dưới sử dụng
        UserDao userDao = new UserDao();
        // ===================== XỬ LÝ ĐĂNG NHẬP =====================
        if ("/login".equals(path)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            // Kiểm tra tính hợp lệ dữ liệu đầu vào
            if (email == null || email.trim().isEmpty()
                    || password == null || password.trim().isEmpty()) {
                request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }

            // Thực hiện xác thực
            User user = userDao.login(email.trim(), password);

            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60); // Hết hạn sau 30 phút
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                request.setAttribute("error", "Email hoặc mật khẩu không chính xác!");
                request.setAttribute("oldEmail", email);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }

            // ===================== XỬ LÝ ĐĂNG KÝ =====================
        } else if ("/register".equals(path)) {
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirm = request.getParameter("confirmPassword");

            if (username == null || username.trim().isEmpty()
                    || email == null || email.trim().isEmpty()
                    || password == null || password.trim().isEmpty()) {
                request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            if (!password.equals(confirm)) {
                request.setAttribute("error", "Mật khẩu xác nhận không khớp!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            if (userDao.isEmailExists(email.trim())) {
                request.setAttribute("error", "Email này đã được sử dụng, vui lòng chọn email khác!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            String passwordHash = UserDao.hashPassword(password); // Băm mật khẩu SHA-256

            User newUser = new User(email.trim(), passwordHash, username.trim());
            boolean success = userDao.insert(newUser);

            if (success) {
                response.sendRedirect(request.getContextPath() + "/login?msg=success");
            } else {
                request.setAttribute("error", "Đăng ký thất bại do hệ thống lỗi kết nối Database!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            }
        }
    }

    private void giuLaiDuLieuRegister(HttpServletRequest request, String username, String email) {
        request.setAttribute("oldUsername", username);
        request.setAttribute("oldEmail", email);
    }
}
