package com.github.rx107.p0y0money;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.*;
import java.util.Base64;

@SuppressWarnings("deprecation")
public class MarketManager {
    // アイテムを文字列に変換
    public static String serialize(ItemStack item) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(item);
            os.flush();
            return Base64.getEncoder().encodeToString(io.toByteArray());
        } catch (IOException e) { return null; }
    }

    // 文字列をアイテムに復元
    public static ItemStack deserialize(String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream io = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream is = new BukkitObjectInputStream(io);
            return (ItemStack) is.readObject();
        } catch (IOException | ClassNotFoundException e) { return null; }
    }
}
