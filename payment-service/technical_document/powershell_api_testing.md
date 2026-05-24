# Hướng dẫn test API Payment bằng PowerShell

Dưới đây là các câu lệnh PowerShell (`Invoke-RestMethod`) để bạn có thể gọi thẳng vào Backend (đang chạy ở cổng 8080) và test các chức năng mà không cần dùng đến Postman hay Frontend.

Mở **PowerShell**, copy và dán từng lệnh (hoặc cụm lệnh) dưới đây rồi nhấn Enter nhé!

---

### 1. Tạo mã QR (Generate QR)
Lệnh này sẽ gọi API `/api/payment/generate-qr` để lấy mã QR thanh toán.
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/payment/generate-qr" `
    -Method Post `
    -Headers @{ "Content-Type" = "application/json" } `
    -Body '{"amount": 500000}'

$response | ConvertTo-Json -Depth 10
```
> **Lưu ý:** Chạy xong lệnh trên, bạn hãy chú ý cái `externalTransactionId` (ví dụ `REF-123456`) trong kết quả trả về để dùng cho các lệnh dưới nhé!

---

### 2. Giả lập thanh toán thành công (Simulate Payment)
Lệnh này sẽ báo cho VietQR biết để VietQR gọi ngược (Webhook) về Backend của chúng ta.
*(Nhớ thay `REF-...` bằng mã sinh ra từ bước 1)*
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/payment/simulate-payment" `
    -Method Post `
    -Headers @{ "Content-Type" = "application/json" } `
    -Body '{
        "externalTransactionId": "REF-THAY-BANG-MA-CUA-BAN",
        "amount": 500000
    }' | ConvertTo-Json -Depth 10
```

---

### 3. Kiểm tra trạng thái giao dịch (Check Transaction Status)
Dùng lệnh này để hỏi Backend xem giao dịch đã thành công chưa. 
*(Nhớ thay `REF-...` bằng mã của bạn)*
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/payment/transaction/REF-THAY-BANG-MA-CUA-BAN" `
    -Method Get | ConvertTo-Json -Depth 10
```

---

### 4. Chuyển đổi phương thức thanh toán (Switch Method)
Lệnh này mô phỏng hành động khách hàng hủy VietQR để chuyển sang thanh toán bằng cổng khác (ví dụ: PayPal).
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/payment/switch-method" `
    -Method Post `
    -Headers @{ "Content-Type" = "application/json" } `
    -Body '{
        "externalTransactionId": "REF-THAY-BANG-MA-CUA-BAN",
        "method": "PayPal"
    }' | ConvertTo-Json -Depth 10
```

---

### 5. (Phụ) Bắn thẳng Webhook (Giả lập ngân hàng gọi về)
Nếu VietQR bị lỗi không gọi Webhook về, bạn có thể tự đóng vai ngân hàng bằng lệnh này:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/payment/webhook" `
    -Method Post `
    -Headers @{ "Content-Type" = "application/json" } `
    -Body '{
        "error": 0,
        "data": [
            {
                "cUSC": "AIMS",
                "transactionRefId": "REF-THAY-BANG-MA-CUA-BAN",
                "amount": 500000,
                "content": "AIMS REF-THAY-BANG-MA-CUA-BAN"
            }
        ]
    }' | ConvertTo-Json -Depth 10
```
