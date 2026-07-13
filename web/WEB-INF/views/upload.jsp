<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Tải lên tài liệu - EduDoc</title>
        <style>
            /* ================= RESET & CẤU TRÚC CHÍNH ================= */
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
                font-family: 'Segoe UI', Tahoma, sans-serif;
            }
            body {
                background-color: #f8f9fa;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                padding: 20px;
            }

            .upload-container {
                background: #ffffff;
                width: 100%;
                max-width: 600px;
                padding: 32px;
                border-radius: 16px;
                border: 1px solid #e0e0e0;
                box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            }
            .upload-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 24px;
                border-bottom: 1px solid #f1f3f4;
                padding-bottom: 16px;
            }
            .upload-header h2 {
                font-size: 20px;
                font-weight: 500;
                color: #1f1f1f;
            }
            .btn-back {
                color: #0b57d0;
                text-decoration: none;
                font-size: 14px;
                font-weight: 500;
            }
            .btn-back:hover {
                text-decoration: underline;
            }

            /* ================= VÙNG KÉO THẢ FILE (DRAG & DROP) ================= */
            .drop-zone {
                border: 2px dashed #a8c7fa;
                border-radius: 12px;
                background-color: #f8fafc;
                padding: 40px 20px;
                text-align: center;
                cursor: pointer;
                transition: all 0.2s ease;
                position: relative;
            }
            .drop-zone:hover, .drop-zone.drag-over {
                background-color: #ecf3fe;
                border-color: #0b57d0;
            }
            .drop-zone-icon {
                font-size: 40px;
                color: #0b57d0;
                margin-bottom: 12px;
                display: block;
            }
            .drop-zone-text {
                font-size: 15px;
                color: #444746;
                margin-bottom: 6px;
            }
            .drop-zone-hint {
                font-size: 12px;
                color: #747775;
            }
            #file-input {
                display: none;
            } /* Ẩn nút chọn file mặc định xấu xí đi */

            /* ================= DANH SÁCH FILE CHỜ UPLOAD ================= */
            .file-list-section {
                margin-top: 24px;
                display: none;
            } /* Chỉ hiện khi có file */
            .file-list-title {
                font-size: 14px;
                font-weight: 500;
                color: #5f6368;
                margin-bottom: 12px;
            }
            .file-item {
                display: flex;
                align-items: center;
                justify-content: space-between;
                background: #f0f4f9;
                padding: 12px 16px;
                border-radius: 8px;
                margin-bottom: 8px;
                font-size: 14px;
            }
            .file-info {
                display: flex;
                align-items: center;
                gap: 12px;
                width: 80%;
            }
            .file-name {
                color: #1f1f1f;
                font-weight: 500;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .file-size {
                color: #747775;
                font-size: 12px;
            }
            .btn-remove {
                background: none;
                border: none;
                color: #ea4335;
                font-weight: bold;
                cursor: pointer;
                font-size: 16px;
                padding: 0 4px;
            }

            /* ================= THANH TIẾN TRÌNH (PROGRESS BAR) ================= */
            .progress-container {
                margin-top: 16px;
                display: none;
            }
            .progress-text {
                display: flex;
                justify-content: space-between;
                font-size: 12px;
                color: #5f6368;
                margin-bottom: 6px;
            }
            .progress-bar {
                width: 100%;
                height: 6px;
                background-color: #e0e0e0;
                border-radius: 3px;
                overflow: hidden;
            }
            .progress-fill {
                height: 100%;
                background-color: #0b57d0;
                width: 0%;
                transition: width 0.2s;
            }

            /* ================= NÚT THỰC THI ================= */
            .action-area {
                margin-top: 32px;
                display: flex;
                justify-content: flex-end;
                gap: 12px;
            }
            .btn {
                padding: 10px 24px;
                border-radius: 20px;
                font-size: 14px;
                font-weight: 500;
                border: none;
                cursor: pointer;
                transition: background-color 0.2s;
            }
            .btn-cancel {
                background-color: transparent;
                color: #444746;
                border: 1px solid #747775;
            }
            .btn-cancel:hover {
                background-color: #f1f3f4;
            }
            .btn-upload {
                background-color: #0b57d0;
                color: #ffffff;
            }
            .btn-upload:hover {
                background-color: #0842a0;
            }
            .btn-upload:disabled {
                background-color: #e3e3e3;
                color: #9e9e9e;
                cursor: not-allowed;
            }
        </style>
    </head>
    <body>
        <div class="upload-container">
            <div class="upload-header">
                <h2>Tải tệp lên Drive</h2>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn-back">Quay lại Dashboard</a>
            </div>

            <form id="upload-form" action="${pageContext.request.contextPath}/DocumentServlet" method="POST" enctype="multipart/form-data">

                <div class="drop-zone" id="drop-zone" onclick="document.getElementById('file-input').click()">
                    <span class="drop-zone-icon">☁</span>
                    <p class="drop-zone-text">Kéo và thả tệp vào đây hoặc <span style="color:#0b57d0; font-weight:500;">Duyệt tệp</span></p>
                    <p class="drop-zone-hint">Hỗ trợ: PDF, DOCX, XLSX, PPTX, Hình ảnh (Không giới hạn dung lượng)</p>
                    <input type="file" name="file" id="file-input" multiple onchange="handleFileSelect(this.files)">
                </div>

                <div class="file-list-section" id="file-list-section">
                    <div class="file-list-title">Tệp đã chọn</div>
                    <div id="file-list-container"></div>
                </div>

                <div class="progress-container" id="progress-container">
                    <div class="progress-text">
                        <span id="upload-status">Đang tải lên...</span>
                        <span id="progress-percent">0%</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill" id="progress-fill"></div>
                    </div>
                </div>

                <div class="action-area">
                    <button type="button" class="btn btn-cancel" onclick="window.location.href = '${pageContext.request.contextPath}/dashboard'">Hủy bớt</button>   
                    <button type="submit" class="btn btn-upload" id="submit-btn" disabled>Bắt đầu tải lên</button>
                </div>
            </form>
        </div>

        <script>
            const dropZone = document.getElementById('drop-zone');
            const fileListSection = document.getElementById('file-list-section');
            const fileListContainer = document.getElementById('file-list-container');
            const submitBtn = document.getElementById('submit-btn');
            const form = document.getElementById('upload-form');
            const progressContainer = document.getElementById('progress-container');
            const progressFill = document.getElementById('progress-fill');
            const progressPercent = document.getElementById('progress-percent');
            const uploadStatus = document.getElementById('upload-status');

            let selectedFiles = [];

            // 1. Xử lý các sự kiện kéo thả chuột (Drag & Drop)
            ['dragenter', 'dragover'].forEach(eventName => {
                dropZone.addEventListener(eventName, (e) => {
                    e.preventDefault();
                    dropZone.classList.add('drag-over');
                }, false);
            });

            ['dragleave', 'drop'].forEach(eventName => {
                dropZone.addEventListener(eventName, (e) => {
                    e.preventDefault();
                    dropZone.classList.remove('drag-over');
                }, false);
            });

            dropZone.addEventListener('drop', (e) => {
                const dt = e.dataTransfer;
                const files = dt.files;
                handleFileSelect(files);
            });

            // 2. Hiển thị thông tin các file được chọn ra màn hình
            function handleFileSelect(files) {
                // Chuyển danh sách file mới chọn thành mảng và gộp vào mảng selectedFiles hiện có
                const newFiles = Array.from(files);

                // Tránh trùng lặp file nếu người dùng chọn trùng tên file
                newFiles.forEach(file => {
                    const isDuplicate = selectedFiles.some(f => f.name === file.name && f.size === file.size);
                    if (!isDuplicate) {
                        selectedFiles.push(file);
                    }
                });

                renderFileList();
            }

            // Hàm vẽ giao diện danh sách file dựa trên mảng selectedFiles
            function renderFileList() {
                fileListContainer.innerHTML = '';

                if (selectedFiles.length > 0) {
                    fileListSection.style.display = 'block';
                    submitBtn.disabled = false;

                    // Truyền biến index (vị trí) của từng file vào để xử lý xóa riêng lẻ
                    selectedFiles.forEach((file, index) => {
                        const sizeInMB = (file.size / (1024 * 1024)).toFixed(2);
                        const fileItem = document.createElement('div');
                        fileItem.className = 'file-item';
                        fileItem.innerHTML = `
                            <div class="file-info">
                                <span style="font-size:18px;">📄</span>
                                <div>
                                    <div class="file-name">\${file.name}</div>
                                    <div class="file-size">\${sizeInMB} MB</div>
                                </div>
                            </div>
                            <button type="button" class="btn-remove" onclick="removeSingleFile(\${index})">✕</button>
                        `;
                        fileListContainer.appendChild(fileItem);
                    });
                } else {
                    resetUploadForm();
                }
            }

            // 3. HÀM XÓA CHỈ MỘT FILE ĐƯỢC CHỌN
            function removeSingleFile(indexToRemove) {
                // Xóa 1 phần tử tại vị trí indexToRemove trong mảng dữ liệu
                selectedFiles.splice(indexToRemove, 1);

                // Cập nhật lại danh sách <input type="file"> để khi submit gửi đúng dữ liệu còn lại
                updateInputFile();

                // Vẽ lại giao diện danh sách file
                renderFileList();
            }

            // Đồng bộ lại mảng selectedFiles vào trong thẻ input HTML5
            function updateInputFile() {
                const fileInput = document.getElementById('file-input');
                const dataTransfer = new DataTransfer();

                selectedFiles.forEach(file => {
                    dataTransfer.items.add(file);
                });

                fileInput.files = dataTransfer.files; // Gán lại danh sách file mới vào input
            }

            function resetUploadForm() {
                selectedFiles = [];
                document.getElementById('file-input').value = '';
                fileListSection.style.display = 'none';
                submitBtn.disabled = true;
            }

            // 4. XỬ LÝ UPLOAD NHIỀU FILE QUA AJAX (Giữ nguyên tiến trình thật)
            form.addEventListener('submit', function (e) {
                e.preventDefault();

                if (selectedFiles.length === 0)
                    return;

                submitBtn.disabled = true;
                progressContainer.style.display = 'block';
                uploadStatus.innerText = "Đang chuẩn bị tải lên...";

                const formData = new FormData();
                selectedFiles.forEach(file => {
                    formData.append("files", file);
                });

                const xhr = new XMLHttpRequest();

                xhr.upload.addEventListener('progress', function (e) {
                    if (e.lengthComputable) {
                        const percentComplete = Math.round((e.loaded / e.total) * 100);
                        progressFill.style.width = percentComplete + '%';
                        progressPercent.innerText = percentComplete + '%';

                        if (percentComplete < 100) {
                            uploadStatus.innerText = `Đang tải lên \${selectedFiles.length} tệp...`;
                        } else {
                            uploadStatus.innerText = "Đang xử lý lưu tệp trên hệ thống...";
                        }
                    }
                });

                xhr.onreadystatechange = function () {
                    if (xhr.readyState === XMLHttpRequest.DONE) {
                        if (xhr.status === 200) {
                            // Trường hợp 1: Thành công hoàn toàn
                            uploadStatus.style.color = "#137333"; // Màu xanh lá dễ nhìn
                            uploadStatus.innerText = "Tải lên thành công hoàn toàn!";
                            setTimeout(() => {
                                window.location.href = "${pageContext.request.contextPath}/dashboard";
                            }, 1000);
                        } else {
                            // Trường hợp 2: Không thành công (Bắt các mã lỗi 401, 500, v.v.)
                            uploadStatus.style.color = "#da1212"; // Đổi màu trạng thái sang Đỏ
                            progressFill.style.backgroundColor = "#da1212"; // Đổi thanh tiến trình sang màu Đỏ hiển thị lỗi

                            if (xhr.status === 401) {
                                uploadStatus.innerText = "Lỗi: Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!";
                            } else if (xhr.status === 500) {
                                uploadStatus.innerText = "Lỗi: Hệ thống không thể lưu trữ tệp hoặc lỗi kết nối Database!";
                            } else {
                                uploadStatus.innerText = "Tải lên thất bại! Vui lòng kiểm tra lại dung lượng hoặc kết nối mạng.";
                            }

                            // Khôi phục lại trạng thái nút bấm cho phép người dùng thử lại
                            submitBtn.disabled = false;
                        }
                    }
                };

                xhr.open('POST', form.action, true);
                xhr.send(formData);
            });
        </script>
    </body>
</html>