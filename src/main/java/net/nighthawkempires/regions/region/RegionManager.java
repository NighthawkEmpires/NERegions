package net.nighthawkempires.regions.region;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.core.NECore;
import net.nighthawkempires.core.file.FileType;
import net.nighthawkempires.regions.NERegions;
import net.nighthawkempires.regions.region.flag.RegionFlag;
import net.nighthawkempires.regions.region.type.RegionCuboid;
import net.nighthawkempires.regions.region.type.RegionType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class RegionManager {

    private ConcurrentMap<String, Class<? extends Region>> classMap;
    private ConcurrentMap<String, Region> regionMap;
    private List<Region> regions;

    public RegionManager() {
        classMap = Maps.newConcurrentMap();
        regionMap = Maps.newConcurrentMap();
        regions = Lists.newArrayList();

        classMap.put(RegionType.CUBOID.name(), RegionCuboid.class);
    }

    public void loadRegions() {
        Set<String> sections = getRegionsFile().getConfigurationSection("regions").getKeys(false);
        if (sections != null) {
            for (String string : sections) {
                loadRegion(string);
            }
        }
        NECore.getLoggers().info(NERegions.getPlugin(), "Loaded a total of " + regions.size() + " regions.");
    }

    public void loadRegion(String name) {
        ConfigurationSection section = getRegionsFile().getConfigurationSection("regions." + name);
        RegionType type = (section.isSet("type") ? RegionType.valueOf(section.getString("type").toUpperCase()) : null);
        if (type == null) {
            NECore.getLoggers().warn(NERegions.getPlugin(), "\'" + name + "\' does not have a valid region type set.");
            return;
        }

        Class clazz = getClassMap().get(type.name());
        Region region;
        try {
            region = (Region) clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            NECore.getLoggers().warn(NERegions.getPlugin(), "Could not load region: \'" + name + "\'");
            return;
        }

        try {
            region.create(name);
            getRegionMap().put(name, region);
            getRegions().add(region);
        } catch (Exception e) {
            NECore.getLoggers().warn(NERegions.getPlugin(), "Failed to load region: \'" + name + "\'");
        }
    }

    public void saveRegions() {
        for (String section : getRegionsFile().getConfigurationSection("regions").getKeys(false)) {
            if (!exists(section)) {
                getRegionsFile().set("regions." + section, null);
                saveRegionsFile();
            }
        }

        if (getRegions().size() != 0) {
            for (Region region : getRegions()) {
                getRegionsFile().set("regions." + region.getName() + ".flags", null);
                saveRegionsFile();
                for (RegionFlag flag : RegionFlag.values()) {
                    if (region.getResult(flag) != RegionFlag.Result.IGNORE) {
                        getRegionsFile().set("regions." + region.getName() + ".flags." + flag.name(), region.getResult(flag).name());
                        saveRegionsFile();
                    }
                }
            }
        }
    }

    public void createRegion(String name, Location pos1, Location pos2) {
            getRegionsFile().set("regions." + name + ".type", RegionType.CUBOID.name());
            getRegionsFile().set("regions." + name + ".priority", 1);
            getRegionsFile().set("regions." + name + ".world", pos1.getWorld().getName());
            getRegionsFile().set("regions." + name + ".pos-1.cord-x", pos1.getBlockX());
            getRegionsFile().set("regions." + name + ".pos-1.cord-y", pos1.getBlockY());
            getRegionsFile().set("regions." + name + ".pos-1.cord-z", pos1.getBlockZ());
            getRegionsFile().set("regions." + name + ".pos-2.cord-x", pos2.getBlockX());
            getRegionsFile().set("regions." + name + ".pos-2.cord-y", pos2.getBlockY());
            getRegionsFile().set("regions." + name + ".pos-2.cord-z", pos2.getBlockZ());
            saveRegionsFile();

            loadRegion(name);
    }

    public void deleteRegion(String name) {
        if (exists(name)) {
            getRegionsFile().set("regions." + name, null);
            saveRegionsFile();
            getRegions().remove(getRegion(name));
            getRegionMap().remove(getRegion(name).getName());
        }
    }

    public Region getRegion(String name) {
        for (String string : regionMap.keySet()) {
            if (string.toLowerCase().equals(name.toLowerCase())) {
                return regionMap.get(string);
            }
        }
        return null;
    }

    public boolean exists(String name) {
        for (String string : regionMap.keySet()) {
            if (string.toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public ConcurrentMap<String, Class<? extends Region>> getClassMap() {
        return classMap;
    }

    public ConcurrentMap<String, Region> getRegionMap() {
        return regionMap;
    }

    public List<Region> getRegions() {
        return regions;
    }

    private FileConfiguration getRegionsFile() {
        return NECore.getFileManager().get(FileType.REGION);
    }

    private void saveRegionsFile() {
        NECore.getFileManager().save(FileType.REGION, true);
    }
}
