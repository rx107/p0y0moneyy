package com.github.rx107.p0y0money;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class ShopCommand implements BasicCommand {
    private final Economy eco;
    private final ShopManager shopManager;
    private final PriceManager priceManager;
    private final String mode;

    public ShopCommand(Economy eco, ShopManager shopManager, PriceManager priceManager, String mode) {
        this.eco = eco;
        this.shopManager = shopManager;
        this.priceManager = priceManager;
        this.mode = mode;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (!(stack.getSender() instanceof Player player)) return;

        if (mode.equals("shop")) {
            shopManager.openMainMenu(player);
        } else if (mode.equals("internal") && args.length >= 1) {
            handleInternal(player, args);
        }
    }

    private void handleInternal(Player player, String[] args) {
        switch (args[0]) {
            case "confirm" -> {
                Material m = Material.valueOf(args[1]);
                int price = priceManager.getBuyPrice(m);
                if (shopManager.isSaleItem(m)) price /= 2;

                if (eco.getBalance(player) < price) {
                    player.sendMessage("§cお金が足りません！");
                    return;
                }
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage("§cインベントリがいっぱいです！");
                    return;
                }

                eco.withdrawPlayer(player, price);
                player.getInventory().addItem(new ItemStack(m));
                player.sendMessage("§a" + m.name() + " を購入しました！");
            }
            case "cancel" -> {
                player.sendMessage("§cキャンセルしました。");
                shopManager.openMainMenu(player); // 最初からやり直し
            }
        }
    }

    public void sendConfirmMessage(Player player, Material m) {
        int price = priceManager.getBuyPrice(m);
        if (shopManager.isSaleItem(m)) price /= 2;

        player.sendMessage(Component.text("\n§bアイテムを購入しますか？\n" + m.name() + "  価格: " + price + "円\n")
                .append(Component.text("§a§l[購入]").clickEvent(ClickEvent.runCommand("/shop_internal confirm " + m.name())))
                .append(Component.text("   "))
                .append(Component.text("§c§l[キャンセル]").clickEvent(ClickEvent.runCommand("/shop_internal cancel"))));
    }
}
