package com.draconicvelum.disenchantplus;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AuraSkillsHook {

    public static boolean isEnabled() {
        return Main.getInstance().getConfig().getBoolean("auraskills.enabled")
                && Bukkit.getPluginManager().getPlugin("AuraSkills") != null;
    }

    public static void giveXP(Player player, double xp) {
        if (!isEnabled() || xp <= 0) return;

        try {
            AuraSkillsApi.get().getUser(player.getUniqueId())
                    .addSkillXp(Skills.ENCHANTING, xp);
        } catch (Throwable ignored) {}
    }
}