package com.github.rx107.p0y0money;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class P0y0money extends JavaPlugin {
    private SQLiteManager db;
    @Override
    public void onEnable() {
        db = new SQLiteManager();
        db.load(getDataFolder());

        VaultImplementer implementer = new VaultImplementer(db);
        getServer().getServicesManager().register(Economy.class, implementer, this, ServicePriority.Highest);

        // コマンドの登録
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            // マーケットコマンド
            commands.register("market", "フリマ機能", new MarketCommand(implementer, db, "market"));
            commands.register("market_internal", "内部用", new MarketCommand(implementer, db, "internal"));
            // それぞれ別の「モード」を持たせて登録
            commands.register("money", "残高を確認します", new EconomyCommand(implementer, "money"));
            commands.register("pay", "送金します", new EconomyCommand(implementer, "pay"));
            commands.register("sell", "アイテムを売却します", new EconomyCommand(implementer, "sell"));

            // デバッグコマンドを追加 (モード: debug)
            commands.register("dep0y0", "管理者用デバッグコマンド", new EconomyCommand(implementer, "debug"));
        });
        getServer().getPluginManager().registerEvents(new MarketListener(implementer, db), this);
        getServer().getPluginManager().registerEvents(new SellListener(implementer), this);
    }

    @Override
    public void onDisable() {
        if (db != null) db.close();
    }
}

