package net.nighthawkempires.regions.selection;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SelectionClipboard {

    private UUID uuid;
    private boolean confirming;
    private boolean selecting;
    private ItemStack slotRestore;
    private Location position1;
    private Location position2;
    private SelectionType type;
    private String name;

    public SelectionClipboard(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isConfirming() {
        return confirming;
    }

    public void setConfirming(boolean confirming) {
        this.confirming = confirming;
    }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    public ItemStack getSlotRestore() {
        return slotRestore;
    }

    public void setSlotRestore(ItemStack slotRestore) {
        this.slotRestore = slotRestore;
    }

    public Location getPosition1() {
        return position1;
    }

    public void setPosition1(Location position1) {
        this.position1 = position1;
    }

    public Location getPosition2() {
        return position2;
    }

    public void setPosition2(Location position2) {
        this.position2 = position2;
    }

    public SelectionType getType() {
        return type;
    }

    public void setType(SelectionType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
