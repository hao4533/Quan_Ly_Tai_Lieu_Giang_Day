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

        // Nếu đã đăng nhập rồi mà vào login/register -> chuyển thẳng vào dashboard
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

        // ===================== ĐĂNG NHẬP =====================
        if ("/login".equals(path)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            // Kiểm tra không được để trống
            if (username == null || username.trim().isEmpty()
                    || password == null || password.trim().isEmpty()) {
                request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }

            // Gọi DAO để xác thực tài khoản từ DB
            UserDao userDao = new UserDao();
            User user = userDao.login(username.trim(), password);

            if (user != null) {
                // Đăng nhập thành công -> lưu user vào session
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60); // hết hạn sau 30 phút
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                // Sai tài khoản hoặc mật khẩu
                request.setAttribute("error", "Tên tài khoản hoặc mật khẩu không chính xác!");
                request.setAttribute("oldUsername", username); // giữ lại giá trị đã nhập
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }

            // ===================== ĐĂNG KÝ =====================
        } else if ("/register".equals(path)) {
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirm = request.getParameter("confirmPassword");

            // --- Validate: Kiểm tra không để trống ---
            if (username == null || username.trim().isEmpty()
                    || email == null || email.trim().isEmpty()
                    || password == null || password.trim().isEmpty()) {
                request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            // --- Validate: Mật khẩu xác nhận ---
            if (!password.equals(confirm)) {
                request.setAttribute("error", "Mật khẩu xác nhận không khớp!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            // --- Validate: Kiểm tra độ mạnh mật khẩu ---
            String passwordError = validatePassword(password);
            if (passwordError != null) {
                request.setAttribute("error", passwordError);
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            UserDao userDao = new UserDao();

            // --- Kiểm tra email đã tồn tại chưa ---
            if (userDao.isEmailExists(email.trim())) {
                request.setAttribute("error", "Email này đã được sử dụng, vui lòng chọn email khác!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            // --- Hash mật khẩu và lưu vào DB ---
            String passwordHash = UserDao.hashPassword(password);
            boolean success = userDao.registerUser(email.trim(), passwordHash, username.trim());

            if (success) {
                // Đăng ký thành công -> chuyển sang trang login kèm thông báo
                response.sendRedirect(request.getContextPath() + "/login?msg=success");
            } else {
                request.setAttribute("error", "Đăng ký thất bại, vui lòng thử lại!");
                giuLaiDuLieuRegister(request, username, email);
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            }
        }
    }

    // Hàm kiểm tra độ mạnh mật khẩu
    private String validatePassword(String password) {
        if (password == null || password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự!";
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpper) {
            return "Mật khẩu phải có ít nhất 1 chữ cái viết hoa!";
        }
        if (!hasLetter || !hasDigit) {
            return "Mật khẩu phải bao gồm cả chữ cái và chữ số!";
        }
        return null; // hợp lệ
    }

    // Hàm phụ: giữ lại dữ liệu đã nhập khi form bị lỗi (UX tốt hơn)
    private void giuLaiDuLieuRegister(HttpServletRequest request, String username, String email) {
        request.setAttribute("oldUsername", username);
        request.setAttribute("oldEmail", email);
    }
}
