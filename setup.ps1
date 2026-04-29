# p0y0money デプロイ自動化 セットアップスクリプト

Write-Host "================================" -ForegroundColor Green
Write-Host "🚀 セットアップ開始" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""

# 1. Gradle の確認
Write-Host "1️⃣  Gradle の確認..." -ForegroundColor Cyan
$gradleCheck = gradle --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Gradle がインストールされていません" -ForegroundColor Yellow
    Write-Host "   → 以下のいずれかを実行してください：" -ForegroundColor Yellow
    Write-Host "   - Gradle をインストール: https://gradle.org/install/" -ForegroundColor Gray
    Write-Host "   - または PowerShellスクリプトを直実行: powershell -ExecutionPolicy Bypass -File deploy.ps1" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host "✅ Gradle: OK" -ForegroundColor Green
}

# 2. Posh-SSH の確認
Write-Host "2️⃣  Posh-SSH モジュールの確認..." -ForegroundColor Cyan
if (-not (Get-Module -ListAvailable -Name Posh-SSH)) {
    Write-Host "📥 Posh-SSH をインストール中..." -ForegroundColor Yellow
    try {
        Install-Module -Name Posh-SSH -Force -Scope CurrentUser -AllowClobber
        Write-Host "✅ Posh-SSH: インストール完了" -ForegroundColor Green
    } catch {
        Write-Host "❌ Posh-SSH のインストールに失敗しました" -ForegroundColor Red
        Write-Host "   管理者権限で再実行してください" -ForegroundColor Yellow
    }
} else {
    Write-Host "✅ Posh-SSH: OK" -ForegroundColor Green
}

# 3. SSH鍵の確認
Write-Host "3️⃣  SSH鍵の確認..." -ForegroundColor Cyan
$sshKeyPath = "$HOME\.ssh\id_rsa"
if (Test-Path $sshKeyPath) {
    Write-Host "✅ SSH鍵: $sshKeyPath" -ForegroundColor Green
} else {
    Write-Host "❌ SSH鍵が見つかりません: $sshKeyPath" -ForegroundColor Red
    Write-Host "   生成中..." -ForegroundColor Yellow
    ssh-keygen -t rsa -b 4096 -f $sshKeyPath -N '""'
    Write-Host "✅ SSH鍵を生成しました" -ForegroundColor Green
    Write-Host "   公開鍵をサーバーに登録してください:" -ForegroundColor Yellow
    Write-Host "   cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys" -ForegroundColor Gray
}

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "✅ セットアップ完了！" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "📦 デプロイを実行してください：" -ForegroundColor Cyan
Write-Host "   gradle build deploy" -ForegroundColor Gray
Write-Host ""
Write-Host "または PowerShell スクリプトで：" -ForegroundColor Cyan
Write-Host "   powershell -ExecutionPolicy Bypass -File deploy.ps1" -ForegroundColor Gray
Write-Host ""

