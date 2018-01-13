package net.nighthawkempires.regions.region;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.core.NECore;
import net.nighthawkempires.core.file.FileType;
import net.nighthawkempires.regions.NERegions;
import net.nighthawkempires.regions.region.flag.RegionFlag;
import net.nighthawkempires.regions.region.type.RegionType;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public abstract class Region implements Comparable<Region> {

    private int priority;
    private String name;
    private RegionType type;
    private ConcurrentMap<RegionFlag, RegionFlag.Result> flags;
    private List<UUID> bypass;
    private List<UUID> inside;

    public final void create(String name) {
        this.name = name;
        ConfigurationSection section = getRegionsFile().getConfigurationSection("regions." + name);
        this.priority = (section.isSet("priority") ? section.getInt("priority") : 0);
        this.type = (section.isSet("type") ? RegionType.valueOf(section.getString("type").toUpperCase()) : RegionType.CUBOID);
        flags = Maps.newConcurrentMap();
        if (section.isSet("flags")) {
            section = section.getConfigurationSection("flags");
            for (RegionFlag flag : RegionFlag.values()) {
                RegionFlag.Result result = RegionFlag.Result.IGNORE;
                if (section.isSet(flag.name())) {
                    result = RegionFlag.Result.valueOf(section.getString(flag.name()).toUpperCase());
                }
                flags.put(flag, result);
            }
        } else {
            for (RegionFlag flag : RegionFlag.values()) {
                flags.put(flag, RegionFlag.Result.IGNORE);
            }
        }
        bypass = Lists.newArrayList();
        inside = Lists.newArrayList();
        loadRegion(name);
    }

    public abstract Region loadRegion(String name);

    public abstract boolean inRegion(Location location);

    public boolean inRegion(Player player) {
        return inRegion(player.getLocation());
    }

    public RegionFlag.Result getResult(RegionFlag flag) {
        if (flags.containsKey(flag)) {
            return flags.get(flag);
        }
        return RegionFlag.Result.IGNORE;
    }

    public void setFlag(RegionFlag flag, RegionFlag.Result result) {
        if (flags.containsKey(flag)) {
            flags.remove(flag);
        }

        flags.put(flag, result);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public RegionType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ConcurrentMap<RegionFlag, RegionFlag.Result> getFlags() {
        return flags;
    }

    public int getPriority() {
        return priority;
    }

    public List<UUID> getBypass() {
        return bypass;
    }

    public List<UUID> getInside() {
        return inside;
    }

    public int compareTo(Region region) {
        return this.getPriority() < region.getPriority() ? 1 : (this.getPriority() > region.getPriority() ? -1 : this.getName().compareTo(region.getName()));
    }

    private FileConfiguration getRegionsFile() {
        return NECore.getFileManager().get(FileType.REGION);
    }

    private void saveRegionsFile() {
        NECore.getFileManager().save(FileType.REGION, true);
    }
}
