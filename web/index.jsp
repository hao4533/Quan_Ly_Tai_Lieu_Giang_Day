<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>EduDoc - Hệ Thống Quản Lý Tài Liệu Giảng Dạy</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
        <link rel="stylesheet" href="assets/css/style_index.css">        
    </head>
    <body>

        <nav class="navbar">
            <div class="container">
                <a class="navbar-brand" href="${pageContext.request.contextPath}/">
                    <i class="bi bi-folder-fill"></i>
                    <span>EduDoc Manager</span>
                </a>

                <button class="navbar-toggler" onclick="toggleMenu()" aria-label="Toggle navigation">
                    <i class="bi bi-list"></i>
                </button>

                <div class="navbar-menu" id="navbarNav">
                    <ul class="navbar-nav">
                        <li><a class="nav-link active" href="#features">Tính năng</a></li>
                        <li><a class="nav-link" href="#contact">Liên hệ</a></li>
                    </ul>
                    <div class="navbar-actions">
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Đăng nhập</a>
                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary">Đăng ký</a>
                    </div>
                </div>
            </div>
        </nav>

        <header class="hero-section">
            <div class="container">
                <div class="hero-grid">
                    <div class="hero-content">
                        <span class="badge-success">Giải pháp số cho Giáo dục 2026</span>
                        <h1 class="hero-title">Quản Lý Tài Liệu Giảng Dạy<br>Khoa Học & Thông Minh</h1>
                        <p class="hero-desc">Hệ thống lưu trữ, phân loại bài giảng, đề thi và giáo trình trực quan dành riêng cho Giảng viên. Giúp tối ưu hóa thời gian chuẩn bị lên lớp và chia sẻ học liệu dễ dàng.</p>
                        <div class="hero-buttons">
                            <a href="#features" class="btn btn-light">Tìm hiểu thêm</a>
                            <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light">Bắt đầu ngay</a>
                        </div>
                    </div>
                    <div class="hero-image-box">
                        <i class="bi bi-cloud-arrow-up-fill"></i>
                    </div>
                </div>
            </div>
        </header>

        <section id="features" class="features-section">
            <div class="container">
                <div class="section-header">
                    <h2>Tại sao nên chọn EduDoc Manager?</h2>
                    <p>Được thiết kế tinh gọn nhằm đáp ứng chính xác nhu cầu lưu trữ và quản lý chuyên môn của Thầy/Cô.</p>
                </div>

                <div class="features-grid">
                    <div class="feature-card">
                        <div class="icon-box"><i class="bi bi-folder2-open"></i></div>
                        <h5 class="card-title">Phân loại thông minh</h5>
                        <p class="card-text">Tự động sắp xếp tài liệu chuyên nghiệp theo cấu trúc hình cây thư mục học phần và quản lý tiện lợi.</p>
                    </div>
                    <div class="feature-card">
                        <div class="icon-box"><i class="bi bi-lightning-charge"></i></div>
                        <h5 class="card-title">Đọc File Trực Tuyến</h5>
                        <p class="card-text">Hỗ trợ đọc trực tiếp nội dung các tài liệu văn bản (Word, Excel, PowerPoint) ngay trên môi trường web.</p>
                    </div>
                    <div class="feature-card">
                        <div class="icon-box"><i class="bi bi-share"></i></div>
                        <h5 class="card-title">Chia sẻ qua Email</h5>
                        <p class="card-text">Tích hợp dịch vụ gửi Mail tự động, cho phép giảng viên chuyển học liệu tới người học chỉ với một cú click.</p>
                    </div>
                </div>
            </div>
        </section>

        <footer id="contact">
            <div class="container">
                <p>&copy; 2026 EduDoc Manager. Tất cả các quyền được bảo lưu.</p>
            </div>
        </footer>

        <script>
            function toggleMenu() {
                var menu = document.getElementById("navbarNav");
                if (menu.classList.contains("show")) {
                    menu.classList.remove("show");
                } else {
                    menu.classList.add("show");
                }
            }
        </script>
    </body>
</html>