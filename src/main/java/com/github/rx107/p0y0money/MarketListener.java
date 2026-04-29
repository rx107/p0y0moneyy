package com.github.rx107.p0y0money;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("all")
public class MarketListener implements Listener {
    private final SQLiteManager db;

    public MarketListener(net.milkbowl.vault.economy.Economy eco, SQLiteManager db) { this.db = db; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals("マーケットGUI")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta() || item.getItemMeta().lore() == null) return;

        // Loreから出品番号を無理やり抽出
        String lastLine = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().lore().get(item.getItemMeta().lore().size()-1));
        int id = Integer.parseInt(lastLine.replaceAll("[^0-9]", ""));

        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        player.performCommand("market buy " + id);
    }
}