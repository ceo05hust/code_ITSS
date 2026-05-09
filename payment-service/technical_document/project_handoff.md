# 📋 AIMS Payment Service — Handoff Context

> **Mục đích:** Dán file này vào cửa sổ AI mới để tiếp tục phát triển dự án mà không mất context.

---

## 1. Tổng quan dự án

- **Tên dự án:** AIMS Payment Service
- **Repo GitHub:** `github.com/ceo05hust/code_ITSS` — branch `final`
- **Đường dẫn local:** `c:\Users\FPT\OneDrive\Documents\code_ITSS\payment-service`
- **Cấu trúc:** Monorepo gồm 2 thư mục: `backend/` và `frontend/`

### Mô tả nghiệp vụ
Hệ thống mô phỏng thanh toán VietQR cho ứng dụng bán sách AIMS.
- Khách hàng review đơn hàng → bấm "Confirm & Pay" → mã QR hiện ra → quét bằng app ngân hàng → backend nhận callback từ VietQR → lưu Invoice + TransactionInfo vào DB.
- **Thiết kế quan trọng:** Invoice **KHÔNG** được lưu vào DB khi bắt đầu thanh toán. Invoice tạm thời được lưu trong **ConcurrentHashMap** (RAM). Chỉ lưu DB khi VietQR callback xác nhận thành công. Mục đích: tránh "garbage data" do khách không thanh toán.

---

## 2. Backend (Spring Boot)

### Công nghệ
- Java 21, Spring Boot 3.x, Spring Data JPA
- PostgreSQL (`aims_payment` schema)
- Lombok, Maven
- **Port:** `8080`

### Cấu trúc thư mục quan trọng
```
backend/src/main/java/com/aims/payment_service/
├── controller/
│   ├── PayOrderController.java        ← REST API cho Angular
│   └── VietQRInboundController.java   ← Nhận webhook callback từ VietQR
├── service/
│   └── PaymentCacheService.java       ← In-memory cache (ConcurrentHashMap)
├── subsystem/vietqr/
│   ├── VietQRController.java          ← Gọi VietQR API ngoài
│   ├── VietQRBoundary.java            ← Tầng HTTP thô với VietQR
│   └── IPaymentQRCode.java            ← Interface
├── entity/
│   ├── Invoice.java
│   ├── TransactionInfo.java
│   └── QRCode.java                    ← Không phải @Entity, chỉ là DTO parse response VietQR
└── dto/
    ├── ApiResponse.java               ← Generic response wrapper
    ├── InvoiceRequest.java            ← Request body cho generate-qr
    └── CallbackPayload.java           ← Parse VietQR webhook payload
```

### API Endpoints

#### `POST /api/payment/generate-qr`
**Request Body (JSON):**
```json
{
  "invoiceId": 0,
  "shippingFee": 12000,
  "totalProductPriceExVAT": 450000,
  "totalProductPriceIncVAT": 495000,
  "totalAmount": 507000
}
```
**Response (success):**
```json
{
  "success": true,
  "message": "QR code generated",
  "data": {
    "qrCode": {
      "qrCode": "000201010211...",
      "qrLink": "https://pro.vietqr.vn/test/qr-generated?token=...",
      "bankCode": "BIDV",
      "bankName": "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam",
      "bankAccount": "5321320559"
    },
    "paymentRef": "REF-8B732B49"
  }
}
```
> **Lưu ý:** `qrCode.qrCode` là chuỗi chuẩn **EMVCo** (VietQR), KHÔNG phải base64 PNG. Dùng API ngoài để render ảnh: `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=<encodeURIComponent(qrCode)>`

#### `POST /api/payment/confirm`
**Request Body:**
```json
{ "paymentRef": "REF-8B732B49" }
```
**Response (thành công):**
```json
{
  "success": true,
  "data": { "transactionId": "123", "paymentRef": "REF-8B732B49", "status": "SUCCESS" }
}
```
**Response (timeout/chưa thanh toán):**
```json
{
  "success": true,
  "data": { "paymentRef": "REF-8B732B49", "status": "PENDING" },
  "message": "Payment pending - please wait"
}
```

#### `GET /api/payment/transaction/{paymentRef}`
Lấy thông tin giao dịch đã hoàn thành.

#### `POST /vietqr/inbound` (Webhook — KHÔNG phải từ frontend)
VietQR gọi về URL này khi thanh toán xác nhận. Backend parse `paymentRef` từ nội dung, lưu Invoice + TransactionInfo vào DB.

### PaymentCacheService
```java
// Lưu Invoice tạm khi generate-qr
paymentCacheService.addPendingInvoice(paymentRef, invoice);

// Lấy Invoice từ cache khi callback về
paymentCacheService.getPendingInvoice(paymentRef);

// Lưu kết quả thanh toán thành công (sau khi lưu DB)
paymentCacheService.markPaymentComplete(paymentRef, transactionInfo);

// Frontend poll kết quả
paymentCacheService.getCompletedPayment(paymentRef);
```

---

## 3. Frontend (Angular 21 + TypeScript)

### Công nghệ
- Angular 21 (Standalone Components), TypeScript, Vanilla CSS
- Port: `4200`
- Proxy: `/api/*` → `http://localhost:8080` (cấu hình trong `proxy.conf.json`)

