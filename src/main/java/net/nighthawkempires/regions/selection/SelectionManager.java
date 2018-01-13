package net.nighthawkempires.regions.selection;

import com.google.common.collect.Maps;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class SelectionManager {

    public ConcurrentMap<UUID, SelectionClipboard> clipboards;

    public SelectionManager() {
        clipboards = Maps.newConcurrentMap();
    }

    public SelectionClipboard getClipboard(UUID uuid) {
        if (clipboards.containsKey(uuid)) {
            return clipboards.get(uuid);
        } else {
            SelectionClipboard clipboard = new SelectionClipboard(uuid);
            clipboards.put(uuid, clipboard);
            return clipboards.get(uuid);
        }
    }

    public void deleteClipboard(UUID uuid) {
        if (clipboards.containsKey(uuid)) {
            clipboards.remove(uuid);
        }
    }
}
