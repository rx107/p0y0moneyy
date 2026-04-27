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
        econ = implementer;
        getServer().getServicesManager().register(Economy.class, implementer, this, ServicePriority.Highest);

        // コマンド登録のライフサイクル
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            // /sell で直接 GUI モード（EconomyCommandのsell）を呼び出す
            commands.register("sell", "GUIを開いてアイテムを売却します", new EconomyCommand(implementer, "sell"));

            // その他の既存コマンド
            commands.register("market", "フリマ機能", new MarketCommand(implementer, db, "market"));
            commands.register("money", "残高を確認します", new EconomyCommand(implementer, "money"));
            commands.register("pay", "送金します", new EconomyCommand(implementer, "pay"));
            commands.register("dep0y0", "管理者用デバッグコマンド", new EconomyCommand(implementer, "debug"));
        });

        // リスナーの登録
        getServer().getPluginManager().registerEvents(new MarketListener(implementer, db), this);
        // 精算ロジックは SellListener が担当
        getServer().getPluginManager().registerEvents(new SellListener(implementer, priceManager), this);
    }

    public Economy getEconomy() {
        return econ;
    }

    @Override
    public void onDisable() {
        if (db != null) db.close();
    }
}
