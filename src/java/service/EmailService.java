package service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

public class EmailService {

    // Địa chỉ gửi mặc định của Resend (Dùng địa chỉ này nếu chưa verify domain riêng)
    private static final String DEFAULT_SENDER = "onboarding@resend.dev";

    /**
     * Lấy API Key từ Biến môi trường hoặc JVM Option
     */
    private String getApiKey() {
        // 1. Thử lấy từ Biến môi trường hệ thống (System Environment)
        String apiKey = System.getenv("RESEND_API_KEY");

        // 2. Nếu không thấy, thử lấy từ JVM Option của GlassFish (-DRESEND_API_KEY=...)
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getProperty("RESEND_API_KEY");
        }

        return (apiKey != null) ? apiKey.trim() : null;
    }

    /**
     * Cấu hình nạp TrustStore SSL/TLS mặc định của JDK cho GlassFish
     */
    private void fixSslTrustStore() {
        try {
            String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
            if (trustStorePath == null || trustStorePath.isEmpty()) {
                String defaultCacerts = System.getProperty("java.home") + "/lib/security/cacerts";
                System.setProperty("javax.net.ssl.trustStore", defaultCacerts);
                System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning SSL Config: " + e.getMessage());
        }
    }

    /**
     * Hàm chính xử lý gửi email chia sẻ tài liệu qua Resend
     * 
     * @param targetEmail Email người nhận
     * @param subject     Tiêu đề email
     * @param htmlBody    Nội dung email (dạng HTML)
     * @return boolean    true nếu gửi thành công, false nếu thất bại
     */
    public boolean sendEmailViaResend(String targetEmail, String subject, String htmlBody) {
        System.out.println("=== BAT DAU GUI MAIL VIA RESEND ===");
        System.out.println(" -> Target Email: " + targetEmail);

        // 1. Sửa lỗi SSL TrustStore nếu có
        fixSslTrustStore();

        // 2. Lấy API Key từ biến môi trường
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ LỖI: Chưa cấu hình biến môi trường 'RESEND_API_KEY'!");
            return false;
        }

        try {
            // 3. Khởi tạo SDK Resend
            Resend resend = new Resend(apiKey);

            // 4. Khởi tạo thông số Email
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(DEFAULT_SENDER)
                    .to(targetEmail)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            // 5. Thực thi gửi email
            CreateEmailResponse response = resend.emails().send(params);

            if (response != null && response.getId() != null) {
                System.out.println("✅ GỬI MAIL THÀNH CÔNG! ID: " + response.getId());
                return true;
            } else {
                System.err.println("❌ LỖI: Server Resend không phản hồi ID.");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ LỖI CRASH KHI GỌI API RESEND:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hàm bao (Wrapper) hỗ trợ việc chia sẻ tài liệu
     */
    public boolean processShareDocument(String recipientEmail, String documentTitle, String shareLink) {
        String javaHome = System.getProperty("java.home");
        System.setProperty("javax.net.ssl.trustStore", javaHome + "/lib/security/cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        String subject = "Tài liệu giảng dạy được chia sẻ với bạn: " + documentTitle;
        String htmlBody = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                + "<h2>Bạn nhận được một tài liệu mới!</h2>"
                + "<p>Tài liệu: <strong>" + documentTitle + "</strong></p>"
                + "<p>Vui lòng nhấn vào liên kết bên dưới để truy cập:</p>"
                + "<p><a href='" + shareLink + "' style='background-color: #007bff; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; display: inline-block;'>Xem Tài Liệu</a></p>"
                + "<hr><p style='font-size: 12px; color: #777;'>Email này được gửi tự động từ Hệ thống Quản lý Tài liệu Giảng dạy.</p>"
                + "</div>";

        return sendEmailViaResend(recipientEmail, subject, htmlBody);
    }
}