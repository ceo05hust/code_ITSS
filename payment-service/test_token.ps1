$authHeader = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("customer-hustaims-user26572:Y3VzdG9tZXItaHVzdGFpbXMtdXNlcjI2NTcy"))
try {
    Write-Host "Getting token..."
    $res = Invoke-RestMethod -Uri "https://dev.vietqr.org/vqr/api/token_generate" -Method POST -Headers @{ Authorization = $authHeader } -ContentType "application/json"
    $res | ConvertTo-Json
} catch {
    Write-Host "Failed: " $_.Exception.Message
    Write-Host $_.Exception.Response.GetResponseStream() | %{ (New-Object System.IO.StreamReader($_)).ReadToEnd() }
}
