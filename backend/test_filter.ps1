$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"Admin@123"}'
$token = $response.data.token

try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/sales/leads/filter" -Method Post -Headers @{Authorization="Bearer $token"} -ContentType "application/json" -Body '{"filters":[{"field":"branch.id","operator":"EQUAL","value":"2"}],"page":0,"size":20}'
    Write-Output "Success"
    $res | ConvertTo-Json
} catch {
    Write-Output "HTTP Error:"
    $_.Exception.Response.StatusCode
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $reader.ReadToEnd()
}
