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
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("all")
public class EconomyCommand implements BasicCommand {
    private static final String ERROR_ADMIN_ONLY = "§cこのコマンドを実行する権限がありません。";
    private static final String ERROR_INSUFFICIENT_BALANCE = "§c残高が足りません。";
    private static final String ERROR_INVALID_NUMBER = "§c金額は数字で指定してください。";
    private static final String ERROR_POSITIVE_INTEGER = "§c値には正の整数を指定してください。";
    private static final String DEBUG_PREFIX = "§e[Debug] §f";

    private final Economy economy;
    private final String mode;

    public EconomyCommand(Economy economy, String mode) {
        this.economy = economy;
        this.mode = mode;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) return;

        switch (mode) {
            case "money" -> handleMoneyCommand(player);
            case "pay" -> handlePayCommand(player, args);
            case "sell" -> handleSellCommand(player);
            case "debug" -> handleDebugCommand(player, args);
        }
    }

    private void handleMoneyCommand(Player player) {
        double balance = economy.getBalance(player);
        player.sendMessage("§a現在の残高: §e" + economy.format(balance));
    }

    private void handlePayCommand(Player player, String[] args) {
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
                player.sendMessage(ERROR_INSUFFICIENT_BALANCE);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ERROR_INVALID_NUMBER);
        }
    }

    private void handleSellCommand(Player player) {
        Inventory sellGui = Bukkit.createInventory(null, 27, Component.text("売り物GUI"));
        player.openInventory(sellGui);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage("§e売りたいアイテムをGUIに入れて、画面を閉じてください。");
    }

    private void handleDebugCommand(Player player, String[] args) {
        if (!player.isOp()) {
            player.sendMessage(ERROR_ADMIN_ONLY);
            return;
        }

        if (!isValidDebugArgs(player, args)) {
            return;
        }

        String action = args[1].toLowerCase();
        int amount = Integer.parseInt(args[2]);

        if (action.equals("add")) {
            handleMoneyAdd(player, amount);
        } else if (action.equals("delete")) {
            handleMoneyDelete(player, amount);
        } else {
            player.sendMessage("§c使用法: /dep0y0 money <add|delete> <値>");
        }
    }

    private boolean isValidDebugArgs(Player player, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("money")) {
            player.sendMessage("§c使用法: /dep0y0 money <add|delete> <値>");
            return false;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            if (amount < 0) {
                player.sendMessage(ERROR_POSITIVE_INTEGER);
                return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c値には小数点のない整数を指定してください。");
            return false;
        }

        return true;
    }

    private void handleMoneyAdd(Player player, int amount) {
        economy.depositPlayer(player, amount);
        player.sendMessage(DEBUG_PREFIX + amount + "moneyを追加しました。");
        displayBalance(player);
    }

    private void handleMoneyDelete(Player player, int amount) {
        double currentBalance = economy.getBalance(player);

        if (currentBalance < amount) {
            economy.withdrawPlayer(player, currentBalance);
            player.sendMessage(DEBUG_PREFIX + "所持金が足りないため、残高を強制的に§c0§fにしました。");
        } else {
            economy.withdrawPlayer(player, amount);
            player.sendMessage(DEBUG_PREFIX + amount + "moneyを剥奪しました。");
        }

        displayBalance(player);
    }

    private void displayBalance(Player player) {
        player.sendMessage("§a現在の残高: §e" + economy.format(economy.getBalance(player)));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        return List.of();
    }
}