package com.github.rx107.p0y0money;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class P0y0money extends JavaPlugin {
    private SQLiteManager db;
    private PriceManager priceManager;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        db = new SQLiteManager();
        db.load(getDataFolder());
        this.priceManager = new PriceManager();

        VaultImplementer implementer = new VaultImplementer(db);
        ShopManager shopManager = new ShopManager(priceManager);
        ShopCommand shopCmd = new ShopCommand(implementer, shopManager, priceManager, "shop");
        ShopCommand internalCmd = new ShopCommand(implementer, shopManager, priceManager, "internal");
        econ = implementer;
        getServer().getServicesManager().register(Economy.class, implementer, this, ServicePriority.Highest);

        // コマンド登録のライフサイクル
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            //コマンドリスト
            commands.register("market", "フリマ機能", new MarketCommand(implementer, db, "market"));
            commands.register("market_internal", "内部用", new MarketCommand(implementer, db, "internal"));
            commands.register("shop", "ショップを開く", shopCmd);
            commands.register("shop_internal", "内部用", internalCmd);
            commands.register("money", "残高を確認します", new EconomyCommand(implementer, "money"));
            commands.register("pay", "送金します", new EconomyCommand(implementer, "pay"));
            commands.register("sell", "GUIを開いてアイテムを売却します", new EconomyCommand(implementer, "sell"));
            commands.register("dep0y0", "管理者用デバッグコマンド", new EconomyCommand(implementer, "debug"));
        });

        //GUIの設定(Listener)
        getServer().getPluginManager().registerEvents(new MarketListener(implementer, db), this);
        getServer().getPluginManager().registerEvents(new SellListener(implementer, priceManager), this);
        getServer().getPluginManager().registerEvents(new ShopListener(shopManager, shopCmd), this);
    }

    public Economy getEconomy() {
        return econ;
    }

    @Override
    public void onDisable() {
        if (db != null) db.close();
    }
}
