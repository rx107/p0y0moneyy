package com.github.rx107.p0y0money;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultImplementer extends AbstractEconomy {
    private final SQLiteManager db;

    public VaultImplementer(SQLiteManager db) { this.db = db; }

    @Override public boolean isEnabled() { return true; }
    @Override public String getName() { return "MyEconomy"; }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override public String format(double amount) { return String.format("%.0f円", amount); }

    @Override
    public String currencyNamePlural() {
        return "";
    }

    @Override
    public String currencyNameSingular() {
        return "";
    }

    @Override
    public boolean hasAccount(String s) {
        return false;
    }

    @Override public int fractionalDigits() { return 0; }

    @Override
    public double getBalance(OfflinePlayer player) {
        return db.getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(String s, String s1) {
        return 0;
    }

    @Override
    public boolean has(String s, double v) {
        return false;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        double newBalance = getBalance(player) + amount;
        db.setBalance(player.getUniqueId(), newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        double current = getBalance(player);
        if (current < amount) return new EconomyResponse(0, current, EconomyResponse.ResponseType.FAILURE, "残高不足");
        double newBalance = current - amount;
        db.setBalance(player.getUniqueId(), newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return null;
    }

    @Override public boolean hasAccount(OfflinePlayer player) { return true; }

    @Override
    public boolean hasAccount(String s, String s1) {
        return false;
    }

    @Override
    public double getBalance(String s) {
        return 0;
    }
    // その他の必須メソッドはAbstractEconomyが自動補完します
}