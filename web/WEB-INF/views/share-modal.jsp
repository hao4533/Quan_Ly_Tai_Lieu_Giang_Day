<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Modal Bootstrap/Custom UI Chia Sẻ Tài Liệu -->
<div class="modal-overlay" id="shareModal" onclick="closeShareModal(event)">
    <div class="modal-box" onclick="event.stopPropagation()" style="max-width: 480px; width: 90%;">
        <div class="modal-header">
            <h3 class="modal-title"><i class="bi bi-people-fill" style="color: #0b57d0; margin-right: 8px;"></i>Chia sẻ tài liệu</h3>
            <button class="modal-close" onclick="closeShareModal()">✕</button>
        </div>
        
        <form id="shareForm" onsubmit="submitShareForm(event)">
            <input type="hidden" id="shareDocumentId" name="documentId">
            
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-size: 13px; font-weight: 600; color: #444746; margin-bottom: 6px;">Tên tài liệu chia sẻ</label>
                <input type="text" id="shareDocumentName" readonly style="width: 100%; padding: 8px 12px; border: 1px solid #dadce0; border-radius: 4px; background-color: #f8f9fa; color: #3c4043; font-size: 14px; box-sizing: border-box;">
            </div>

            <div style="margin-bottom: 20px;">
                <label for="recipientEmail" style="display: block; font-size: 13px; font-weight: 600; color: #444746; margin-bottom: 6px;">Nhập Email người nhận <span style="color: red;">*</span></label>
                <input type="email" id="recipientEmail" name="recipientEmail" required placeholder="nhapemail@domain.com" style="width: 100%; padding: 10px 12px; border: 1px solid #1a73e8; border-radius: 4px; font-size: 14px; box-sizing: border-box; outline: none;">
            </div>

            <div id="shareAlert" style="display: none; padding: 10px; border-radius: 4px; font-size: 13px; margin-bottom: 16px;"></div>

            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <button type="button" onclick="closeShareModal()" style="padding: 8px 16px; border: 1px solid #dadce0; background: #fff; border-radius: 4px; font-size: 14px; cursor: pointer; color: #3c4043;">Hủy</button>
                <button type="submit" id="btnSubmitShare" style="padding: 8px 20px; border: none; background: #0b57d0; color: white; border-radius: 4px; font-size: 14px; font-weight: 500; cursor: pointer;">
                    <span id="btnShareText">Gửi chia sẻ</span>
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    function openShareModal(docId, docName) {
        document.getElementById('shareDocumentId').value = docId;
        document.getElementById('shareDocumentName').value = docName;
        document.getElementById('recipientEmail').value = '';
        
        const alertBox = document.getElementById('shareAlert');
        alertBox.style.display = 'none';
        
        document.getElementById('shareModal').classList.add('active');
    }

    function closeShareModal(e) {
        if (!e || e.target === document.getElementById('shareModal') || e.type === 'click') {
            document.getElementById('shareModal').classList.remove('active');
        }
    }

    function submitShareForm(event) {
        event.preventDefault();
        
        const docId = document.getElementById('shareDocumentId').value;
        const recipientEmail = document.getElementById('recipientEmail').value;
        const alertBox = document.getElementById('shareAlert');
        const btnText = document.getElementById('btnShareText');
        const btnSubmit = document.getElementById('btnSubmitShare');

        btnSubmit.disabled = true;
        btnText.innerText = 'Đang gửi...';
        alertBox.style.display = 'none';

        const params = new URLSearchParams();
        params.append('documentId', docId);
        params.append('recipientEmail', recipientEmail);

        fetch('${pageContext.request.contextPath}/share', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            body: params
        })
        .then(response => response.json().then(data => ({ status: response.status, body: data })))
        .then(res => {
            alertBox.style.display = 'block';
            if (res.status === 200 && res.body.status === 'success') {
                alertBox.style.backgroundColor = '#e6f4ea';
                alertBox.style.color = '#137333';
                alertBox.innerText = res.body.message || 'Đã gửi email chia sẻ thành công!';
                setTimeout(() => {
                    closeShareModal();
                }, 1500);
            } else {
                alertBox.style.backgroundColor = '#fce8e6';
                alertBox.style.color = '#c5221f';
                alertBox.innerText = res.body.message || 'Có lỗi xảy ra, vui lòng thử lại.';
            }
        })
        .catch(() => {
            alertBox.style.display = 'block';
            alertBox.style.backgroundColor = '#fce8e6';
            alertBox.style.color = '#c5221f';
            alertBox.innerText = 'Lỗi kết nối máy chủ!';
        })
        .finally(() => {
            btnSubmit.disabled = false;
            btnText.innerText = 'Gửi chia sẻ';
        });
    }
</script>