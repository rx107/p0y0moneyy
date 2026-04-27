package com.github.rx107.p0y0money;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class EconomyCommand implements BasicCommand {
    private final Economy economy;
    private final String mode; // "money", "pay", "sell" のどれかを保持

    public EconomyCommand(Economy economy, String mode) {
        this.economy = economy;
        this.mode = mode;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) return;

        // モードによって処理を分岐
        switch (mode) {
            case "money" -> {
                player.sendMessage("§a現在の残高: §e" + economy.format(economy.getBalance(player)));
            }
            case "pay" -> {
                if (args.length < 2) {
                    player.sendMessage("§c使用法: /pay <名前> <金額>");
                    return;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                try {
                    double amount = Double.parseDouble(args[1]);
                    if (economy.withdrawPlayer(player, amount).transactionSuccess()) {
                        economy.depositPlayer(target, amount);
                        player.sendMessage("§b" + target.getName() + "へ " + economy.format(amount) + " 送金しました。");
                    } else {
                        player.sendMessage("§c残高が足りません。");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c金額は数字で指定してください。");
                }
            }
            case "sell" -> {
                Inventory sellGui = Bukkit.createInventory(null, 27, Component.text("売り物GUI"));
                player.openInventory(sellGui);
                // 経験値オーブを拾った音を再生 (場所, 音の種類, 音量, ピッチ)
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                player.sendMessage("§e売りたいアイテムをGUIに入れて、画面を閉じてください。");
            }
            case "debug" -> {
                // 1. 管理者権限(OP)チェック
                if (!player.isOp()) {
                    player.sendMessage("§cこのコマンドを実行する権限がありません。");
                    return;
                }

                // 引数チェック: /dep0y0 money <add|delete> <値> (最低4つの要素が必要)
                if (args.length < 3 || !args[0].equalsIgnoreCase("money")) {
                    player.sendMessage("§c使用法: /dep0y0 money <add|delete> <値>");
                    return;
                }

                String action = args[1].toLowerCase();
                int amount;

                // 2. 小数点がないこと（整数のみ）のチェック
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount < 0) {
                        player.sendMessage("§c値には正の整数を指定してください。");
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c値には小数点のない整数を指定してください。");
                    return;
                }

                double currentBalance = economy.getBalance(player);

                if (action.equals("add")) {
                    // 所持金の追加
                    economy.depositPlayer(player, amount);
                    player.sendMessage("§e[Debug] §f" + amount + "moneyを追加しました。");
                    player.sendMessage("§a現在の残高: §e" + economy.format(economy.getBalance(player)));

                } else if (action.equals("delete")) {
                    // 3. 所持金の剥奪（マイナスになる場合は0にする）
                    if (currentBalance < amount) {
                        // 全額没収（残高を0にするために、現在の全額を引き出す）
                        economy.withdrawPlayer(player, currentBalance);
                        player.sendMessage("§e[Debug] §f所持金が足りないため、残高を強制的に§c0§fにしました。");
                    } else {
                        economy.withdrawPlayer(player, amount);
                        player.sendMessage("§e[Debug] §f" + amount + "moneyを剥奪しました。");
                    }
                    player.sendMessage("§a現在の残高: §e" + economy.format(economy.getBalance(player)));

                } else {
                    player.sendMessage("§c使用法: /dep0y0 money <add|delete> <値>");

                }

            }
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        return List.of();
    }
}