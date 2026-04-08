$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    filters = @()
    sorts = @()
    page = 0
    size = 20
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/sales/bookings/filter" -Method POST -Headers $headers -Body $body -UseBasicParsing
    Write-Host "Success: $($response.StatusCode)"
    Write-Host $response.Content
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody"
    }
}
