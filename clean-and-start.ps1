# PowerShell script to clean and start the app
Write-Host "Cleaning node_modules..." -ForegroundColor Yellow
Remove-Item -Recurse -Force node_modules -ErrorAction SilentlyContinue
Write-Host "Cleaning package-lock.json..." -ForegroundColor Yellow
Remove-Item package-lock.json -ErrorAction SilentlyContinue
Write-Host "Cleaning .expo cache..." -ForegroundColor Yellow
Remove-Item -Recurse -Force .expo -ErrorAction SilentlyContinue
Write-Host "Installing dependencies..." -ForegroundColor Green
npm install
Write-Host "Starting Expo with cleared cache..." -ForegroundColor Green
npx expo start --clear
