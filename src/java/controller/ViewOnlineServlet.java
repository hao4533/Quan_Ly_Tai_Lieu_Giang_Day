package controller;

import dao.DocumentDao;
import dao.ShareDao;
import model.Document;
import model.Share;
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
import java.sql.Timestamp;
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

    private static final String EXTERNAL_STORAGE_DIR = "C:" + File.separator + "app_data" + File.separator + "uploads";

    private final ShareDao shareDao = new ShareDao();
    private final DocumentDao docDao = new DocumentDao();

    private String getStoragePath() {
        File folder = new File(EXTERNAL_STORAGE_DIR);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return EXTERNAL_STORAGE_DIR;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // =========================================================================
        // 1. CHO PHÉP ONLYOFFICE TẢI FILE BẰNG TOKEN (Không cần Session)
        // =========================================================================
        if ("download_by_token".equals(action)) {
            String token = request.getParameter("token");
            if (token == null || token.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu token!");
                return;
            }

            Share share = shareDao.getByToken(token);
            if (share == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Token không tồn tại!");
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (share.getExpireAt() != null && share.getExpireAt().before(now)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Link đã hết hạn!");
                return;
            }

            Document doc = docDao.getById(share.getDocumentId());
            if (doc == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu không tồn tại!");
                return;
            }

            streamFileToClient(response, doc);
            return;
        }

        // =========================================================================
        // 2. CHO PHÉP ONLYOFFICE TẢI FILE BẰNG ID TÀI LIỆU (Không cần Session)
        // =========================================================================
        if ("download".equals(action)) {
            String idParam = request.getParameter("id");
            if (idParam == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID tài liệu!");
                return;
            }
            int docId = Integer.parseInt(idParam);
            Document doc = docDao.getById(docId);
            if (doc == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tài liệu không tồn tại!");
                return;
            }

            streamFileToClient(response, doc);
            return;
        }

        // =========================================================================
        // 3. HIỂN THỊ TRANG PREVIEW CHO CHỦ SỞ HỮU (Cần Kiểm Tra Session Login)
        // =========================================================================
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID tài liệu!");
            return;
        }

        int docId = Integer.parseInt(idParam);
        Document doc = docDao.getById(docId);

        if (doc == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu trong CSDL!");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        boolean isOwner = (doc.getUser_id() == currentUser.getId());

        if (!isOwner) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem trực tiếp tài liệu này!");
            return;
        }

        request.setAttribute("document", doc);
        request.setAttribute("isOwner", isOwner);
        request.setAttribute("shareToken", ""); // Token rỗng dành cho chủ sở hữu

        request.getRequestDispatcher("/WEB-INF/views/preview.jsp").forward(request, response);
    }

    // Hàm đọc ghi File nhị phân truyền về OnlyOffice
    private void streamFileToClient(HttpServletResponse response, Document doc) throws IOException {
        File file = new File(getStoragePath(), doc.getPhysical_path());
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File vật lý không tồn tại trên ổ đĩa!");
            return;
        }

        response.reset();
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(file.length());

        // 🔴 CẦN THIẾT DÀNH CHO ONLYOFFICE (CORS Headers)
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + doc.getOriginal_name() + "\"");

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)); OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024 * 8];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    // Callback nhận dữ liệu chỉnh sửa lưu đè từ OnlyOffice
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        try {
            Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";

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
            if (idParam != null && (status == 2 || status == 3)) {
                int docId = Integer.parseInt(idParam);
                Document doc = docDao.getById(docId);

                if (doc != null && downloadUrl != null) {
                    File file = new File(getStoragePath(), doc.getPhysical_path());

                    try (InputStream in = new URL(downloadUrl).openStream(); FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024 * 8];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }

                    doc.setFile_size_bytes(file.length());
                    doc.setUpdated_at(LocalDateTime.now());
                    docDao.update(doc);
                }
            }

            out.write("{\"error\":0}");
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"error\":1, \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
