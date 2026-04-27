package com.github.rx107.p0y0money;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SellListener implements Listener {
    private final Economy economy;
    private final Map<Material, Double> prices = new HashMap<>();

    public SellListener(Economy economy) {
        this.economy = economy;
        // 価格設定 (土:1, 砂:1, 丸石:1, 花崗岩:2, 安山岩:2, 閃緑岩:2)
        prices.put(Material.DIRT, 1.0);
        prices.put(Material.SAND, 1.0);
        prices.put(Material.COBBLESTONE, 1.0);
        prices.put(Material.GRANITE, 2.0);
        prices.put(Material.ANDESITE, 2.0);
        prices.put(Material.DIORITE, 2.0);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // コンポーネントを文字列に変換して比較
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (!title.equals("売り物GUI")) return;

        Player player = (Player) event.getPlayer();
        double totalEarned = 0;
        StringBuilder soldMessage = new StringBuilder();
        boolean hasReturnedItems = false;

        // GUIの中身を1つずつチェック
        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            if (prices.containsKey(item.getType())) {
                // 売れるアイテムの場合
                double price = prices.get(item.getType()) * item.getAmount();
                totalEarned += price;

                // メッセージ用の記録
                soldMessage.append("§f").append(item.getType().name())
                        .append("§7x§f").append(item.getAmount()).append(" ");
            } else {
                // 売れないアイテムの場合：プレイヤーに返す
                // インベントリが満杯なら足元に落とす
                Map<Integer, ItemStack> leftOver = player.getInventory().addItem(item);
                for (ItemStack dropItem : leftOver.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), dropItem);
                }
                hasReturnedItems = true;
            }

            // 最後に村人の取引成立音を再生
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        }

        // お金の支払い処理
        if (totalEarned > 0) {
            economy.depositPlayer(player, totalEarned);
            player.sendMessage(soldMessage.toString() + "§aを売り、§e" + totalEarned + "money§a追加しました！");
        }

        if (hasReturnedItems) {
            player.sendMessage("§c売れないアイテムはインベントリに返還しました。");
        }
    }
}