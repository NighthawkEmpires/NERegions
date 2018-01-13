package net.nighthawkempires.regions.portal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.core.NECore;
import net.nighthawkempires.core.file.FileType;
import net.nighthawkempires.regions.NERegions;
import net.nighthawkempires.regions.portal.destination.DestinationType;
import net.nighthawkempires.regions.portal.type.PortalCubiod;
import net.nighthawkempires.regions.portal.type.PortalType;
import net.nighthawkempires.regions.region.type.RegionType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class PortalManager {

    private ConcurrentMap<String, Class<? extends Portal>> classMap;
    private ConcurrentMap<String, Portal> portalMap;
    private List<Portal> portals;

    public PortalManager() {
        classMap = Maps.newConcurrentMap();
        portalMap = Maps.newConcurrentMap();
        portals = Lists.newArrayList();

        classMap.put(PortalType.CUBOID.name(), PortalCubiod.class);
    }

    public void loadPortals() {
        Set<String> sections = getPortalsFile().getConfigurationSection("portals").getKeys(false);
        if (sections != null) {
            for (String string : sections) {
                loadPortal(string);
            }
        }
        NECore.getLoggers().info(NERegions.getPlugin(), "Loaded a total of " + portals.size() + " portals.");
    }

    public void loadPortal(String name) {
        ConfigurationSection section = getPortalsFile().getConfigurationSection("portals." + name);
        PortalType type = (section.isSet("type") ? PortalType.valueOf(section.getString("type").toUpperCase()) : null);
        if (type == null) {
            NECore.getLoggers().warn(NERegions.getPlugin(), "\'" + name + "\' does not have a valid portal type set.");
            return;
        }

        Class clazz = getClassMap().get(type.name());
        Portal portal;
        try {
            portal = (Portal) clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            NECore.getLoggers().warn(NERegions.getPlugin(), "Could not load portal: \'" + name + "\'");
            return;
        }

        try {
            portal.create(name);
            getPortalMap().put(name, portal);
            getPortals().add(portal);
        } catch (Exception e) {
            NECore.getLoggers().warn(NERegions.getPlugin(), "Failed to load portal: \'" + name + "\'");
        }
    }

    public void savePortals() {
        if (getPortals().size() == 0) {
            return;
        }
        for (String section : getPortalsFile().getConfigurationSection("portals").getKeys(false)) {
            if (!exists(section)) {
                getPortalsFile().set("portals." + section, null);
                savePortalsFile();
            }
        }

        if (getPortals().size() != 0) {
            for (Portal portal : getPortals()) {
                getPortalsFile().set("portals." + portal.getName() + ".destination-type", portal.getDestinationType().name());
                getPortalsFile().set("portals." + portal.getName() + ".destination", portal.getDestination());
                savePortalsFile();
            }
        }
    }

    public void createPortal(String name, Location pos1, Location pos2) {
        getPortalsFile().set("portals." + name + ".type", RegionType.CUBOID.name());
        getPortalsFile().set("portals." + name + ".priority", 1);
        getPortalsFile().set("portals." + name + ".world", pos1.getWorld().getName());
        getPortalsFile().set("portals." + name + ".pos-1.cord-x", pos1.getBlockX());
        getPortalsFile().set("portals." + name + ".pos-1.cord-y", pos1.getBlockY());
        getPortalsFile().set("portals." + name + ".pos-1.cord-z", pos1.getBlockZ());
        getPortalsFile().set("portals." + name + ".pos-2.cord-x", pos2.getBlockX());
        getPortalsFile().set("portals." + name + ".pos-2.cord-y", pos2.getBlockY());
        getPortalsFile().set("portals." + name + ".pos-2.cord-z", pos2.getBlockZ());
        savePortalsFile();

        loadPortal(name);
    }

    public void setDestination(String name, DestinationType type, String destination) {
        if (exists(name)) {
            getPortalsFile().set("portals." + name + ".destination-type", type.name());
            getPortalsFile().set("portals." + name + ".destinatoin", destination);
            getPortal(name).setDestinationType(type);
            getPortal(name).setDestination(destination);
            savePortalsFile();
        }
    }

    public void deletePortal(String name) {
        if (exists(name)) {
            getPortalsFile().set("regions." + name, null);
            savePortalsFile();
            getPortals().remove(getPortal(name));
            getPortalMap().remove(getPortal(name).getName());
        }
    }

    public Portal getPortal(String name) {
        for (String string : portalMap.keySet()) {
            if (string.toLowerCase().equals(name.toLowerCase())) {
                return portalMap.get(string);
            }
        }
        return null;
    }

    public boolean exists(String name) {
        for (String string : portalMap.keySet()) {
            if (string.toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public ConcurrentMap<String, Class<? extends Portal>> getClassMap() {
        return classMap;
    }

    public ConcurrentMap<String, Portal> getPortalMap() {
        return portalMap;
    }

    public List<Portal> getPortals() {
        return portals;
    }

    private FileConfiguration getPortalsFile() {
        return NECore.getFileManager().get(FileType.PORTAL);
    }

    private void savePortalsFile() {
        NECore.getFileManager().save(FileType.PORTAL, true);
    }
}
