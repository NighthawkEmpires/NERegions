package net.nighthawkempires.regions.portal.type;

import net.nighthawkempires.regions.portal.Portal;

public enum  PortalType {

    CUBOID(new PortalCubiod(), PortalCubiod.class);

    private Portal portal;
    private Class clazz;

    PortalType(Portal portal, Class clazz) {
        this.portal = portal;
        this.clazz = clazz;
    }

    public Portal getPortal() {
        return portal;
    }

    public Class getClazz() {
        return clazz;
    }}
