package com.github.rx107.p0y0money;

import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;

@SuppressWarnings("all")
public class MarketCommand implements BasicCommand {
    // モード定数
    private static final String MODE_MARKET = "market";           // 通常のマーケットコマンド
    private static final String MODE_INTERNAL = "internal";       // 内部用コマンド（Click Event用）
    
    // Discord設定
    private static final String DISCORD_CHANNEL_ID = "1451477356542955540";  // 出品通知を送信するDiscordチャンネルID
    
    // GUI設定
    private static final int MARKET_GUI_SIZE = 54;  // マーケットGUIの行数（54 = 6行×9列）
    
    // ID生成用定数
    private static final int ID_MIN = 1000;         // 出品番号の最小値
    private static final int ID_MAX_RANGE = 9000;   // 出品番号の範囲（1000～9999）
    
    // エラーメッセージ定数
    private static final String ERROR_ITEM_NOT_HELD = "§cアイテムを手に持ってください。";
    private static final String ERROR_POSITIVE_NUMBER = "§c正の数値を入力してください。";
    private static final String ERROR_INSUFFICIENT_ITEMS = "§c持っているアイテムの個数が足りません。";
    private static final String ERROR_ITEM_NUMBER_NOT_FOUND = "§cその出品番号は存在しません！";
    private static final String ERROR_INSUFFICIENT_BALANCE = "§cお金が足りません！";
    private static final String ERROR_INVENTORY_FULL = "§cインベントリの中がいっぱいです！";
    private static final String ERROR_LISTING_ENDED = "§cその出品は既に終了しているか、存在しません。";
    private static final String ERROR_INVALID_NUMBER_FORMAT = "§c個数と値段は半角数字で入力してください。";
    private static final String ERROR_DATABASE = "§cデータベースエラーが発生しました。";
    
    // 成功メッセージ定数
    private static final String SUCCESS_LISTED = "§a出品しました！ 出品番号: §e";
    private static final String SUCCESS_PURCHASED = "§a購入しました！";
    private static final String CANCELLED_MESSAGE = "§7[キャンセルしました。]";

    private final Economy eco;
    private final SQLiteManager db;
    private final String mode;

    /**
     * コンストラクタ
     * @param eco 経済システム（Vault）
     * @param db SQLiteデータベース管理
     * @param mode コマンドモード（"market" または "internal"）
     */
    public MarketCommand(Economy eco, SQLiteManager db, String mode) {
        this.eco = eco;
        this.db = db;
        this.mode = mode;
    }

