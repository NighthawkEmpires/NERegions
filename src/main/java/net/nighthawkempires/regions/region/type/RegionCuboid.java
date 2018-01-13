package net.nighthawkempires.regions.region.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.nighthawkempires.core.NECore;
import net.nighthawkempires.core.file.FileType;
import net.nighthawkempires.regions.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class RegionCuboid extends Region {

    private String world;
    private int x;
    private int y;
    private int z;
    private int xx;
    private int yy;
    private int zz;
    private List<Chunk> chunks;

    public Region loadRegion(String name) {
        ConfigurationSection region = getRegionsFile().getConfigurationSection("regions." + name);
        ConfigurationSection pos1 = region.getConfigurationSection("pos-1");
        ConfigurationSection pos2 = region.getConfigurationSection("pos-2");
        String world = region.getString("world");
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

    public boolean inRegion(Location location) {
        if (!location.getWorld().getName().toLowerCase().equals(world.toLowerCase())) {
            return false;
        } else {
            int xxx = location.getBlockX();
            int yyy = location.getBlockY();
            int zzz = location.getBlockZ();
            return x <= xxx && xxx <= xx && y <= yyy && yyy <= yy && z <= zzz && zzz <= zz;
        }
    }

    public boolean containsChunk(Chunk chunk) {
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                int y = 64;
                Block block = chunk.getBlock(x, y, z);
                if (inRegion(block.getLocation())) {
                    return true;
                }
            }
        }
        return false;
    }

    private FileConfiguration getRegionsFile() {
        return NECore.getFileManager().get(FileType.REGION);
    }

    private void saveRegionsFile() {
        NECore.getFileManager().save(FileType.REGION, true);
    }
}
