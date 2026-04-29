# p0y0money Deploy Script - シンプル版
# 使用方法: powershell -ExecutionPolicy Bypass -File deploy-simple.ps1

param(
    [string]$jarFile = "build\libs\p0y0money-1.0-SNAPSHOT.jar",
    [string]$sshHost = "05.jpn.gg",
    [string]$sshUser = "rxhvoehx.b39af915",
    [string]$sshPass = "4oT|-#eI",
    [int]$sshPort = 2032,
    [string]$remotePath = "/plugins"
)

$ErrorActionPreference = "Stop"

Write-Host "======================================" -ForegroundColor Green
Write-Host "Deploy: p0y0money" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Write-Host ""

if (-not (Test-Path $jarFile)) {
    Write-Host "Error: JAR file not found: $jarFile" -ForegroundColor Red
    exit 1
}
Write-Host "JAR: $jarFile" -ForegroundColor Green

# WinSCP の場所を検索
$winscpPaths = @(
    "C:\Users\107rx\AppData\Local\Programs\WinSCP\WinSCP.com",
    "C:\Program Files (x86)\WinSCP\WinSCP.com",
    "C:\Program Files\WinSCP\WinSCP.com"
)
$winscp = $winscpPaths | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $winscp) {
    Write-Host "Error: WinSCP.com not found" -ForegroundColor Red
    exit 1
}

try {
    Write-Host ""
    Write-Host "Connecting..." -ForegroundColor Cyan

    $jarName = Split-Path $jarFile -Leaf
    $jarAbs = (Resolve-Path $jarFile).Path

    Write-Host "Uploading: $jarName" -ForegroundColor Cyan

    & $winscp /command `
        "open sftp://${sshUser}:${sshPass}@${sshHost}:${sshPort}/ -hostkey=*" `
        "put `"$jarAbs`" $remotePath/" `
        "exit"

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "======================================" -ForegroundColor Green
        Write-Host "Deploy Success!" -ForegroundColor Green
        Write-Host "======================================" -ForegroundColor Green
    } else {
        throw "WinSCP failed with exit code $LASTEXITCODE"
    }

} catch {
    Write-Host ""
    Write-Host "Error: $($_)" -ForegroundColor Red
    Write-Host "======================================" -ForegroundColor Red
    exit 1
}


