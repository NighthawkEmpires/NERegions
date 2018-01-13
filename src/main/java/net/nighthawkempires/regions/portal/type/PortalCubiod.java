package net.nighthawkempires.regions.portal.type;

import net.nighthawkempires.core.NECore;
import net.nighthawkempires.core.file.FileType;
import net.nighthawkempires.regions.portal.Portal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class PortalCubiod extends Portal {

    private String world;
    private int x;
    private int y;
    private int z;
    private int xx;
    private int yy;
    private int zz;

    public Portal loadPortal(String name) {
        ConfigurationSection portal = getPortalsFile().getConfigurationSection("portals." + name);
        ConfigurationSection pos1 = portal.getConfigurationSection("pos-1");
        ConfigurationSection pos2 = portal.getConfigurationSection("pos-2");
        String world = portal.getString("world");
        int x1 = pos1.getInt("cord-x"), x2 = pos2.getInt("cord-x"), y1 = pos1.getInt("cord-y"),
                y2 = pos2.getInt("cord-y"), z1 = pos1.getInt("cord-z"), z2 = pos2.getInt("cord-z");

        if (x1 < x2) {
            x = x1;
            xx = x2;
        } else {
            x = x2;
            xx = x1;
        }

        if(y1 < y2) {
            y = y1;
            yy = y2;
        } else {
            y = y2;
            yy = y1;
        }

        if(z1 < z2) {
            z = z1;
            zz = z2;
        } else {
            z = z2;
            zz = z1;
        }

        if (Bukkit.getWorlds().contains(Bukkit.getWorld(world))) {
            this.world = Bukkit.getWorld(world).getName();
        } else {
            this.world = null;
        }
        return this;
    }

    public boolean inPortal(Location location) {
        if (!location.getWorld().getName().toLowerCase().equals(world.toLowerCase())) {
            return false;
        } else {
            int xxx = location.getBlockX();
            int yyy = location.getBlockY();
            int zzz = location.getBlockZ();
            return x <= xxx && xxx <= xx && y <= yyy && yyy <= yy && z <= zzz && zzz <= zz;
        }
    }

    private FileConfiguration getPortalsFile() {
        return NECore.getFileManager().get(FileType.PORTAL);
    }

    private void savePortalsFile() {
        NECore.getFileManager().save(FileType.PORTAL, true);
    }
}
