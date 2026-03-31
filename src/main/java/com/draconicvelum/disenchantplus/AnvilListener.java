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
        // DISENCHANT
        // =========================
        if (Utils.isEnchanted(item) && second.getType() == Material.BOOK) {
            ItemStack result = Utils.createBookFromItem(item);
            if (result != null) {
                event.setResult(result);
            }
            return;
        }

        // =========================
        // SPLIT BOOK
        // =========================
        if (Utils.isEnchantedBook(item) && second.getType() == Material.BOOK) {



            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            int total = meta.getStoredEnchants().size();

            int nonCurseCount = 0;
            for (Enchantment e : meta.getStoredEnchants().keySet()) {
                if (!e.getKey().getKey().startsWith("curse")) {
                    nonCurseCount++;
                }
            }
            Map.Entry<Enchantment, Integer> enchant = null;
            boolean preventCurses = Main.getInstance().getConfig().getBoolean("prevent-curses");
            for (Map.Entry<Enchantment, Integer> e : meta.getStoredEnchants().entrySet()) {
                if (preventCurses) {
                    if (e.getKey().getKey().getKey().startsWith("curse")) continue;
                }

                if (enchant == null || e.getValue() > enchant.getValue()) {
                    enchant = e;
                }
            }

            // No valid enchant → stop
            if (enchant == null) return;

            // ❌ Block if only curses
            if (nonCurseCount == 0) return;

            // ❌ Block if only 1 enchant total (would leave empty)
            if (total <= 1) return;

            ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta newMeta = (EnchantmentStorageMeta) newBook.getItemMeta();
            newMeta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
            newBook.setItemMeta(newMeta);

            ItemMeta displayMeta = newBook.getItemMeta();

            String name = enchant.getKey().getKey().getKey()
                    .replace("_", " ")
                    .toLowerCase();

            String[] words = name.split(" ");
            StringBuilder formatted = new StringBuilder();

            for (String word : words) {
                if (word.isEmpty()) continue;
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
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

            // Create result book
            ItemStack book = Utils.createBookFromItem(item);
            if (book == null) return;

            // 🔥 FULL CLEAN RESET (fixes ALL hidden data issues)
            ItemStack cleanItem = new ItemStack(item.getType(), item.getAmount());

            ItemMeta oldMeta = item.getItemMeta();
            ItemMeta newMeta = cleanItem.getItemMeta();

            if (oldMeta != null && newMeta != null) {

                // Copy display name
                if (oldMeta.hasDisplayName()) {
                    newMeta.setDisplayName(oldMeta.getDisplayName());
                }

                // Copy lore
                if (oldMeta.hasLore()) {
                    newMeta.setLore(oldMeta.getLore());
                }

                // Copy flags
                newMeta.getItemFlags().forEach(newMeta::addItemFlags);

                // 🔥 IMPORTANT: reset repair cost
                if (newMeta instanceof Repairable repairable) {
                    repairable.setRepairCost(0);
                }

                cleanItem.setItemMeta(newMeta);
            }

            // Set cleaned item back
            inv.setItem(0, cleanItem);

            // Clear second slot (important)
            ItemStack newSecond = second.clone();

            if (newSecond.getAmount() > 1) {
                newSecond.setAmount(newSecond.getAmount() - 1);
                inv.setItem(1, newSecond);
            } else {
                inv.setItem(1, null);
            }

            // Give book to player
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(book);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }

            // Force sync (VERY IMPORTANT)
            player.updateInventory();

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
                if (!e.getKey().getKey().startsWith("curse")) {
                    nonCurseCount++;
                }
            }
            Map.Entry<Enchantment, Integer> enchant = null;

            boolean preventCurses = config.getBoolean("prevent-curses");

            for (Map.Entry<Enchantment, Integer> e : meta.getStoredEnchants().entrySet()) {
                if (preventCurses) {
                    if (e.getKey().getKey().getKey().startsWith("curse")) continue;
                }

                if (enchant == null || e.getValue() > enchant.getValue()) {
                    enchant = e;
                }
            }

            // No valid enchant → stop
            if (enchant == null) {
                event.setCancelled(true);
                player.sendMessage("§cNo valid enchantments to split!");
                return;
            }

            // ❌ Only curses → block
            if (nonCurseCount == 0) {
                event.setCancelled(true);
                player.sendMessage("§cCannot split curse-only books!");
                return;
            }

            // ❌ Would leave empty
            if (total <= 1) {
                event.setCancelled(true);
                player.sendMessage("§cCannot split the last enchantment!");
                return;
            }

            int cost = config.getInt("split-xp");

            if (XPUtils.getTotalXP(player) < cost) {
                player.sendMessage("§cNot enough XP!");
                event.setCancelled(true);
                return;
            }

            XPUtils.removeXP(player, cost);

            // REMOVE FROM ORIGINAL
            meta.removeStoredEnchant(enchant.getKey());
            if (meta instanceof Repairable repairable) {
                repairable.setRepairCost(0);
            }
            item.setItemMeta(meta);

            // GIVE NEW BOOK
            ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta newMeta = (EnchantmentStorageMeta) newBook.getItemMeta();
            newMeta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
            newBook.setItemMeta(newMeta);

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(newBook);
            for (ItemStack itemDrop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemDrop);
            }

            inv.setItem(0, item);

            // ✅ consume 1 book safely (clone to avoid reference bugs)
            ItemStack newSecond = second.clone();

            if (newSecond.getAmount() > 1) {
                newSecond.setAmount(newSecond.getAmount() - 1);
                inv.setItem(1, newSecond);
            } else {
                inv.setItem(1, null);
            }
            inv.setItem(2, null);
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                player.updateInventory();
            });
            player.updateInventory();
            playEffect(player, "split");

            String raw = enchant.getKey().getKey().getKey()
                    .replace("_", " ")
                    .toLowerCase();

            String[] words = raw.split(" ");
            StringBuilder formatted = new StringBuilder();

            for (String word : words) {
                if (word.isEmpty()) continue;
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
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
                // Optional fallback
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        } catch (Exception ignored) {}

        player.spawnParticle(Particle.ENCHANT, player.getLocation(), 30);
    }
}