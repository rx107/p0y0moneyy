package com.github.rx107.p0y0money;

import org.bukkit.Material;
import java.util.EnumMap;
import java.util.Map;

public class PriceManager {
    private final Map<Material, Integer> prices = new EnumMap<>(Material.class);

    public PriceManager() {
        // --- 木材 ---
        add(Material.OAK_LOG, 2); add(Material.SPRUCE_LOG, 2); add(Material.BIRCH_LOG, 2);
        add(Material.JUNGLE_LOG, 3); add(Material.ACACIA_LOG, 3); add(Material.DARK_OAK_LOG, 3);
        add(Material.MANGROVE_LOG, 3); add(Material.CHERRY_LOG, 3);

        // --- 石・土・砂・砂利 ---
        add(Material.COBBLESTONE, 1); add(Material.STONE, 1); add(Material.DIRT, 1);
        add(Material.SAND, 1); add(Material.GRAVEL, 1); add(Material.GRASS_BLOCK, 2);
        add(Material.RED_SAND, 2); add(Material.GRANITE, 1); add(Material.DIORITE, 1); add(Material.ANDESITE, 1);

        // --- 鉱石 ---
        add(Material.COAL, 5); add(Material.IRON_INGOT, 15); add(Material.GOLD_INGOT, 30);
        add(Material.DIAMOND, 500); add(Material.EMERALD, 50); add(Material.NETHERITE_SCRAP, 2500);

        // --- 作物 ---
        add(Material.WHEAT, 2); add(Material.CARROT, 1); add(Material.POTATO, 1);
        add(Material.SUGAR_CANE, 2); add(Material.PUMPKIN, 2);
    }

    private void add(Material m, int p) { prices.put(m, p); }
    public int getPrice(Material m) { return prices.getOrDefault(m, 0); }
    public boolean canSell(Material m) { return prices.containsKey(m); }
}