# Setup Script - シンプル版
Write-Host "Starting setup..." -ForegroundColor Green
Write-Host ""

# OpenSSH の確認
Write-Host "Checking OpenSSH..." -ForegroundColor Cyan
$sshCheck = Get-Command ssh -ErrorAction SilentlyContinue
if ($sshCheck) {
    Write-Host "OpenSSH: OK" -ForegroundColor Green
} else {
    Write-Host "OpenSSH not found!" -ForegroundColor Red
    Write-Host "Windows 10/11: Go to Settings > Apps > Optional Features > Add OpenSSH Client" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# SSH 鍵の確認
Write-Host "Checking SSH key..." -ForegroundColor Cyan
$sshKey = "$HOME\.ssh\id_rsa"
if (Test-Path $sshKey) {
    Write-Host "SSH key found: $sshKey" -ForegroundColor Green
} else {
    Write-Host "SSH key not found. Generating..." -ForegroundColor Yellow

    # .ssh ディレクトリ作成
    if (-not (Test-Path "$HOME\.ssh")) {
        New-Item -ItemType Directory -Path "$HOME\.ssh" | Out-Null
    }

    # ssh-keygen で鍵生成
    ssh-keygen -t rsa -b 4096 -f $sshKey -N ""
    Write-Host "SSH key generated: $sshKey" -ForegroundColor Green
}

Write-Host ""
Write-Host "Setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next: Run deploy-simple.ps1" -ForegroundColor Cyan
Write-Host "  powershell -ExecutionPolicy Bypass -File deploy-simple.ps1" -ForegroundColor Gray


