package net.nighthawkempires.regions.region.type;


import net.nighthawkempires.regions.region.Region;

public enum RegionType {

    CUBOID(new RegionCuboid(), RegionCuboid.class);

    private Region region;
    private Class clazz;

    RegionType(Region region, Class clazz) {
        this.region = region;
        this.clazz = clazz;
    }

    public Region getRegion() {
        return region;
    }

    public Class getClazz() {
        return clazz;
    }
}
