package com.github.rx107.p0y0money;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class ShopManager {
    private final PriceManager priceManager;
    private final List<Material> saleItems = new ArrayList<>();
    private final int ITEMS_PER_PAGE = 45; // 5行分

    public ShopManager(PriceManager priceManager) {
        this.priceManager = priceManager;
        updateSales();
    }

    public void updateSales() {
        saleItems.clear();
        List<Material> all = new ArrayList<>(priceManager.getAllShopItems());
        Collections.shuffle(all);
        for (int i = 0; i < Math.min(10, all.size()); i++) {
            saleItems.add(all.get(i));
        }
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("ショップメニュー"));
        inv.setItem(11, createGuiItem(Material.GOLD_INGOT, "§6§l[ショップを確認]"));
        inv.setItem(15, createGuiItem(Material.WHITE_BANNER, "§f§l[セール情報]"));
        player.openInventory(inv);
    }

    // ページ指定付きの商品一覧
    public void openBuyGui(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("ショップ一覧 Page " + page));
        List<Material> allItems = priceManager.getAllShopItems();

        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allItems.size());

        for (int i = start; i < end; i++) {
            Material m = allItems.get(i);
            inv.addItem(createShopItem(m, priceManager.getBuyPrice(m), false));
        }

        // 下段ナビゲーション
        if (page > 1) inv.setItem(45, createGuiItem(Material.ARROW, "§e前のページへ (Page " + (page - 1) + ")"));
        inv.setItem(49, createGuiItem(Material.BARRIER, "§cメニューに戻る"));
        if (end < allItems.size()) inv.setItem(53, createGuiItem(Material.ARROW, "§e次のページへ (Page " + (page + 1) + ")"));

        player.openInventory(inv);
    }

    public void openSaleGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("本日替わりセール品"));
        for (Material m : saleItems) {
            inv.addItem(createShopItem(m, priceManager.getBuyPrice(m) / 2, true));
        }
        inv.setItem(22, createGuiItem(Material.BARRIER, "§cメニューに戻る"));
        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material m, String name) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createShopItem(Material m, int price, boolean isSale) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§f価格: §e" + price + "円" + (isSale ? " §c(50% OFF!)" : "")));
        lore.add(Component.text("§7左クリックで購入確認"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isSaleItem(Material m) { return saleItems.contains(m); }
}
