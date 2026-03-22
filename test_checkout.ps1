$baseUrl = "https://prm392-xb7s.onrender.com/api"
$username = "testuser_$(Get-Random)"
$password = "Test@123"
$email = "$username@test.com"
$phone = "0123456789"

Write-Host "Registering $username..."
$regBody = @{
    username = $username
    password = $password
    email = $email
    phoneNumber = $phone
} | ConvertTo-Json

$regResponse = Invoke-WebRequest -Uri "$baseUrl/Auth/register" -Method Post -Body $regBody -ContentType "application/json" -UseBasicParsing
Write-Host "Register output: $($regResponse.Content)"

Write-Host "Logging in..."
$loginBody = @{
    username = $username
    password = $password
} | ConvertTo-Json

$loginResponse = Invoke-WebRequest -Uri "$baseUrl/Auth/login" -Method Post -Body $loginBody -ContentType "application/json" -UseBasicParsing
$token = ($loginResponse.Content | ConvertFrom-Json).token
Write-Host "Token obtained."

$headers = @{
    "Authorization" = "Bearer $token"
}

Write-Host "Adding item 1 to cart..."
$addCartBody = @{
    productId = 1
    quantity = 1
} | ConvertTo-Json
$addResponse = Invoke-WebRequest -Uri "$baseUrl/Cart/items" -Method Post -Headers $headers -Body $addCartBody -ContentType "application/json" -UseBasicParsing
Write-Host "Add to cart output: $($addResponse.Content)"

Write-Host "Creating Order..."
$orderBody = @{
    billingAddress = "123 Test Street"
    paymentMethod = "COD"
} | ConvertTo-Json

try {
    $orderResponse = Invoke-WebRequest -Uri "$baseUrl/Order" -Method Post -Headers $headers -Body $orderBody -ContentType "application/json" -UseBasicParsing
    Write-Host "Order created successfully! Output: $($orderResponse.Content)"
} catch {
    Write-Host "Order creation failed!"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $errorMsg = $reader.ReadToEnd()
    Write-Host "Error Body: $errorMsg"
}
