package com.draconicvelum.disenchantplus;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static boolean taxlessEnabled;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        var plugin = getServer().getPluginManager().getPlugin("TaxlessEnchanting");
        taxlessEnabled = plugin != null && plugin.isEnabled();

        getServer().getPluginManager().registerEvents(new AnvilListener(), this);

        // AuraSkills hook log
        boolean auraEnabled = getServer().getPluginManager().isPluginEnabled("AuraSkills");

        if (auraEnabled) {
            getLogger().info("AuraSkills detected and hooked.");
        } else {
            getLogger().info("AuraSkills not found, running without it.");
        }

        getLogger().info("DisenchantPlus enabled!");
        getLogger().info("TaxlessEnchanting compatibility: " + (taxlessEnabled ? "ENABLED" : "DISABLED"));
    }

    public static boolean isTaxlessEnabled() {
        return taxlessEnabled;
    }
    @Override
    public void onLoad() {
        var plugin = getServer().getPluginManager().getPlugin("TaxlessEnchanting");
        taxlessEnabled = plugin != null && plugin.isEnabled();
    }
}
