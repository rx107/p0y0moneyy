package com.github.rx107.p0y0money;

import org.bukkit.Material;
import java.util.*;

public class PriceManager {
    private final Map<Material, Integer> sellPrices = new EnumMap<>(Material.class);
    private final Map<Material, Integer> buyPrices = new EnumMap<>(Material.class);

    // ショップカテゴリー用リスト
    private final List<Material> shopItems = new ArrayList<>();

    public PriceManager() {
        // --- 木材 (売:2~3 / 買:10) ---
        setupItem(Material.OAK_LOG, 2, 10);
        setupItem(Material.SPRUCE_LOG, 2, 10);
        setupItem(Material.BIRCH_LOG, 2, 10);
        setupItem(Material.JUNGLE_LOG, 3, 10);
        setupItem(Material.ACACIA_LOG, 3, 10);
        setupItem(Material.DARK_OAK_LOG, 3, 10);
        setupItem(Material.MANGROVE_LOG, 3, 10);
        setupItem(Material.CHERRY_LOG, 3, 10);

        // --- 石・土・砂 (売:1 / 買:5) ---
        setupItem(Material.COBBLESTONE, 1, 5);
        setupItem(Material.STONE, 1, 5);
        setupItem(Material.DIRT, 1, 5);
        setupItem(Material.SAND, 1, 5);
        setupItem(Material.GRAVEL, 1, 5);
        setupItem(Material.GRANITE, 1, 8);
        setupItem(Material.DIORITE, 1, 8);
        setupItem(Material.ANDESITE, 1, 8);
        setupItem(Material.TUFF, 1, 8);

        // --- 鉱石 (売:3~2500 / 買:15~8000) ---
        setupItem(Material.COAL, 5, 20);
        setupItem(Material.IRON_INGOT, 15, 60);
        setupItem(Material.GOLD_INGOT, 30, 120);
        setupItem(Material.REDSTONE, 3, 15);
        setupItem(Material.LAPIS_LAZULI, 10, 40);
        setupItem(Material.DIAMOND, 500, 2000);
        setupItem(Material.EMERALD, 50, 250);

        // --- 作物 (売:1~2 / 買:8) ---
        setupItem(Material.WHEAT, 2, 8);
        setupItem(Material.CARROT, 1, 8);
        setupItem(Material.POTATO, 1, 8);
        setupItem(Material.SUGAR_CANE, 2, 10);
        setupItem(Material.PUMPKIN, 2, 10);
        setupItem(Material.MELON_SLICE, 1, 5);

        // --- 染料 (買:15) ---
        Material[] dyes = {Material.WHITE_DYE, Material.ORANGE_DYE, Material.MAGENTA_DYE, Material.LIGHT_BLUE_DYE, Material.YELLOW_DYE, Material.LIME_DYE, Material.PINK_DYE, Material.GRAY_DYE, Material.LIGHT_GRAY_DYE, Material.CYAN_DYE, Material.PURPLE_DYE, Material.BLUE_DYE, Material.BROWN_DYE, Material.GREEN_DYE, Material.RED_DYE, Material.BLACK_DYE};
        for (Material m : dyes) setupItem(m, 2, 15);

        // --- 花 (買:15) ---
        Material[] flowers = {Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY};
        for (Material m : flowers) setupItem(m, 2, 15);
    }

    private void setupItem(Material m, int sell, int buy) {
        sellPrices.put(m, sell);
        buyPrices.put(m, buy);
        shopItems.add(m);
    }

    public int getSellPrice(Material m) { return sellPrices.getOrDefault(m, 0); }
    public int getBuyPrice(Material m) { return buyPrices.getOrDefault(m, 0); }
    public boolean canSell(Material m) { return sellPrices.containsKey(m); }
    public List<Material> getAllShopItems() { return shopItems; }
}