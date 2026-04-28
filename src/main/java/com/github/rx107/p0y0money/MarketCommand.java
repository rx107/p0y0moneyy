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

public class MarketCommand implements BasicCommand {
    private final Economy eco;
    private final SQLiteManager db;
    private final String mode;

    public MarketCommand(Economy eco, SQLiteManager db, String mode) {
        this.eco = eco;
        this.db = db;
        this.mode = mode;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) return;

        // --- /market 系の処理 ---
        if (mode.equals("market")) {
            if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
                handleMarketAdd(player, args);
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("buy")) {
                sendConfirmMessage(player, Integer.parseInt(args[1]));
            } else if (args.length == 0) {
                openMarketGui(player);
            }
        }

        // --- /market_internal 系の処理 (チャットのボタン用) ---
        if (mode.equals("internal") && args.length >= 2) {
            if (args[0].equals("confirm")) {
                processPurchase(player, Integer.parseInt(args[1]));
            } else if (args[0].equals("cancel")) {
                player.sendMessage("§7[キャンセルしました。]");
            }
        }
    }

    private void handleMarketAdd(Player player, String[] args) {
        // 引数の数が足りているかチェック (/market add <個数> <値段> なので args は 3つ必要)
        if (args.length < 3) {
            player.sendMessage("§c使用法: /market add [個数] [値段]");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand().clone();
        if (item.getType() == org.bukkit.Material.AIR) {
            player.sendMessage("§cアイテムを手に持ってください。");
            return;
        }

        try {
            // args[0]は"add"なので、args[1]が個数、args[2]が値段
            int amount = Integer.parseInt(args[1]);
            double price = Double.parseDouble(args[2]);

            if (amount <= 0 || price < 0) {
                player.sendMessage("§c正の数値を入力してください。");
                return;
            }

            if (player.getInventory().getItemInMainHand().getAmount() < amount) {
                player.sendMessage("§c持っているアイテムの個数が足りません。");
                return;
            }

            item.setAmount(amount);
            int id = new java.util.Random().nextInt(9000) + 1000;
            String serialized = MarketManager.serialize(item);

            java.sql.Connection conn = db.getConnection();
            String sql = "INSERT INTO market (id, seller_uuid, seller_name, item_data, price, amount) VALUES (?,?,?,?,?,?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, player.getUniqueId().toString());
                ps.setString(3, player.getName());
                ps.setString(4, serialized);
                ps.setDouble(5, price);
                ps.setInt(6, amount);
                ps.executeUpdate();

                // 手に持っているアイテムを減らす
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - amount);
                player.sendMessage("§a出品しました！ 出品番号: §e" + id);

                // Discordへ送信
                sendToDiscord(id, player.getName(), item.getType().name(), amount, price);
            }
        } catch (NumberFormatException e) {
            // 数字以外が入力された場合や、小数点のある個数が入力された場合にここに来る
            player.sendMessage("§c個数と値段は半角数字で入力してください。");
        } catch (java.sql.SQLException e) {
            player.sendMessage("§cデータベースエラーが発生しました。");
            e.printStackTrace();
        }
    }

    // GUIを開く処理
    private void openMarketGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text("マーケットGUI"));
        Connection conn = db.getConnection();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM market")) {
            while (rs.next()) {
                ItemStack item = MarketManager.deserialize(rs.getString("item_data"));
                if (item == null) continue;

                ItemMeta meta = item.getItemMeta();
                List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
                if (lore == null) lore = new ArrayList<>();
                lore.add(Component.text("§7----------------"));
                lore.add(Component.text("§f価格: §e" + rs.getDouble("price")));
                lore.add(Component.text("§f出品者: §7" + rs.getString("seller_name")));
                lore.add(Component.text("§f出品番号: §b" + rs.getInt("id")));
                meta.lore(lore);
                item.setItemMeta(meta);
                gui.addItem(item);
            }
            player.openInventory(gui);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendConfirmMessage(Player player, int id) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM market WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                player.sendMessage("§cその出品番号は存在しません！");
                return;
            }

            double price = rs.getDouble("price");
            ItemStack item = MarketManager.deserialize(rs.getString("item_data"));
            String itemName = (item != null) ? item.getType().name() : "不明なアイテム";
            int amount = rs.getInt("amount");

            player.sendMessage(Component.text("\n§bアイテムを購入しますか？\n" + itemName + " x" + amount + "  価格: " + price + "money\n")
                    .append(Component.text("§a§l[購入]").clickEvent(ClickEvent.runCommand("/market_internal confirm " + id)))
                    .append(Component.text("   "))
                    .append(Component.text("§c§l[キャンセル]").clickEvent(ClickEvent.runCommand("/market_internal cancel " + id))));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processPurchase(Player player, int id) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM market WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                player.sendMessage("§cその出品は既に終了しているか、存在しません。");
                return;
            }

            double price = rs.getDouble("price");
            ItemStack item = MarketManager.deserialize(rs.getString("item_data"));

            if (eco.getBalance(player) < price) {
                player.sendMessage("§cお金が足りません！");
                return;
            }
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage("§cインベントリの中がいっぱいです！");
                return;
            }

            eco.withdrawPlayer(player, price);
            eco.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("seller_uuid"))), price);
            player.getInventory().addItem(item);

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM market WHERE id = ?")) {
                del.setInt(1, id);
                del.executeUpdate();
            }

            player.sendMessage("§a購入しました！");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendToDiscord(int id, String playerName, String itemName, int amount, double price) {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;

        String channelId = "1451477356542955540";
        TextChannel channel = DiscordUtil.getTextChannelById(channelId);

        if (channel != null) {
            String message = String.format(
                    "📦 **フリマ出品情報**\n" +
                            "【番号】: %d\n" +
                            "【出品者】: %s\n" +
                            "【アイテム】: %s x%d\n" +
                            "【価格】: %.0f money",
                    id, playerName, itemName, amount, price
            );
            channel.sendMessage(message).queue();
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack s, @NotNull String[] a) {
        return List.of();
    }
}