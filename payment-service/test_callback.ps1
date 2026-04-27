$authHeader = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("customer-bachkhoa-user26560:Y3VzdG9tZXItYmFjaGtob2EtdXNlcjI2NTYw"))
$tokenResponse = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/api/token_generate" -Method POST -Headers @{ Authorization = $authHeader } -ContentType "application/json"
$token = $tokenResponse.access_token

function Test-Callback ($code) {
    $body = @{
        bankAccount = "5321320559"
        content = "AIMS INV-001"
        amount = 250000
        bankCode = $code
        transType = "C"
    } | ConvertTo-Json

    try {
        Write-Host "Testing with bankCode $code..."
        $res = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/bank/api/test/transaction-callback" -Method POST -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
        $res | ConvertTo-Json
    } catch {
        Write-Host "$code Failed: "
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $reader.ReadToEnd()
    }
}

Test-Callback "BIDV"
Test-Callback "970418"
Test-Callback "MB"
