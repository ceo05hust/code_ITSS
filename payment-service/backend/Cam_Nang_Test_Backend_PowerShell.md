# CẨM NANG TEST BACKEND PAYMENT SERVICE BẰNG POWERSHELL
*(Dùng để test API Backend độc lập khi Frontend chưa code xong hoặc đang bị lỗi)*

---

## MỞ BÀI: Yêu cầu chuẩn bị
- Đảm bảo Backend (Spring Boot) đang chạy ở cổng `8080`.
- Mở **PowerShell** trên Windows để chạy các lệnh bên dưới.
- Luồng test chuẩn: **Tạo QR (lấy ID) -> Bắn Webhook giả lập -> Kiểm tra trạng thái.**

---

## BƯỚC 1: TẠO MÃ QR (GENERATE QR)
Lệnh này sẽ mô phỏng việc bấm "Xác nhận đặt hàng". Backend sẽ tự động tạo một hoá đơn mới trong Database, sau đó sang VietQR lấy mã QR về.

**Script PowerShell:**
```powershell
$bodyQR = @{
    invoiceId = 0   # Để 0 để Backend tự tạo ID mới tự tăng (SERIAL)
    shippingFee = 30000
    totalProductPriceExVAT = 200000
    totalProductPriceIncVAT = 220000
    totalAmount = 250000
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/payment/generate-qr" `
    -Method Post `
    -ContentType "application/json" `
    -Body $bodyQR

# Hiển thị kết quả ra màn hình
$response.data | Format-List
```
> **Lưu ý quan trọng:** Sau khi chạy xong, hãy nhìn vào kết quả in ra màn hình để xem `invoiceId` được Backend cấp là số mấy (Ví dụ: `invoiceId : 6`). Bạn sẽ dùng số này cho các bước tiếp theo.

---

## BƯỚC 2: GIẢ LẬP WEBHOOK VIETQR BÁO THÀNH CÔNG
Trong thực tế, khách hàng quét QR xong thì VietQR sẽ gọi vào endpoint này. Khi test, ta dùng PowerShell bắn thẳng vào endpoint này để báo "Đã nhận được tiền".

**Script PowerShell:**
```powershell
$bodyWebhook = @{
    transactionid = "VQR-TEST-" + (Get-Random -Minimum 1000 -Maximum 9999)
    transactionRefId = "6"   # <--- ĐIỀN ĐÚNG SỐ invoiceId LẤY ĐƯỢC Ở BƯỚC 1 VÀO ĐÂY
    amount = 250000
    content = "Thanh toan AIMS"
    status = "00"            # 00 = Thành công
    message = "Giao dich thanh cong"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/vqr/bank/api/test/transaction-callback" `
    -Method Post `
    -Headers @{
        "Authorization" = "Bearer dummy_token_123" # Bắt buộc phải có token này để bảo mật
        "Content-Type" = "application/json"
    } `
    -Body $bodyWebhook
```
> Nếu kết quả trả về là `success: true` thì Database đã ghi nhận giao dịch thành công.

---

## BƯỚC 3: XÁC NHẬN ĐƠN HÀNG / KIỂM TRA TRẠNG THÁI
Đây là lệnh mô phỏng việc Frontend hỏi Backend "Anh ơi đơn này đã được thanh toán chưa?".

### Cách 1: Gọi API Xác nhận (Confirm)
Đây là hàm dùng khi khách bấm nút "Tôi đã chuyển tiền xong".

**Script PowerShell:**
```powershell
$bodyConfirm = @{
    invoiceId = 6   # <--- ĐIỀN SỐ invoiceId
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/payment/confirm" `
    -Method Post `
    -ContentType "application/json" `
    -Body $bodyConfirm
```
> Trạng thái trả về sẽ là `Invoice already paid` nếu Bước 2 đã làm thành công.

### Cách 2: Trực tiếp lấy thông tin Giao dịch (Transaction)
Mô phỏng hàm Frontend dùng để hiển thị biên lai sau khi thanh toán xong.

**Script PowerShell:**
```powershell
# Sửa số 6 ở cuối link thành số invoiceId thực tế của bạn
$txn = Invoke-RestMethod -Uri "http://localhost:8080/api/payment/transaction/6" -Method Get
$txn.data | Format-List
```
> Nếu trả về đầy đủ `transactionId`, `amount`, `paymentMethod` và `status = SUCCESS` tức là luồng Backend hoạt động hoàn hảo 100% không cần Frontend!
---

## BƯỚC 4: KÍCH HOẠT CALLBACK TỰ ĐỘNG TỪ VIETQR SANDBOX (TEST CAO CẤP)
Thay vì tự bắn vào Local, ta gọi API của VietQR để nhờ họ bắn Webhook về cho mình. Cách này giúp test được cả Ngrok và logic xác thực Token thật.

**Script PowerShell:**
```powershell
# 1. Lấy Token từ VietQR (Dùng tài khoản trong application.properties)
$user = "customer-hustaims-user26572"
$pass = "Y3VzdG9tZXItaHVzdGFpbXMtdXNlcjI2NTcy"
$pair = "$($user):$($pass)"
$encoded = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($pair))

$tokenRes = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/api/token_generate" `
    -Method Post -Headers @{ Authorization = "Basic $encoded" }

$accessToken = $tokenRes.access_token

# 2. Gửi lệnh yêu cầu VietQR bắn Callback về Server mình
$bodyTrigger = @{
    bankAccount = "5321320559" # Số tài khoản nhận tiền của bạn
    amount = 250000
    content = "VQRxxxx AIMS 6" # <--- QUAN TRỌNG: Thay VQRxxxx bằng mã nhận được khi tạo QR
    bankCode = "970418"       # Mã ngân hàng (BIDV)
    transType = "C"           # C = Credit (Nhận tiền)
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/bank/api/test/transaction-callback" `
    -Method Post `
    -Headers @{ Authorization = "Bearer $accessToken" } `
    -ContentType "application/json" `
    -Body $bodyTrigger
```

> **Mẹo:** Trong phần `content`, chuỗi `VQRxxxx` giúp Sandbox định danh giao dịch. Nếu bạn dùng đúng mã QR vừa sinh ra, VietQR sẽ bắn Webhook về Ngrok của bạn ngay lập tức!
