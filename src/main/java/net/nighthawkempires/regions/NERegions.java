package net.nighthawkempires.regions;

import net.nighthawkempires.core.NECore;
import net.nighthawkempires.core.utils.ItemUtil;
import net.nighthawkempires.core.utils.LocationUtil;
import net.nighthawkempires.regions.commands.PortalsCommand;
import net.nighthawkempires.regions.commands.RegionsCommand;
import net.nighthawkempires.regions.listener.PortalsListener;
import net.nighthawkempires.regions.listener.RegionsListener;
import net.nighthawkempires.regions.portal.PortalManager;
import net.nighthawkempires.regions.region.Region;
import net.nighthawkempires.regions.region.RegionManager;
import net.nighthawkempires.regions.selection.SelectionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NERegions extends JavaPlugin {

    private static Plugin plugin;
    private static NERegions instance;
    private static PluginManager pluginManager;
    private static PortalManager portalManager;
    private static RegionManager regionManager;
    private static SelectionManager selectionManager;

    private static ItemStack selectionTool;

    private static RegionsListener regionsListener;
    private static Region obeyRegion;

    public void onEnable() {
        plugin = this;
        instance = this;
        pluginManager = Bukkit.getPluginManager();
        portalManager = new PortalManager();
        regionManager = new RegionManager();
        selectionManager = new SelectionManager();

        selectionTool = ItemUtil.customItem(Material.BLAZE_ROD, 1, ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "Selection Wand");
        try {
            regionManager.loadRegions();
        } catch (Exception e) {
            NECore.getLoggers().warn(this, "Could not load any regions!");
        }
        try {
            portalManager.loadPortals();
        } catch (Exception e) {
            NECore.getLoggers().warn(this, "Could not load any portals!");
        }

        regionsListener = new RegionsListener();
        registerCommands();
        registerListeners();
    }

    public void onDisable() {
        try {
            getRegionManager().saveRegions();
            getPortalManager().savePortals();
        } catch (NullPointerException ignored) {}
    }

    private void registerCommands() {
        this.getCommand("portals").setExecutor(new PortalsCommand());
        this.getCommand("regions").setExecutor(new RegionsCommand());
    }

    private void registerListeners() {
        getPluginManager().registerEvents(new PortalsListener(), this);
        getPluginManager().registerEvents(regionsListener, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static NERegions getInstance() {
        return instance;
    }

    public static PluginManager getPluginManager() {
        return pluginManager;
    }

    public static PortalManager getPortalManager() {
        return portalManager;
    }

    public static ItemStack getSelectionTool() {
        return selectionTool;
    }

    public static RegionManager getRegionManager() {
        return regionManager;
    }

    public static SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public static Region getObeyRegion(Location location) {
        return regionsListener.obeyRegion(location);
    }
}
