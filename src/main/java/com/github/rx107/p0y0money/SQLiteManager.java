package com.github.rx107.p0y0money;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SQLiteManager {
    private Connection connection;

    public void load(File dataFolder) {
        // プラグインのフォルダがなければ作成
        if (!dataFolder.exists()) dataFolder.mkdirs();
        File file = new File(dataFolder, "economy.db");


        try {
            // SQLiteに接続（ファイルがなければ自動作成される）
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            // テーブル作成：UUIDをキーにして、balance（残高）を保存
            try (Statement s = connection.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS economy (uuid TEXT PRIMARY KEY, balance DOUBLE)");
                // loadメソッド内のテーブル作成に追加
                s.executeUpdate("CREATE TABLE IF NOT EXISTS market (" +
                        "id INTEGER PRIMARY KEY, " +
                        "seller_uuid TEXT, " +
                        "item_data BLOB, " + // アイテムをバイナリ形式で保存
                        "price DOUBLE, " +
                        "amount INTEGER)");
            }
            // テーブル作成の直後あたりに、この1行だけ追加して一度起動する
            try (Statement s = connection.createStatement()) {
                // すでにカラムがある場合はエラーになりますが、なければ追加されます
                s.executeUpdate("ALTER TABLE market ADD COLUMN seller_name TEXT");
            } catch (SQLException e) {
                // すでにカラムがある場合は「Duplicate column」的なエラーが出るので無視してOK
                org.bukkit.Bukkit.getLogger().info("seller_nameカラムは既に存在するか、追加に失敗しました（無視してOK）");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getBalance(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT balance FROM economy WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public void setBalance(UUID uuid, double amount) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO economy (uuid, balance) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setDouble(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public Connection getConnection() {
        return connection;
    }
    public void close() {
        try { if (connection != null) connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}