package controller;

import dao.DocumentDao;
import model.Document;
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
        String idParam = request.getParameter("id");

        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID tài liệu!");
            return;
        }

        int docId = Integer.parseInt(idParam);
        DocumentDao docDao = new DocumentDao();
        Document doc = docDao.getById(docId);

        if (doc == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tài liệu!");
            return;
        }

        // 1. XỬ LÝ DOWNLOAD CHO ONLYOFFICE TẢI FILE
        if ("download".equals(action)) {
            // Đọc file từ thư mục ngoài
            File file = new File(getStoragePath(), doc.getPhysical_path());
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File vật lý không tồn tại!");
                return;
            }

            response.setContentType(getServletContext().getMimeType(file.getName()));
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + doc.getOriginal_name() + "\"");

            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)); 
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[1024 * 4];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return;
        }

        // 2. HIỂN THỊ GIAO DIỆN PREVIEW
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (doc.getUser_id() != currentUser.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập tài liệu này!");
            return;
        }

        request.setAttribute("document", doc);
        request.getRequestDispatcher("/WEB-INF/views/preview.jsp").forward(request, response);
    }

    // 3. XỬ LÝ CALLBACK TỪ ONLYOFFICE KHI BẤM LƯU
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
                if (temp.contains(",")) temp = temp.substring(0, temp.indexOf(","));
                if (temp.contains("}")) temp = temp.substring(0, temp.indexOf("}"));
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
                DocumentDao docDao = new DocumentDao();
                Document doc = docDao.getById(docId);

                if (doc != null && downloadUrl != null) {
                    // Lưu file đè vào thư mục ngoài ổ đĩa
                    File file = new File(getStoragePath(), doc.getPhysical_path());

                    try (InputStream in = new URL(downloadUrl).openStream(); 
                         FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024 * 4];
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