    /**
     * コマンド実行時のメインエントリーポイント
     * プレイヤーのみ実行可能で、モードに応じた処理を分岐
     */
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) return;

        if (MODE_MARKET.equals(mode)) {
            handleMarketMode(player, args);
        } else if (MODE_INTERNAL.equals(mode)) {
            handleInternalMode(player, args);
        }
    }

    /**
     * /market コマンドの処理を分岐
     * - /market add [個数] [値段] : アイテムを出品
     * - /market buy [出品番号] : 出品番号を指定して購入確認
     * - /market : マーケットGUIを開く
     */
    private void handleMarketMode(Player player, String[] args) {
        if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
            handleMarketAdd(player, args);
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("buy")) {
            sendConfirmMessage(player, Integer.parseInt(args[1]));
        } else if (args.length == 0) {
            openMarketGui(player);
        }
    }

    /**
     * /market_internal コマンドの処理を分岐（Click Eventから呼び出し）
     * - /market_internal confirm [出品番号] : 購入を確定
     * - /market_internal cancel [出品番号] : 購入をキャンセル
     */
    private void handleInternalMode(Player player, String[] args) {
        if (args.length < 2) return;

        if (args[0].equals("confirm")) {
            processPurchase(player, Integer.parseInt(args[1]));
        } else if (args[0].equals("cancel")) {
            player.sendMessage(CANCELLED_MESSAGE);
        }
    }

    /**
     * アイテムの出品処理を実行
     * 1. 引数チェック
     * 2. アイテムの妥当性検証
     * 3. データベースに出品情報を保存
     * 4. プレイヤーのインベントリからアイテムを削除
     * 5. Discordに出品通知を送信
     */
    private void handleMarketAdd(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c使用法: /market add [個数] [値段]");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand().clone();
        
        if (!isValidListingItem(player, item)) return;
        if (!isValidListingArgs(player, args, item)) return;

        try {
            int amount = Integer.parseInt(args[1]);
            double price = Double.parseDouble(args[2]);

            item.setAmount(amount);
            int id = generateListingId();
            String serialized = MarketManager.serialize(item);

            insertListingToDatabase(player, id, serialized, price, amount);
            player.getInventory().getItemInMainHand().setAmount(
                    player.getInventory().getItemInMainHand().getAmount() - amount
            );
            player.sendMessage(SUCCESS_LISTED + id);
            sendToDiscord(id, player.getName(), item.getType().name(), amount, price);
        } catch (NumberFormatException e) {
            player.sendMessage(ERROR_INVALID_NUMBER_FORMAT);
        } catch (SQLException e) {
            player.sendMessage(ERROR_DATABASE);
        }
    }

    /**
     * 出品するアイテムが有効か確認
     * エアブロック（何も持っていない状態）は出品不可
     */
    private boolean isValidListingItem(Player player, ItemStack item) {
        if (item.getType() == Material.AIR) {
            player.sendMessage(ERROR_ITEM_NOT_HELD);
            return false;
        }
        return true;
    }

    /**
     * 出品時の引数（個数と値段）が妥当か確認
     * - 個数と値段は正の数値
     * - プレイヤーが持っているアイテムの個数以下
     */
    private boolean isValidListingArgs(Player player, String[] args, ItemStack item) {
        try {
            int amount = Integer.parseInt(args[1]);
            double price = Double.parseDouble(args[2]);

            if (amount <= 0 || price < 0) {
                player.sendMessage(ERROR_POSITIVE_NUMBER);
                return false;
            }

            if (player.getInventory().getItemInMainHand().getAmount() < amount) {
                player.sendMessage(ERROR_INSUFFICIENT_ITEMS);
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 出品番号をランダム生成（1000～9999）
     */
    private int generateListingId() {
        return new Random().nextInt(ID_MAX_RANGE) + ID_MIN;
    }

    /**
     * 出品情報をデータベースに保存
     * 出品番号、売主UUID、売主名、シリアル化されたアイテム、価格、個数を保存
     */
    private void insertListingToDatabase(Player player, int id, String serialized, double price, int amount) 
            throws SQLException {
        Connection conn = db.getConnection();
        String sql = "INSERT INTO market (id, seller_uuid, seller_name, item_data, price, amount) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, player.getName());
            ps.setString(4, serialized);
            ps.setDouble(5, price);
            ps.setInt(6, amount);
            ps.executeUpdate();
        }
    }

    /**
     * マーケットGUIを開く
     * データベースから全ての出品情報を取得して、インベントリに表示
     * 各アイテムには価格、売主、出品番号をロアに追加
     */
    private void openMarketGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, MARKET_GUI_SIZE, Component.text("マーケットGUI"));
        Connection conn = db.getConnection();
        
        try (Statement s = conn.createStatement(); 
             ResultSet rs = s.executeQuery("SELECT * FROM market")) {
            while (rs.next()) {
                ItemStack item = MarketManager.deserialize(rs.getString("item_data"));
                if (item == null) continue;

                addListingItemToGui(item, rs);
                gui.addItem(item);
            }
            player.openInventory(gui);
        } catch (SQLException e) {
            // Database error handling
        }
    }

    /**
     * アイテムにロア（説明文）を追加してGUIに表示する情報を整形
     * 価格、売主名、出品番号を表示する
     */
    private void addListingItemToGui(ItemStack item, ResultSet rs) throws SQLException {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();
        
        lore.add(Component.text("§7----------------"));
        lore.add(Component.text("§f価格: §e" + rs.getDouble("price")));
        lore.add(Component.text("§f出品者: §7" + rs.getString("seller_name")));
        lore.add(Component.text("§f出品番号: §b" + rs.getInt("id")));
        
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    /**
     * 購入確認画面を表示
     * 出品番号から出品情報を取得して、プレイヤーに購入確認メッセージを送信
     * [購入]と[キャンセル]のボタン付きテキストを表示
     */
    private void sendConfirmMessage(Player player, int id) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM market WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) {
                player.sendMessage(ERROR_ITEM_NUMBER_NOT_FOUND);
                return;
            }

            displayPurchaseConfirmation(player, rs, id);
        } catch (SQLException e) {
            // Database error handling
        }
    }

    /**
     * 購入確認メージを表示
     * アイテム名、個数、価格を表示し、Click Eventで購入/キャンセルを実行
     */
    private void displayPurchaseConfirmation(Player player, ResultSet rs, int id) throws SQLException {
        double price = rs.getDouble("price");
        ItemStack item = MarketManager.deserialize(rs.getString("item_data"));
        String itemName = (item != null) ? item.getType().name() : "不明なアイテム";
        int amount = rs.getInt("amount");

        player.sendMessage(Component.text("\n§bアイテムを購入しますか？\n" + itemName + " x" + amount + "  価格: " + price + "money\n")
                .append(Component.text("§a§l[購入]").clickEvent(ClickEvent.runCommand("/market_internal confirm " + id)))
                .append(Component.text("   "))
                .append(Component.text("§c§l[キャンセル]").clickEvent(ClickEvent.runCommand("/market_internal cancel " + id))));
    }

    /**
     * 購入処理を実行
     * 1. データベースから出品情報を取得
     * 2. 購入条件を検証（残高、インベントリ容量など）
     * 3. 取引を完了（金銭の移動、アイテムの移動、DBから削除）
     */
    private void processPurchase(Player player, int id) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM market WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) {
                player.sendMessage(ERROR_LISTING_ENDED);
                return;
            }

            if (!validatePurchase(player, rs)) return;

            completePurchase(player, rs, id, conn);
        } catch (SQLException e) {
            // Database error handling
        }
    }

    /**
     * 購入の前提条件をチェック
     * - プレイヤーの残高が出品価格以上
     * - プレイヤーのインベントリに空きがある
     */
    private boolean validatePurchase(Player player, ResultSet rs) throws SQLException {
        double price = rs.getDouble("price");

        if (eco.getBalance(player) < price) {
            player.sendMessage(ERROR_INSUFFICIENT_BALANCE);
            return false;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ERROR_INVENTORY_FULL);
            return false;
        }

        return true;
    }

    /**
     * 購入を完了
     * 1. プレイヤーから代金を引き出す
     * 2. 売主にお金を振込す
     * 3. プレイヤーのインベントリにアイテムを追加
     * 4. データベースから出品情報を削除
     */
    private void completePurchase(Player player, ResultSet rs, int id, Connection conn) throws SQLException {
        double price = rs.getDouble("price");
        ItemStack item = MarketManager.deserialize(rs.getString("item_data"));
        UUID sellerUuid = UUID.fromString(rs.getString("seller_uuid"));

        eco.withdrawPlayer(player, price);
        eco.depositPlayer(Bukkit.getOfflinePlayer(sellerUuid), price);
        player.getInventory().addItem(item);

        deleteListingFromDatabase(conn, id);
        player.sendMessage(SUCCESS_PURCHASED);
    }

    /**
     * データベースから出品情報を削除
     * 購入完了時に売却済み出品を削除
     */
    private void deleteListingFromDatabase(Connection conn, int id) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM market WHERE id = ?")) {
            del.setInt(1, id);
            del.executeUpdate();
        }
    }

    /**
     * Discord に出品通知を送信
     * DiscordSRVプラグインが有効な場合のみ動作
     * 指定チャンネルに出品情報（番号、売主、アイテム、価格）を送信
     */
    private void sendToDiscord(int id, String playerName, String itemName, int amount, double price) {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;

        TextChannel channel = DiscordUtil.getTextChannelById(DISCORD_CHANNEL_ID);
        if (channel == null) return;

        String message = buildDiscordMessage(id, playerName, itemName, amount, price);
        channel.sendMessage(message).queue();
    }

    /**
     * Discord送信用のメッセージを生成
     * 絵文字と見出しを含むフォーマットされたメッセージを作成
     */
    private String buildDiscordMessage(int id, String playerName, String itemName, int amount, double price) {
        return String.format(
                "📦 **フリマ出品情報**\n" +
                        "【番号】: %d\n" +
                        "【出品者】: %s\n" +
                        "【アイテム】: %s x%d\n" +
                        "【価格】: %.0f money",
                id, playerName, itemName, amount, price
        );
    }

    /**
     * コマンドのタブ補完を提供
     * 現在は補完なし（空のリストを返す）
     */
    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack s, @NotNull String[] a) {
        return List.of();
    }
}