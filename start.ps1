Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Student Notes AI - Starting Up..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Start Docker containers
Write-Host "[1/3] Starting Docker containers (MySQL + Qdrant)..." -ForegroundColor Yellow
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker failed to start. Is Docker Desktop running?" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "Docker containers started." -ForegroundColor Green
Write-Host ""

# Step 2: Wait for MySQL to be ready
Write-Host "Waiting 8 seconds for MySQL to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# Step 3: Start Backend in a new window
Write-Host "[2/3] Starting Backend (Spring Boot)..." -ForegroundColor Yellow
$backendCmd = "Write-Host 'BACKEND - Spring Boot' -ForegroundColor Cyan; cd 'C:\Users\HP\Desktop\student-notes-ai-assistant\backend\notesai'; mvn spring-boot:run"
Start-Process powershell -ArgumentList @("-NoExit", "-Command", $backendCmd)
Write-Host "Backend window launched." -ForegroundColor Green
Write-Host ""

# Step 4: Wait for backend to start
Write-Host "Waiting 25 seconds for backend to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 25

# Step 5: Start Frontend in a new window
Write-Host "[3/3] Starting Frontend (React + Vite)..." -ForegroundColor Yellow
$frontendCmd = "Write-Host 'FRONTEND - React Vite' -ForegroundColor Magenta; cd 'C:\Users\HP\Desktop\student-notes-ai-assistant\frontend'; npm run dev"
Start-Process powershell -ArgumentList @("-NoExit", "-Command", $frontendCmd)
Write-Host "Frontend window launched." -ForegroundColor Green
Write-Host ""

# Done
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " All services are starting!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host " Open your browser and go to:" -ForegroundColor White
Write-Host " http://localhost:5173" -ForegroundColor Yellow
Write-Host ""
Write-Host " (Wait ~30 more seconds for backend to fully load)" -ForegroundColor Gray
Write-Host ""
Read-Host "Press Enter to close this window"
