package com.draconicvelum.disenchantplus;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

public class Utils {

    public static boolean isEnchanted(ItemStack item) {
        return item != null && !item.getEnchantments().isEmpty();
    }

    public static boolean isEnchantedBook(ItemStack item) {
        if (item == null) return false;
        if (!(item.getItemMeta() instanceof EnchantmentStorageMeta meta)) return false;
        return !meta.getStoredEnchants().isEmpty();
    }

    public static ItemStack createBookFromItem(ItemStack item) {
        if (item == null || item.getEnchantments().isEmpty()) return null;

        ItemStack book = new ItemStack(org.bukkit.Material.ENCHANTED_BOOK);

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta == null) return book;

        for (Map.Entry<Enchantment, Integer> e : item.getEnchantments().entrySet()) {
            meta.addStoredEnchant(e.getKey(), e.getValue(), true);
        }

        book.setItemMeta(meta);
        return book;
    }
}
