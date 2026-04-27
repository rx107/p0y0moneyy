package com.github.rx107.p0y0money;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SellCommand implements CommandExecutor {
    private final P0y0money plugin;
    private final PriceManager priceManager;

    public SellCommand(P0y0money plugin, PriceManager priceManager) {
        this.plugin = plugin;
        this.priceManager = priceManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        ItemStack item = player.getInventory().getItemInMainHand();
        Material type = item.getType();

        if (!priceManager.canSell(type)) {
            player.sendMessage(ChatColor.RED + "このアイテムは売却できません。");
            return true;
        }

        int amount = item.getAmount();
        // 整数で計算
        int totalPrice = priceManager.getPrice(type) * amount;

        if (plugin.getEconomy() != null) {
            // Vaultへの入金（メソッドがdoubleを求めるので自動変換されます）
            plugin.getEconomy().depositPlayer(player, totalPrice);
            player.getInventory().setItemInMainHand(null);

            // メッセージも整数で綺麗に表示
            player.sendMessage(ChatColor.GREEN + "手に持っていたアイテムを " +
                    ChatColor.GOLD + totalPrice + "円" +
                    ChatColor.GREEN + " で売却しました！");
        }

        return true;
    }
}
