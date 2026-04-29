package com.github.rx107.p0y0money;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {
    private final ShopManager shopManager;
    private final ShopCommand shopCommand;

    public ShopListener(ShopManager shopManager, ShopCommand shopCommand) {
        this.shopManager = shopManager;
        this.shopCommand = shopCommand;
    }

    @EventHandler
    @SuppressWarnings("all")
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.contains("ショップ") && !title.contains("セール")) return;

        // 左クリック以外（ホイール、右クリック、中クリック）を無視
        if (!event.getClick().isLeftClick()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (title.equals("ショップメニュー")) {
            if (item.getType() == Material.GOLD_INGOT) shopManager.openBuyGui(player, 1);
            if (item.getType() == Material.WHITE_BANNER) shopManager.openSaleGui(player);
        }
        else if (title.startsWith("ショップ一覧")) {
            // ページ番号を取得
            int currentPage = Integer.parseInt(title.substring(title.lastIndexOf(" ") + 1));

            if (item.getType() == Material.ARROW) {
                // ページ送り処理
                String name = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
                if (name.contains("次のページ")) shopManager.openBuyGui(player, currentPage + 1);
                else shopManager.openBuyGui(player, currentPage - 1);
            }
            else if (item.getType() == Material.BARRIER) {
                shopManager.openMainMenu(player);
            }
            else {
                // 購入確認へ
                player.closeInventory();
                shopCommand.sendConfirmMessage(player, item.getType());
            }
        }
        else if (title.equals("本日替わりセール品")) {
            if (item.getType() == Material.BARRIER) {
                shopManager.openMainMenu(player);
            } else {
                player.closeInventory();
                shopCommand.sendConfirmMessage(player, item.getType());
            }
        }
    }
}