### Cấu trúc thư mục
```
frontend/src/
├── styles.css                         ← Global Design System (monochrome)
└── app/
    ├── app.ts                         ← Root component (chỉ có <router-outlet>)
    ├── app.config.ts                  ← provideRouter + provideHttpClient
    ├── app.routes.ts                  ← Định nghĩa 4 routes
    ├── services/
    │   └── payment.service.ts         ← HTTP service gọi backend
    └── pages/
        ├── invoice/invoice.component.ts           ← /invoice
        ├── payment/payment.component.ts           ← /payment
        ├── payment-failed/payment-failed.component.ts  ← /payment-failed
        └── order-success/order-success.component.ts    ← /success
```

### Routes
| Path | Component | Mô tả |
|---|---|---|
| `/` | redirect → `/invoice` | |
| `/invoice` | InvoiceComponent | Review & Checkout (mock data) |
| `/payment` | PaymentComponent | QR Payment Screen (**chính**) |
| `/payment-failed` | PaymentFailedComponent | Màn hình lỗi (**chính**) |
| `/success` | OrderSuccessComponent | Order Confirmed (mock data) |

### Luồng điều hướng
```
/invoice → (CONFIRM & PAY) → /payment → (I have already paid)
    → [SUCCESS] → /success
    → [FAILED]  → /payment-failed → (Retry) → /payment
                                  → (Change Method) → /invoice
```

### Truyền dữ liệu giữa các routes
Dùng `Router.navigate` với `state`:
```typescript
// InvoiceComponent → PaymentComponent
this.router.navigate(['/payment'], {
  state: { totalAmount, shippingFee, totalProductPriceExVAT, totalProductPriceIncVAT }
});

// PaymentComponent → OrderSuccessComponent
this.router.navigate(['/success'], { state: { transactionId, totalAmount } });

// PaymentComponent → PaymentFailedComponent
this.router.navigate(['/payment-failed'], { state: { reason: 'error message' } });
```

Đọc state trong constructor:
```typescript
const nav = this.router.getCurrentNavigation();
const state = nav?.extras?.state as any;
this.totalAmount = state?.['totalAmount'] ?? 507000; // fallback nếu vào thẳng route
```

### Cách render mã QR
```typescript
// qrCode.qrCode là chuỗi EMVCo, render bằng api.qrserver.com
this.qrImageUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=' 
                + encodeURIComponent(qr.qrCode);
// Sau đó hiển thị: <img [src]="qrImageUrl"/>
```
> **Lý do không dùng thư viện:** `angularx-qrcode` không tương thích tốt với Angular 21 Standalone — đã gỡ.

---

## 4. Design System (ScreenStandardizationFramework)

### Bảng màu
| Token | Giá trị | Dùng khi |
|---|---|---|
| Background | `#FFFFFF` | Nền trang |
| Primary Text | `#000000` | Văn bản chính |
| Muted Text | `#6B7280` | Chú thích, label |
| Border | `#E5E5E5` | Viền card, input |
| Error | `#D32F2F` | Màu cảnh báo, icon lỗi |
| Error Background | `#FFF5F5` | Nền box lỗi |

### Typography
- **Font:** Inter (Google Fonts)
- H1: 24px Bold | H2: 20px Semi | Body: 14px | Helper: 12px

### Buttons
- **Primary:** Nền đen `#000000`, chữ trắng, border-radius 8px
- **Secondary:** Nền trắng, viền `#E5E5E5`, chữ đen

### Số tiền
- Dấu `.` làm phân cách nghìn: `507.000 VND`
- Căn phải (text-align: right) trong các bảng tổng tiền

---

## 5. Cài đặt & Chạy dự án

### Backend
```bash
cd backend
./mvnw spring-boot:run
# Chạy ở http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm start
# Chạy ở http://localhost:4200
```

### Ngrok (để VietQR callback về local)
```bash
ngrok http 8080
# Copy HTTPS URL → cấu hình trong application.properties
```

### Cấu hình Backend (`application.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/aims_payment
vietqr.callback.base-url=https://xxxx.ngrok.io   # URL ngrok
```

---

## 6. Trạng thái hiện tại & Việc cần làm tiếp

### Đã hoàn thành ✅
- Backend: transient invoice cache, paymentRef flow, VietQR callback, lưu DB sau thanh toán
- Frontend: 4 trang theo đúng Figma design, monochrome style, routing, API integration

### Còn đang kiểm tra 🔧
- **QR code chưa hiển thị trên frontend:** Nguyên nhân là spinner vẫn xoay sau khi backend response thành công. Đã sửa: dùng `ChangeDetectorRef.detectChanges()` + render QR bằng `api.qrserver.com`. **Cần test lại sau khi restart `npm start`.**

### Việc có thể làm tiếp 📋
1. Kiểm tra QR render đúng chưa bằng cách thử luồng `/invoice` → `/payment`
2. Test luồng thanh toán thành công: scan QR bằng app ngân hàng, xem `/success` hiển thị đúng không
3. (Optional) Thêm polling tự động (mỗi 3s gọi `getTransaction`) thay vì bấm "I have already paid"
4. (Optional) Cleanup timer cho `PaymentCacheService` để xóa các session quá 15 phút
