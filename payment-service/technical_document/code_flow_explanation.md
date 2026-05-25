# Giải thích chi tiết Luồng thực thi Code (Code Flow) - Payment Service

Tài liệu này giải thích chi tiết từng bước (step-by-step) cách dòng code chạy trong hệ thống từ lúc khách hàng ấn thanh toán cho đến khi hệ thống ghi nhận thành công.

Hệ thống có 3 "diễn viên" chính:
1. **Frontend (FE):** Giao diện web/app của khách hàng.
2. **Backend (BE):** Máy chủ Payment Service của chúng ta.
3. **VietQR / Bank:** Hệ thống tạo QR và xử lý giao dịch của ngân hàng.

---

## Luồng 1: Khởi tạo mã QR (Generate QR Code)

**Mục đích:** Khách hàng ấn nút thanh toán, hệ thống cần tạo ra một ảnh QR để khách quét.

1. **Frontend:** Gửi một API Request `POST /api/payment/generate-qr` mang theo số tiền (ví dụ: `{"amount": 500000}`).
2. **Backend (Controller):** Request đập vào hàm `generateQRCode()` trong `PayOrderController.java`.
3. **Backend (Service):** Controller gọi tiếp sang `PaymentService.generateVietQR()`. Tại đây, Service sẽ tự động sinh ra một mã ngẫu nhiên duy nhất gọi là `externalTransactionId` (ví dụ: `REF-A1B2C3`). Mã này chính là "biên lai tạm" để giao tiếp với ngân hàng.
4. **Backend (Subsystem):** Service đẩy mã `REF-A1B2C3` và số tiền xuống tầng dưới cùng là `VietQRController.java` (thông qua interface `IPaymentQRCode`). 
5. **Giao tiếp ngoại mạng (Boundary):** `VietQRController` lấy Access Token, rồi nhờ `VietQRBoundary` gọi API HTTP (qua internet) sang máy chủ của VietQR. Trong nội dung chuyển khoản gửi sang VietQR, BE cố tình nhét chữ `REF-A1B2C3` vào.
6. **VietQR trả lời:** VietQR tạo xong ảnh QR và trả về URL ảnh cho hệ thống của ta.
7. **Trả kết quả về Frontend:** BE lấy URL ảnh đó, kèm theo mã `REF-A1B2C3`, trả về cho FE hiển thị lên màn hình.
*(Lưu ý: Lúc này trong Database của ta vẫn TRỐNG TRƠN, chưa có giao dịch nào được lưu cả).*

---

## Luồng 2: Lắng nghe Ngân hàng báo tiền về (Webhook)

**Mục đích:** Khi khách hàng quét mã và chuyển tiền thành công, ngân hàng sẽ "báo mộng" (gọi ngược API) cho Backend của ta biết.

1. **Khách hàng thao tác:** Khách cầm điện thoại quét mã QR. Trong app ngân hàng của khách lúc này tự động điền sẵn số tiền và lời nhắn là *"AIMS REF-A1B2C3"*. Khách ấn chuyển tiền.
2. **Ngân hàng gọi Webhook:** VietQR nhận được tiền, nó thấy trong hệ thống của nó có khai báo sẵn một cái link Webhook của ta. Nó liền bắn một cái HTTP Request POST vào đường dẫn `POST /api/payment/webhook` của ta.
3. **Backend (Controller):** `VietQRWebhookController.java` đón nhận Request này.
4. **Backend (Service):** Controller đẩy dữ liệu xuống `PaymentService.processWebhook()`. Service sẽ dùng kỹ thuật trích xuất chuỗi (Regex/Substring) để bóc tách lời nhắn giao dịch và móc ra được cái mã `REF-A1B2C3`.
5. **Lưu Database:** Service gọi `TransactionInfoService.createTransactionInfo()`. Lúc này, hệ thống mới chính thức INSERT một dòng vào bảng `transaction_info` trong Database, lưu lại số tiền, thời gian, phương thức thanh toán, và quan trọng nhất là gắn cái cờ `externalTransactionId = REF-A1B2C3` vào.

---

## Luồng 3: Frontend liên tục hỏi thăm trạng thái (Polling)

**Mục đích:** FE hiển thị QR xong, nó không thể ngồi im chờ, nó phải biết khi nào khách trả tiền xong để tự động nhảy sang trang "Thành công". Nhưng FE không nhận được Webhook (vì Webhook chỉ gọi vào BE).

1. **Frontend (Vòng lặp):** Cứ mỗi 3 giây, FE lại gọi API `GET /api/payment/transaction/REF-A1B2C3` lên Backend. (Hành động hỏi liên tục này gọi là Polling).
2. **Backend (Controller):** Request đập vào `PayOrderController.getTransactionStatus()`.
3. **Backend (Kiểm tra DB):** Controller nhờ `TransactionInfoService` lôi cái mã `REF-A1B2C3` vào Database để tìm kiếm (`findByExternalTransactionId`).
4. **Trả kết quả:**
   - **Tình huống 1 (Khách chưa trả tiền):** Luồng 2 (Webhook) chưa xảy ra -> Database trống trơn -> BE không tìm thấy dòng nào -> BE trả lời FE là *"Trạng thái PENDING (Đang chờ)"*. FE tiếp tục quay tròn và 3 giây sau hỏi lại.
   - **Tình huống 2 (Khách vừa trả tiền xong):** Luồng 2 (Webhook) vừa chạy xong -> Database đã có một dòng chứa mã `REF-A1B2C3` -> BE tìm thấy dữ liệu -> BE trả lời FE là *"Trạng thái SUCCESS (Thành công)"*.
5. **Frontend:** FE nhận được chữ SUCCESS, lập tức ngừng vòng lặp, tắt hình ảnh QR và chuyển hướng người dùng sang trang thông báo Đặt hàng thành công!
