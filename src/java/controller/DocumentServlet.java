package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.util.Collection;


@WebServlet(name = "DocumentServlet", urlPatterns = {"/DocumentServlet"})
@MultipartConfig(
    maxFileSize = -1L,
    maxRequestSize = -1L
)
public class DocumentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Lấy toàn bộ danh sách các part được gửi lên
        Collection<Part> parts = request.getParts();
        
        for (Part part : parts) {
            // Kiểm tra đúng phần dữ liệu có tên tham số là "files" và có chứa file thật
            if ("files".equals(part.getName()) && part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty()) {
                String fileName = part.getSubmittedFileName();
                
                // Xử lý lưu từng file vào thư mục lưu trữ của bạn
                // part.write(uploadPath + File.separator + fileName);
                System.out.println("Đã lưu tệp: " + fileName);
            }
        }
        
        // Phản hồi về cho AJAX trạng thái thành công (HTTP 200 OK)
        response.setStatus(HttpServletResponse.SC_OK);
    }
}