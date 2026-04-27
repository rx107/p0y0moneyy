package com.github.rx107.p0y0money;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.util.Map;

public class SellListener implements Listener {
    private final Economy economy;
    private final PriceManager priceManager;

    public SellListener(Economy economy, PriceManager priceManager) {
        this.economy = economy;
        this.priceManager = priceManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // インベントリのタイトルで判定（EconomyCommandで作成した名前と一致させる）
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals("売り物GUI")) return;

        Player player = (Player) event.getPlayer();
        int totalEarned = 0;

        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            if (priceManager.canSell(item.getType())) {
                // 整数で計算
                totalEarned += priceManager.getPrice(item.getType()) * item.getAmount();
            } else {
                // 売れないアイテムはプレイヤーのインベントリに返す
                Map<Integer, ItemStack> leftOver = player.getInventory().addItem(item);
                // インベントリがいっぱいなら足元にドロップ
                leftOver.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
            }
        }

        if (totalEarned > 0) {
            economy.depositPlayer(player, totalEarned);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            player.sendMessage("§a[Market] §e" + totalEarned + "円 §aを受け取りました。");
        }
    }
}