$authHeader = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("customer-hustaims-user26572:Y3VzdG9tZXItaHVzdGFpbXMtdXNlcjI2NTcy"))
$tokenResponse = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/api/token_generate" -Method POST -Headers @{ Authorization = $authHeader } -ContentType "application/json"
$token = $tokenResponse.access_token

$body = @{
    bankAccount = "5321320559"
    content = "VQR502f05154b AIMS INV001"
    amount = 250000
    bankCode = "BIDV"
    transType = "C"
} | ConvertTo-Json

try {
    Write-Host "Testing callback..."
    $res = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/bank/api/test/transaction-callback" -Method POST -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
    $res | ConvertTo-Json
} catch {
    Write-Host "Failed: "
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $reader.ReadToEnd()
}
