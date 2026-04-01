package com.draconicvelum.disenchantplus;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import java.util.HashMap;
import java.util.Map;

public class AnvilListener implements Listener {

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();

        ItemStack item = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (item == null || second == null) return;

        // =========================
        // DISENCHANT PREVIEW
        // =========================
        if (Utils.isEnchanted(item) && second.getType() == Material.BOOK) {
            ItemStack result = Utils.createBookFromItem(item);
            if (result != null) {
                event.setResult(result);
            }
            return;
        }

        // =========================
        // SPLIT PREVIEW
        // =========================
        if (Utils.isEnchantedBook(item) && second.getType() == Material.BOOK) {

            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            int total = meta.getStoredEnchants().size();

            int nonCurseCount = 0;
            for (Enchantment e : meta.getStoredEnchants().keySet()) {
                if (!e.getKey().getKey().contains("curse")) {
                    nonCurseCount++;
                }
            }

            Map.Entry<Enchantment, Integer> enchant = null;
            boolean preventCurses = Main.getInstance().getConfig().getBoolean("prevent-curses");

            for (Map.Entry<Enchantment, Integer> e : meta.getStoredEnchants().entrySet()) {
                if (preventCurses && e.getKey().getKey().getKey().contains("curse")) continue;

                if (enchant == null || e.getValue() > enchant.getValue()) {
                    enchant = e;
                }
            }

            if (enchant == null) return;
            if (nonCurseCount == 0) return;
            if (total <= 1) return;

            ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta newMeta = (EnchantmentStorageMeta) newBook.getItemMeta();
            newMeta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
            newBook.setItemMeta(newMeta);

            ItemMeta displayMeta = newBook.getItemMeta();

            String raw = enchant.getKey().getKey().getKey().replace("_", " ").toLowerCase();
            StringBuilder formatted = new StringBuilder();

            for (String word : raw.split(" ")) {
                if (!word.isEmpty()) {
                    formatted.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1)).append(" ");
                }
            }

            displayMeta.setDisplayName("§a" + formatted.toString().trim() + " " + enchant.getValue());
            newBook.setItemMeta(displayMeta);

            event.setResult(newBook);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getView().getTopInventory() instanceof AnvilInventory inv)) return;
        if (event.getRawSlot() != 2) return;

        Player player = (Player) event.getWhoClicked();
        var config = Main.getInstance().getConfig();

        ItemStack item = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (item == null || second == null) return;
        if (second.getType() != Material.BOOK) return;

        // =========================
        // DISENCHANT APPLY
        // =========================
        if (Utils.isEnchanted(item) && !(item.getItemMeta() instanceof EnchantmentStorageMeta)) {

            int cost = config.getInt("disenchant-xp");

            if (XPUtils.getTotalXP(player) < cost) {
                player.sendMessage("§cNot enough XP!");
                event.setCancelled(true);
                return;
            }

            XPUtils.removeXP(player, cost);

            ItemStack book = Utils.createBookFromItem(item);
            if (book == null) return;

            ItemStack cleanItem = item.clone();
            ItemMeta meta = cleanItem.getItemMeta();

            if (meta != null) {
                meta.getEnchants().keySet().forEach(meta::removeEnchant);

                if (meta instanceof Repairable repairable) {
                    repairable.setRepairCost(0);
                }

                cleanItem.setItemMeta(meta);
            }

            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                inv.setItem(0, cleanItem);
            });

            ItemStack newSecond = second.clone();

            if (newSecond.getAmount() > 1) {
                newSecond.setAmount(newSecond.getAmount() - 1);

                inv.setItem(1, null);

                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    inv.setItem(1, newSecond);
                });

            } else {
                inv.setItem(1, null);
            }
            Bukkit.getScheduler().runTask(Main.getInstance(), player::updateInventory);

            // give result
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(book);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }

            inv.setItem(2, null);

            Bukkit.getScheduler().runTask(Main.getInstance(), player::updateInventory);

            playEffect(player, "disenchant");
            event.setCancelled(true);
            return;
        }

        // =========================
        // SPLIT APPLY
        // =========================
        if (Utils.isEnchantedBook(item)) {

            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            int total = meta.getStoredEnchants().size();

            int nonCurseCount = 0;
            for (Enchantment e : meta.getStoredEnchants().keySet()) {
                if (!e.getKey().getKey().contains("curse")) {
                    nonCurseCount++;
                }
            }

            Map.Entry<Enchantment, Integer> enchant = null;
            boolean preventCurses = config.getBoolean("prevent-curses");

            for (Map.Entry<Enchantment, Integer> e : meta.getStoredEnchants().entrySet()) {
                if (preventCurses && e.getKey().getKey().getKey().contains("curse")) continue;

                if (enchant == null || e.getValue() > enchant.getValue()) {
                    enchant = e;
                }
            }

            if (enchant == null) {
                player.sendMessage("§cNo valid enchantments to split!");
                event.setCancelled(true);
                return;
            }

            if (nonCurseCount == 0) {
                player.sendMessage("§cCannot split curse-only books!");
                event.setCancelled(true);
                return;
            }

            if (total <= 1) {
                player.sendMessage("§cCannot split the last enchantment!");
                event.setCancelled(true);
                return;
            }

            int cost = config.getInt("split-xp");

            if (XPUtils.getTotalXP(player) < cost) {
                player.sendMessage("§cNot enough XP!");
                event.setCancelled(true);
                return;
            }

            XPUtils.removeXP(player, cost);

            meta.removeStoredEnchant(enchant.getKey());

            if (meta instanceof Repairable repairable) {
                repairable.setRepairCost(0);
            }

            item.setItemMeta(meta);

            ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta newMeta = (EnchantmentStorageMeta) newBook.getItemMeta();
            newMeta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
            newBook.setItemMeta(newMeta);

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(newBook);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }

            inv.setItem(0, item);

            ItemStack newSecond = second.clone();

            if (newSecond.getAmount() > 1) {
                newSecond.setAmount(newSecond.getAmount() - 1);

                inv.setItem(1, null);

                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    inv.setItem(1, newSecond);
                });

            } else {
                inv.setItem(1, null);
            }

            inv.setItem(2, null);

            Bukkit.getScheduler().runTask(Main.getInstance(), player::updateInventory);

            playEffect(player, "split");

            String raw = enchant.getKey().getKey().getKey().replace("_", " ").toLowerCase();
            StringBuilder formatted = new StringBuilder();

            for (String word : raw.split(" ")) {
                if (!word.isEmpty()) {
                    formatted.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1)).append(" ");
                }
            }

            player.sendMessage("§aExtracted " + formatted.toString().trim() + " " + enchant.getValue() + "!");
            event.setCancelled(true);
        }
    }

    private void playEffect(Player player, String type) {
        var config = Main.getInstance().getConfig();
        if (!config.getBoolean("sounds.enabled")) return;

        String soundName = config.getString("sounds." + type);

        if (soundName == null || soundName.isEmpty()) {
            player.spawnParticle(Particle.ENCHANT, player.getLocation(), 30);
            return;
        }

        try {
            NamespacedKey key = NamespacedKey.minecraft(soundName.toLowerCase());
            Sound sound = Registry.SOUNDS.get(key);

            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1f, 1f);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        } catch (Exception ignored) {}

        player.spawnParticle(Particle.ENCHANT, player.getLocation(), 30);
    }
}
