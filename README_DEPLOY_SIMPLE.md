# 🚀 デプロイ自動化 - クイックスタート（修正版）

## 📋 やることリスト（初回のみ）

### ステップ 1: セットアップ実行

PowerShell を**通常で実行** して：

```powershell
cd C:\Users\107rx\Documents\poyo_work\untitled
powershell -ExecutionPolicy Bypass -File setup-simple.ps1
```

このスクリプトが自動で：
- ✅ Posh-SSH モジュールをインストール（必要な場合）
- ✅ SSH鍵を生成（必要な場合）

---

## 🎯 毎回のデプロイ方法

### 方法1: PowerShellスクリプト（推奨）

```powershell
powershell -ExecutionPolicy Bypass -File deploy-simple.ps1
```

### 方法2: Windows Batch（最も簡単）

```cmd
deploy.bat
```

エクスプローラーで `deploy.bat` をダブルクリックするだけ！

### 方法3: Gradle（要インストール）

Gradleをインストールしている場合：
```bash
gradle build deploy
```

---

## 📁 ファイル構成

| ファイル | 用途 |
|---------|------|
| `setup-simple.ps1` | **初回セットアップ**（Posh-SSH と SSH鍵） |
| `deploy-simple.ps1` | **メインのデプロイスクリプト** |
| `deploy.bat` | Windows Batch（わかりやすい） |
| `build.gradle` | Gradle ビルド設定 |
| `DEPLOY_GUIDE.md` | 詳細ガイド |

---

## 🔧 サーバー接続情報

| 項目 | 値 |
|------|-----|
| ホスト | `05.jpn.gg` |
| ユーザー | `rxhvoehx` |
| ポート | `22` |
| 転送先 | `/home/container/plugins` |
| SSH鍵 | `~/.ssh/id_rsa` |

---

## ⚠️ トラブルシューティング

### エラー: `Posh-SSH modul not found`

**解決:**
```powershell
Install-Module -Name Posh-SSH -Force -Scope CurrentUser -AllowClobber -SkipPublisherCheck
```

### エラー: `SSH key not found`

**解決:**
```powershell
ssh-keygen -t rsa -b 4096 -f $HOME\.ssh\id_rsa -N ""
```

### エラー: `Connection refused`

確認項目：
1. SSH鍵がサーバーに登録されているか
2. ユーザー名・ホストが正しいか
3. ファイアウォール設定

---

## 🎉 完成！

これで毎回 `deploy.bat` をダブルクリックするだけで、
自動的にビルド・転送されます！

質問があれば `DEPLOY_GUIDE.md` を参照してください。

