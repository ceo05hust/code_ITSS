$authHeader = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("customer-bachkhoa-user26560:Y3VzdG9tZXItYmFjaGtob2EtdXNlcjI2NTYw"))
$tokenResponse = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/api/token_generate" -Method POST -Headers @{ Authorization = $authHeader } -ContentType "application/json"
$token = $tokenResponse.access_token

$body = @{
    bankCode = "970418"
    bankName = "BIDV"
    bankAccount = "5321320559"
    userBankName = "NGUYEN MANH HUNG"
    amount = 250000
    content = "AIMS INV-001"
    qrType = 0
    orderId = "INV-001"
    transType = "C"
} | ConvertTo-Json

try {
    Write-Host "Generating QR..."
    $res = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/api/qr/generate-customer" -Method POST -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
    $res | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Failed: " $_.Exception.Response.GetResponseStream() | %{ (New-Object System.IO.StreamReader($_)).ReadToEnd() }
}
