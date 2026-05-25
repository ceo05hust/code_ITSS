# Cẩm Nang Hiểu Nhanh Toàn Bộ Code Backend Payment Service

Tài liệu này được viết theo ngôn ngữ "bình dân" nhất dành cho người mới bắt đầu học Java/Spring Boot. Thay vì đi giải thích từng dòng của 30 file (sẽ làm bạn ngợp), chúng ta sẽ phân tích **những file xương sống quan trọng nhất** theo đúng dòng chảy của dữ liệu.

Hệ thống Spring Boot hoạt động giống hệt một **Nhà hàng**:
- **Controller (Tiếp tân):** Đón khách, ghi nhận yêu cầu.
- **Service (Bếp trưởng):** Nhận order từ tiếp tân, suy nghĩ công thức nấu ăn, điều phối công việc.
- **Subsystem / Boundary (Shipper):** Chạy ra ngoài siêu thị (VietQR) để mua nguyên liệu.
- **Entity & Repository (Nhà kho):** Lưu trữ thông tin đơn hàng vào sổ sách (Database).

---

## 1. Tầng Tiếp Tân (Controller)

Nằm trong thư mục `controller`. Nhiệm vụ của nó là hứng các đường dẫn web (URL) như `/api/payment/...`.

### `PayOrderController.java`
*   **Mục đích:** Là nơi đầu tiên đón Request từ Frontend gửi lên.
*   **Từ khóa cần nhớ:**
    *   `@RestController`: Báo cho Java biết class này làm nhiệm vụ "Tiếp tân".
    *   `@PostMapping("/generate-qr")`: Nếu khách hàng truy cập đường link có đuôi `/generate-qr` bằng phương thức POST, hãy vứt cho hàm bên dưới xử lý.
    *   `@RequestBody PaymentRequest request`: Lấy cục dữ liệu JSON mà Frontend gửi lên (chứa số tiền) và tự động "nhét" nó vào một class Java tên là `PaymentRequest`.
*   **Luồng chạy:** Tiếp tân không tự làm mã QR. Nó gọi Bếp trưởng: `paymentService.generateVietQR(request)`. Bếp trưởng làm xong, tiếp tân gói kết quả vào `ApiResponse` rồi trả cho khách.

### `VietQRWebhookController.java`
*   **Mục đích:** Tiếp tân chuyên biệt chỉ để hứng điện thoại từ Ngân hàng gọi tới (Webhook). Nó có đường link `/api/payment/webhook`.

---

## 2. Tầng Bếp Trưởng (Service)

Nằm trong thư mục `service.order`. Nơi chứa toàn bộ **Logic Nghiệp Vụ (Business Logic)**. 

### `PaymentService.java`
*   **Mục đích:** Đầu não của hệ thống.
*   **Từ khóa cần nhớ:** `@Service` (Đánh dấu đây là Bếp trưởng).
*   **Luồng chạy hàm `generateVietQR()`:**
    1. Hàm này lấy `amount` từ yêu cầu.
    2. Nó tự tạo ra một chuỗi ngẫu nhiên (UUID) bằng `UUID.randomUUID().toString()`, đây chính là mã `externalTransactionId` (Thẻ rung).
    3. Nó gọi `vietQRService.generateQRCode(...)` (sai khiến Shipper) đi sang VietQR lấy ảnh QR về.
    4. Gói cả Thẻ rung và ảnh QR trả lại cho Tiếp tân.
*   **Luồng chạy hàm `processWebhook()`:**
    1. Bóc tách thư do ngân hàng gửi về để tìm xem trong nội dung có mã `REF-...` không.
    2. Nếu có, kiểm tra trong Nhà kho (`TransactionInfoService`) xem giao dịch này đã lưu thành công bao giờ chưa để tránh bị ngân hàng báo trùng 2 lần.
    3. Nếu chưa, sai khiến Nhà kho lưu một dòng mới vào Database.

### `TransactionInfoService.java`
*   **Mục đích:** Chuyên gia quản lý Nhà kho. 
*   Nó gọi các hàm của `transactionInfoRepository` (như `.save()` để lưu vào Database, hoặc `.findByExternalTransactionId()` để tìm kiếm trong Database).

