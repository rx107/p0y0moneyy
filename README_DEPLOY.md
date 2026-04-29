# 🚀 クイックスタート：デプロイ自動化

## セットアップ（初回のみ）

### 1️⃣ Posh-SSH をインストール

PowerShell を**管理者として**実行：

```powershell
Install-Module -Name Posh-SSH -Force -Scope CurrentUser -AllowClobber
```

### 2️⃣ SSH鍵を確認

```powershell
ls $HOME\.ssh\id_rsa
```

無い場合は生成：
```powershell
ssh-keygen -t rsa -b 4096 -f $HOME\.ssh\id_rsa
```

---

## 毎回の使用方法（以降）

### 🎯 推奨：Gradle経由でデプロイ

```bash
gradle build deploy
```

**これだけで：**
✅ コンパイル
✅ JAR生成
✅ サーバーへ自動転送
✅ 完了！

### または：PowerShellスクリプト直実行

```powershell
cd C:\Users\107rx\Documents\poyo_work\untitled
powershell -ExecutionPolicy Bypass -File deploy.ps1
```

---

## 接続情報

| 項目 | 設定値 |
|------|--------|
| サーバー | `05.jpn.gg` |
| ユーザー | `rxhvoehx` |
| 転送先 | `/home/container/plugins` |
| SSH鍵 | `~/.ssh/id_rsa` |

---

## トラブル時

| エラー | 対応 |
|--------|------|
| `Posh-SSH が見つかりません` | 上記の「セットアップ」を実行 |
| `SSH 鍵が見つかりません` | `ssh-keygen` で生成 |
| `認証に失敗` | サーバーの `authorized_keys` にSSH公開鍵を追加 |
| `gradle: コマンドが見つかりません` | PowerShellスクリプトを直実行 |

👉 詳細は `DEPLOY_GUIDE.md` を参照

