package com.draconicvelum.disenchantplus;

import org.bukkit.entity.Player;

public class XPUtils {

    public static int getTotalXP(Player player) {
        int level = player.getLevel();
        float progress = player.getExp();

        int xp = 0;

        for (int i = 0; i < level; i++) {
            xp += getXPToNextLevel(i);
        }

        xp += Math.round(getXPToNextLevel(level) * progress);
        return xp;
    }

    public static void removeXP(Player player, int amount) {
        int total = getTotalXP(player);
        total -= amount;

        if (total < 0) total = 0;

        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);

        player.giveExp(total);
    }

    private static int getXPToNextLevel(int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }
}