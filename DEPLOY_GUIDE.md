# 📦 p0y0money デプロイ自動化ガイド

## 概要
このプロジェクトは、ビルドしたJARファイルを自動的に Agames サーバー（05.jpn.gg）にデプロイするシステムを用意しています。

## 前提条件

### 1. Posh-SSH モジュールのインストール

PowerShell を管理者として実行し、以下のコマンドでインポート：

```powershell
Install-Module -Name Posh-SSH -Force -Scope CurrentUser -AllowClobber
```

初回実行時は自動でインストール確認が出ます。

### 2. SSH認証鍵の確認

SSH鍵が以下の場所に存在することを確認してください：

- **デフォルト**: `C:\Users\107rx\.ssh\id_rsa`

※ SSH鍵の生成方法（初回時）:
```powershell
ssh-keygen -t rsa -b 4096 -f $HOME\.ssh\id_rsa
```

### 3. サーバー設定情報

チップ内に以下の情報が配置されています：

| 設定項目 | 値 |
|---------|-----|
| ホスト | `05.jpn.gg` |
| ポート | `22` |
| ユーザー | `rxhvoehx` |
| リモートパス | `/home/container/plugins` |

## 使用方法

### 方法1: 直接PowerShellスクリプトを実行

```powershell
cd C:\Users\107rx\Documents\poyo_work\untitled
powershell -ExecutionPolicy Bypass -File deploy.ps1
```

### 方法2: Gradle経由でデプロイ（推奨）

```bash
cd C:\Users\107rx\Documents\poyo_work\untitled
gradle build deploy
```

このコマンドは以下を自動で行います：
1. ✅ プロジェクトをビルド（コンパイル）
2. ✅ JARファイルを生成（`build/libs/p0y0money-1.0-SNAPSHOT.jar`）
3. ✅ PowerShell デプロイスクリプトを実行
4. ✅ JARをサーバーに転送

### 方法3: スクリプトをカスタマイズして実行

```powershell
powershell -ExecutionPolicy Bypass -File deploy.ps1 `
  -jarFile "build\libs\p0y0money-1.0-SNAPSHOT.jar" `
  -host "05.jpn.gg" `
  -user "rxhvoehx" `
  -port 22 `
  -keyPath "$HOME\.ssh\id_rsa" `
  -remotePath "/home/container/plugins"
```

## トラブルシューティング

### ❌ エラー: `Posh-SSH モジュールが見つかりません`

**解決策:**
```powershell
Install-Module -Name Posh-SSH -Force -Scope CurrentUser -AllowClobber
```

### ❌ エラー: `SSH 鍵が見つかりません`

**解決策:**
1. SSH鍵を確認: `ls $HOME\.ssh\`
2. 存在しない場合は生成:
```powershell
ssh-keygen -t rsa -b 4096 -f $HOME\.ssh\id_rsa
```

### ❌ エラー: `Gradle: コマンドが見つかりません`

**解決策:**
- Gradleをシステムにインストール、または
- 直接PowerShellスクリプトを実行:
```powershell
powershell -ExecutionPolicy Bypass -File deploy.ps1
```

### ❌ エラー: `認証に失敗しました`

**解決策:**
1. SSH鍵がサーバーに登録されているか確認
2. サーバー側で以下を実行:
```bash
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
```

## ファイル構成

```
untitled/
├── build.gradle          # Gradle設定（deploy タスク含む）
├── deploy.ps1            # PowerShell デプロイスクリプト
├── DEPLOY_GUIDE.md       # このファイル
├── build/
│   └── libs/
│       └── p0y0money-1.0-SNAPSHOT.jar  # デプロイ対象のJAR
└── src/
    └── main/
        └── java/         # ソースコード
```

## 自動化のフロー

```
変更をコミット
    ↓
`gradle build deploy` を実行
    ↓
Gradle が build.gradle の deploy タスクを実行
    ↓
deploy.ps1 スクリプトが実行
    ↓
SSH接続で Posh-SSH を使用
    ↓
SFTP でファイル転送
    ↓
サーバー上の /home/container/plugins に配置
    ↓
完了！ 🎉
```

## 次のステップ

- CI/CD パイプライン（GitHub Actions など）での自動化を検討
- コミット時の自動テスト・デプロイ
- リリースノート・バージョニング管理

---

質問やトラブルがあればお知らせください！