---

## 3. Tầng Shipper - Giao tiếp ngoài (Subsystem & Interfaces)

### `interfaces/IPaymentQRCode.java`
*   **Mục đích:** Đây là một "Bản hợp đồng". Bếp trưởng (`PaymentService`) bảo rằng: *"Tôi không cần biết các anh làm QR kiểu gì, tôi chỉ biết tôi cần gọi hàm `generateQRCode()`, ai ký hợp đồng này thì phải làm được hàm đó cho tôi"*.

### `subsystem/vietqr/VietQRController.java`
*   **Mục đích:** Là nhân viên VietQR đã ký hợp đồng (`implements IPaymentQRCode`). 
*   **Luồng chạy:** Khi bị Bếp trưởng sai đi lấy QR, nó biết VietQR yêu cầu phải có "Giấy phép" (Token) mới cho lấy mã. Nó liền kiểm tra Token cũ còn hạn không, nếu hết hạn nó đi lấy Token mới (`getValidAccessToken()`), sau đó đóng gói dữ liệu và gọi thằng Đưa Thư (`VietQRBoundary`).

### `subsystem/vietqr/VietQRBoundary.java`
*   **Mục đích:** Thằng Đưa Thư chuyên nghiệp (như đã giải thích rất kỹ ở phần trước). 
*   Nó dùng `RestTemplate` để đóng gói phong bì (Headers), bỏ thư vào (Body) và mang đến địa chỉ URL của VietQR trên Internet. Nhận thư phản hồi về, nếu lỗi thì nó ném Cảnh báo (`throw Exception`).

---

## 4. Tầng Nhà Kho (Entity & Repository)

### `entity/order/TransactionInfo.java`
*   **Mục đích:** Bản vẽ thiết kế của một cái "Bảng" trong Database.
*   **Từ khóa cần nhớ:**
    *   `@Entity`: Báo cho Java biết đây là bản thiết kế Database.
    *   `@Id`, `@GeneratedValue`: Tự động sinh số thứ tự (1, 2, 3) cho dòng dữ liệu.
    *   `@Column`: Các cột trong bảng (ví dụ cột `amount` lưu số tiền).

### `repository/order/TransactionInfoRepository.java`
*   **Mục đích:** Cây chổi thần kỳ của Spring Boot. Thay vì bạn phải tự viết lệnh SQL dài ngoằng (`SELECT * FROM transaction_info WHERE...`), bạn chỉ cần khai báo interface `extends JpaRepository`. Spring Boot sẽ tự động biến các lời nói của bạn thành câu lệnh SQL để nói chuyện với cơ sở dữ liệu!

---

## 5. Tầng Phụ Trợ (DTO & Exception)

### Thư mục `dto` (Data Transfer Object)
*   **Mục đích:** Cái "Cặp xách" để chở dữ liệu. Ví dụ khi Frontend gửi một cục JSON có tên người dùng, số tiền... ta không thể ném bừa phứa nó đi được. Ta phải tạo một cái cặp xách `PaymentRequest.java` có đúng các ngăn chứa (thuộc tính) tương ứng để nhét dữ liệu vào cho gọn gàng, rồi xách cái cặp đó đi qua lại giữa Tiếp tân và Bếp trưởng.

### Thư mục `exception`
*   **Mục đích:** Các "Biển báo lỗi" tự chế. Thay vì máy tính văng ra một cái lỗi khó hiểu kiểu `NullPointerException`, ta tự tạo ra các biển báo như `InvalidTokenException` (Lỗi Token không hợp lệ) hay `PaymentFailedException` (Lỗi thanh toán thất bại) để hệ thống bắt lỗi và báo ra màn hình một cách văn minh, lịch sự. Hầu hết các file này rất ngắn, chỉ có tên lỗi kế thừa từ `RuntimeException`.

---
*Hy vọng với cẩm nang "Nhà Hàng Spring Boot" này, bạn đã nhìn thấy được bức tranh toàn cảnh cực kỳ rõ ràng của đồ án!*
