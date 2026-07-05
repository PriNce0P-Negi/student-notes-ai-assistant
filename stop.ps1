Write-Host ""
Write-Host "========================================" -ForegroundColor Red
Write-Host "   Student Notes AI - Shutting Down..." -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""

Write-Host "Stopping Docker containers (MySQL + Qdrant)..." -ForegroundColor Yellow
docker-compose down
Write-Host "Docker containers stopped." -ForegroundColor Green
Write-Host ""
Write-Host "NOTE: Please also close the Backend and Frontend PowerShell windows manually." -ForegroundColor Gray
Write-Host ""
Read-Host "Press Enter to close this window"
