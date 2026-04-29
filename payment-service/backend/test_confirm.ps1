$body = @{
    invoiceId = "INV-001"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/payment/confirm' -Method POST -ContentType 'application/json' -Body $body
    $response | ConvertTo-Json
} catch {
    $_.Exception.Response.GetResponseStream() | %{ (New-Object System.IO.StreamReader($_)).ReadToEnd() }
}
