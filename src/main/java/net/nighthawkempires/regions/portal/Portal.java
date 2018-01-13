package net.nighthawkempires.regions.portal;

import com.google.common.collect.Lists;
import net.nighthawkempires.core.NECore;
import net.nighthawkempires.core.file.FileType;
import net.nighthawkempires.regions.portal.destination.DestinationType;
import net.nighthawkempires.regions.portal.type.PortalType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public abstract class Portal implements Comparable<Portal>{

    private int priority;
    private String name;
    private PortalType type;
    private DestinationType destinationType;
    private String destination;
    private List<UUID> inside;

    public final void create(String name) {
        this.name = name;
        ConfigurationSection section = getPortalsFile().getConfigurationSection("portals." + name);
        this.priority = (section.isSet("priority") ? section.getInt("priority") : 0);
        this.type = (section.isSet("type") ? PortalType.valueOf(section.getString("type").toUpperCase()) : PortalType.CUBOID);
        try {
            this.destinationType = DestinationType.valueOf(section.getString("destination-type").toUpperCase());
            this.destination = section.getString("destination");
        } catch (Exception e) {}
        inside = Lists.newArrayList();
        loadPortal(name);
    }

    public abstract Portal loadPortal(String name);

    public abstract boolean inPortal(Location location);

    public boolean inPortal(Player player) {
        return inPortal(player.getLocation());
    }

    public PortalType getType() {
        return type;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public List<UUID> getInside() {
        return inside;
    }

    public int compareTo(Portal portal) {
        return this.getPriority() < portal.getPriority() ? 1 : (this.getPriority() > portal.getPriority() ? -1 : this.getName().compareTo(portal.getName()));
    }

    private FileConfiguration getPortalsFile() {
        return NECore.getFileManager().get(FileType.PORTAL);
    }

    private void savePortalsFile() {
        NECore.getFileManager().save(FileType.PORTAL, true);
    }
}
