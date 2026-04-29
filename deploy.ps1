# p0y0money デプロイスクリプト
# 使用方法: powershell -ExecutionPolicy Bypass -File deploy.ps1

param(
    [string]$jarFile = "build\libs\p0y0money-1.0-SNAPSHOT.jar",
    [string]$host = "05.jpn.gg",
    [string]$user = "rxhvoehx",
    [int]$port = 2032,
    [string]$keyPath = "$HOME\.ssh\id_rsa",
    [string]$remotePath = "/home/container/plugins"
)

# エラーハンドリング設定
$ErrorActionPreference = "Stop"

Write-Host "===============================================" -ForegroundColor Green
Write-Host "📦 p0y0money Deploy Script" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

# JAR ファイルの確認
if (-not (Test-Path $jarFile)) {
    Write-Host "❌ エラー: JAR ファイルが見つかりません: $jarFile" -ForegroundColor Red
    Write-Host "   先に 'gradle build' を実行してください" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ JAR ファイル確認: $jarFile" -ForegroundColor Green

# SSH 鍵の確認
if (-not (Test-Path $keyPath)) {
    Write-Host "❌ エラー: SSH 鍵が見つかりません: $keyPath" -ForegroundColor Red
    Write-Host "   SSH 鍵を生成してください: ssh-keygen -t rsa" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ SSH 鍵確認: $keyPath" -ForegroundColor Green

# Posh-SSH モジュールをインストール（必要に応じて）
if (-not (Get-Module -ListAvailable -Name Posh-SSH)) {
    Write-Host "📥 Posh-SSH モジュールをインストール中..." -ForegroundColor Yellow
    Install-Module -Name Posh-SSH -Force -Scope CurrentUser -AllowClobber
}

# モジュールをインポート
Import-Module Posh-SSH

try {
    Write-Host ""
    Write-Host "🔗 SSH 接続中: $user@$host:$port" -ForegroundColor Cyan

    # SSH キーの指紋を無視して接続
    $keyObject = New-Object Renci.SshNet.PrivateKeyFile($keyPath)
    $credential = New-Object Renci.SshNet.ConnectionInfo($host, $port, $user, $keyObject)
    $sftpSession = New-SFTPSession -ConnectionInfo $credential -Verbose -WarningAction SilentlyContinue

    Write-Host "✅ SSH 接続成功" -ForegroundColor Green
    Write-Host ""

    $jarFileName = Split-Path $jarFile -Leaf
    Write-Host "📤 ファイル転送中: $jarFileName → $remotePath" -ForegroundColor Cyan

    # SFTP で転送
    Set-SFTPLocation -SFTPSession $sftpSession -Location $remotePath
    Write-Host "   リモートパス: $remotePath に移動" -ForegroundColor Gray

    # ファイルをアップロード
    Set-SFTPFile -SFTPSession $sftpSession -LocalFile $jarFile -RemoteFile $jarFileName -Overwrite

    Write-Host "✅ ファイル転送完了" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 転送情報:" -ForegroundColor Cyan
    Write-Host "   ファイル名: $jarFileName" -ForegroundColor Gray
    Write-Host "   ローカル: $jarFile" -ForegroundColor Gray
    Write-Host "   リモート: $remotePath/$jarFileName" -ForegroundColor Gray
    Write-Host ""

    # セッション終了
    $sftpSession | Remove-SSHSession

    Write-Host "✅ デプロイ完了！" -ForegroundColor Green
    Write-Host "===============================================" -ForegroundColor Green

} catch {
    Write-Host "❌ エラー: $_" -ForegroundColor Red
    Write-Host "===============================================" -ForegroundColor Red
    exit 1
}

