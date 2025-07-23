# Test script for puzzle-2 endpoint
Write-Host "Testing puzzle-2 endpoint..."

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/puzzle/puzzle-2" -Method GET
    Write-Host "Response received:"
    Write-Host "Success: $($response.success)"
    Write-Host "Message: $($response.message)"
    Write-Host "Puzzle content: $($response.puzzle)"
    
    # Check if the puzzle content is the correct image URL
    $expectedUrl = "https://static.wikia.nocookie.net/villainsfanon/images/4/4e/Troll-Face-Meme-PNG.png/revision/latest?cb=20240803095204"
    if ($response.puzzle -eq $expectedUrl) {
        Write-Host "✅ SUCCESS: Puzzle-2 returns the correct image URL without HTML tags" -ForegroundColor Green
    } else {
        Write-Host "❌ FAILED: Puzzle-2 does not return the expected URL" -ForegroundColor Red
        Write-Host "Expected: $expectedUrl"
        Write-Host "Actual: $($response.puzzle)"
    }
} catch {
    Write-Host "❌ Error testing endpoint: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure the backend is running on port 8080"
} 