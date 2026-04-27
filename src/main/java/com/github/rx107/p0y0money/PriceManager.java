package com.github.rx107.p0y0money;

import org.bukkit.Material;
import java.util.EnumMap;
import java.util.Map;

public class PriceManager {
    // 価格を整数(Integer)に変更
    private final Map<Material, Integer> prices = new EnumMap<>(Material.class);

    public PriceManager() {
        // --- 木材 ---
        addPrice(Material.OAK_LOG, 2);
        addPrice(Material.SPRUCE_LOG, 2);
        addPrice(Material.BIRCH_LOG, 2);
        addPrice(Material.JUNGLE_LOG, 3);
        addPrice(Material.ACACIA_LOG, 3);
        addPrice(Material.DARK_OAK_LOG, 3);
        addPrice(Material.MANGROVE_LOG, 3);
        addPrice(Material.CHERRY_LOG, 3);

        // --- 石・土・砂・砂利 ---
        addPrice(Material.COBBLESTONE, 1);
        addPrice(Material.STONE, 1);
        addPrice(Material.COBBLED_DEEPSLATE, 1);
        addPrice(Material.GRANITE, 1);
        addPrice(Material.DIORITE, 1);
        addPrice(Material.ANDESITE, 1);
        addPrice(Material.DIRT, 1);
        addPrice(Material.GRASS_BLOCK, 2);
        addPrice(Material.SAND, 1);
        addPrice(Material.RED_SAND, 2);
        addPrice(Material.GRAVEL, 1);

        // --- 鉱石 ---
        addPrice(Material.COAL, 5);
        addPrice(Material.IRON_INGOT, 15);
        addPrice(Material.GOLD_INGOT, 30);
        addPrice(Material.DIAMOND, 500);
        addPrice(Material.EMERALD, 50);
        addPrice(Material.NETHERITE_SCRAP, 2500);

        // --- 作物 ---
        addPrice(Material.WHEAT, 2);
        addPrice(Material.CARROT, 1);
        addPrice(Material.POTATO, 1);
        addPrice(Material.SUGAR_CANE, 2);
        addPrice(Material.PUMPKIN, 2);
    }

    private void addPrice(Material material, int price) {
        prices.put(material, price);
    }

    public Integer getPrice(Material material) {
        return prices.getOrDefault(material, 0);
    }

    public boolean canSell(Material material) {
        return prices.containsKey(material);
    }
}
