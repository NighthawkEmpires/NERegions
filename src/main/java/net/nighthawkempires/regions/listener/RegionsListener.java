package net.nighthawkempires.regions.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.core.language.Lang;
import net.nighthawkempires.core.utils.LocationUtil;
import net.nighthawkempires.regions.NERegions;
import net.nighthawkempires.regions.region.Region;
import net.nighthawkempires.regions.region.flag.RegionFlag;
import net.nighthawkempires.regions.selection.SelectionClipboard;
import net.nighthawkempires.regions.selection.SelectionType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RegionsListener implements Listener {

    private HashMap<Region, List<UUID>> playerMap = Maps.newHashMap();
    private List<Region> regions = Lists.newArrayList();

    public Region obeyRegion(Location location) {
        regions.clear();
        for (Region region : NERegions.getRegionManager().getRegions()) {
            if (region.inRegion(location)) {
                regions.add(region);
            }
        }

        int highestPriority = 0;
        if (regions.size() != 0) {
            for (Region region : regions) {
                if (highestPriority < region.getPriority()) {
                    highestPriority = region.getPriority();
                }
            }
        }
        for (Region region : regions) {
            if (region.getPriority() == highestPriority) {
                return region;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("ne.regions")) {
            if (player.getItemInHand().equals(NERegions.getSelectionTool())) {
                if (NERegions.getSelectionManager().clipboards.containsKey(player.getUniqueId())) {
                    SelectionClipboard clipboard = NERegions.getSelectionManager().getClipboard(player.getUniqueId());
                    if (clipboard.isSelecting() && clipboard.getType() == SelectionType.REGION) {
                        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            Location location = event.getClickedBlock().getLocation();
                            location.setY(0.0);
                            clipboard.setPosition1(location);
                            player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have selected position 1 at " + LocationUtil.getLocationNameColored(location) + ChatColor.GRAY + "."));
                            if (clipboard.getPosition1() != null && clipboard.getPosition2() != null) {
                                clipboard.setSelecting(false);
                                clipboard.setConfirming(true);
                                player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "yes" + ChatColor.GRAY + " or " + ChatColor.RED + "no " + ChatColor.GRAY
                                        + "to confirm the creation of this region."));
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            Location location = event.getClickedBlock().getLocation();
                            location.setY(256.0);
                            clipboard.setPosition2(location);
                            player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have selected position 2 at " + LocationUtil.getLocationNameColored(location) + ChatColor.GRAY + "."));
                            if (clipboard.getPosition1() != null && clipboard.getPosition2() != null) {
                                clipboard.setSelecting(false);
                                clipboard.setConfirming(true);
                                player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "yes" + ChatColor.GRAY + " or " + ChatColor.RED + "no " + ChatColor.GRAY
                                        + "to confirm the creation of this region."));
                            }
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (obeyRegion(event.getClickedBlock().getLocation()) != null) {
                Region region = obeyRegion(event.getClickedBlock().getLocation());
                if (event.getClickedBlock().getType().name().toLowerCase().contains("button")) {
                    if (region.getResult(RegionFlag.INTERACT_BUTTON) == RegionFlag.Result.DENY) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You can not interact with that in this region!"));
                        event.setCancelled(true);
                    }
                } else if (event.getClickedBlock().getType().name().toLowerCase().contains("ender_chest")) {
                    if (region.getResult(RegionFlag.INTERACT_ENDER_CHEST) == RegionFlag.Result.DENY) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You can not interact with that in this region!"));
                        event.setCancelled(true);
                    }
                } else if (event.getClickedBlock().getType().name().toLowerCase().contains("chest") || event.getClickedBlock().getType().name().toLowerCase().contains("shulker_box")) {
                    if (region.getResult(RegionFlag.INTERACT_CHEST) == RegionFlag.Result.DENY) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You can not interact with that in this region!"));
                        event.setCancelled(true);
                    }
                } else if (event.getClickedBlock().getType().name().toLowerCase().contains("lever")) {
                    if (region.getResult(RegionFlag.INTERACT_LEVER) == RegionFlag.Result.DENY) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You can not interact with that in this region!"));
                        event.setCancelled(true);
                    }
                } else if (event.getClickedBlock().getType().name().toLowerCase().contains("door")) {
                    if (region.getResult(RegionFlag.INTERACT_DOOR) == RegionFlag.Result.DENY) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You can not interact with that in this region!"));
                        event.setCancelled(true);
                    }
                }
            }
        } else if (event.getAction() == Action.PHYSICAL) {
            if (obeyRegion(event.getClickedBlock().getLocation()) != null) {
                Region region = obeyRegion(event.getClickedBlock().getLocation());
                if (event.getClickedBlock().getType().name().toLowerCase().contains("soil")) {
                    if (region.getResult(RegionFlag.CROP_TRAMPLE) == RegionFlag.Result.DENY) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("ne.regions")) {
            if (NERegions.getSelectionManager().clipboards.containsKey(player.getUniqueId())) {
                SelectionClipboard clipboard = NERegions.getSelectionManager().getClipboard(player.getUniqueId());
                if (clipboard.isConfirming() && clipboard.getType() == SelectionType.REGION) {
                    if (event.getMessage().toLowerCase().contains("yes")) {
                        NERegions.getRegionManager().createRegion(clipboard.getName(), clipboard.getPosition1(), clipboard.getPosition2());
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have created region " + ChatColor.BLUE + clipboard.getName() + ChatColor.GRAY + "."));
                    } else if (event.getMessage().toLowerCase().contains("no")) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have cancelled creation of region " + ChatColor.BLUE + clipboard.getName() + ChatColor.GRAY + "."));
                    }
                    player.setItemInHand(clipboard.getSlotRestore());
                    NERegions.getSelectionManager().deleteClipboard(player.getUniqueId());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrain(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (obeyRegion(player.getLocation()) != null) {
                Region region = obeyRegion(player.getLocation());
                if (region.getResult(RegionFlag.HUNGER_DEGEN) == RegionFlag.Result.DENY) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        for (Region region : NERegions.getRegionManager().getRegions()) {
            if (obeyRegion(player.getLocation()) == region) {
                if (region.inRegion(player)) {
                    if (!region.getInside().contains(player.getUniqueId())) {
                        region.getInside().add(player.getUniqueId());
                    }
                } else {
                    if (region.getInside().contains(player.getUniqueId())) {
                        region.getInside().remove(player.getUniqueId());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            if (obeyRegion(((Player) event.getEntity().getShooter()).getLocation()) != null) {
                Region region = obeyRegion(((Player) event.getEntity().getShooter()).getLocation());
                if (region.getResult(RegionFlag.PROJECTILES) == RegionFlag.Result.DENY) {
                    event.getEntity().remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            if (obeyRegion(((Player) event.getEntity().getShooter()).getLocation()) != null) {
                Region region = obeyRegion(((Player) event.getEntity().getShooter()).getLocation());
                if (region.getResult(RegionFlag.PROJECTILES) == RegionFlag.Result.DENY) {
                    event.getEntity().remove();
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        for (Region region : NERegions.getRegionManager().getRegions()) {
            if (obeyRegion(event.getBlock().getLocation()) == region) {
                if (region.inRegion(event.getBlock().getLocation())) {
                    if (region.getResult(RegionFlag.BREAK) == RegionFlag.Result.DENY) {
                        if (!region.getBypass().contains(player.getUniqueId())) {
                            player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You are not allowed to break blocks in this region."));
                            event.setCancelled(true);
                        }
                    } else if (region.getResult(RegionFlag.BREAK) == RegionFlag.Result.ALLOW) {
                        event.setCancelled(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        for (Region region : NERegions.getRegionManager().getRegions()) {
            if (obeyRegion(event.getBlock().getLocation()) == region) {
                if (region.inRegion(event.getBlock().getLocation())) {
                    if (region.getResult(RegionFlag.BUILD) == RegionFlag.Result.DENY) {
                        if (!region.getBypass().contains(player.getUniqueId())) {
                            player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You are not allowed to place blocks in this region."));
                            event.setCancelled(true);
                        }
                    } else if (region.getResult(RegionFlag.BUILD) == RegionFlag.Result.ALLOW) {
                        event.setCancelled(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPrime(ExplosionPrimeEvent event) {
        for (Region region : NERegions.getRegionManager().getRegions()) {
            if (obeyRegion(event.getEntity().getLocation()) == region) {
                if (region.inRegion(event.getEntity().getLocation())) {
                    if (region.getResult(RegionFlag.TNT) == RegionFlag.Result.DENY) {
                        if (event.getEntity() instanceof LivingEntity) {
                            LivingEntity entity = (LivingEntity) event.getEntity();
                            entity.setHealth(0.0);
                        }
                        event.setCancelled(true);
                        event.setFire(false);
                        event.setRadius(0);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            for (Region region : NERegions.getRegionManager().getRegions()) {
                if (obeyRegion(player.getLocation()) == region) {
                    if (region.inRegion(player)) {
                        if (region.getResult(RegionFlag.DAMAGE) == RegionFlag.Result.DENY) {
                            event.setDamage(0.0);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            for (Region region : NERegions.getRegionManager().getRegions()) {
                if (obeyRegion(player.getLocation()) == region) {
                    if (region.inRegion(player)) {
                        if (region.getResult(RegionFlag.PVP) == RegionFlag.Result.DENY) {
                            event.setDamage(0.0);
                            if (event.getDamager() instanceof Player) {
                                event.getDamager().sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You are not allowed to PvP in this region!"));
                            }
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            if (!(event.getEntity() instanceof Item)) {
                for (Region region : NERegions.getRegionManager().getRegions()) {
                    if (obeyRegion(event.getLocation()) == region) {
                        if (region.inRegion(event.getLocation())) {
                            if (region.getResult(RegionFlag.MOB_SPAWN) == RegionFlag.Result.DENY) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        for (Region region : NERegions.getRegionManager().getRegions()) {
            if (obeyRegion(event.getBlock().getLocation()) == region) {
                if (region.inRegion(event.getBlock().getLocation())) {
                    if (region.getResult(RegionFlag.FIRE_SPREAD) == RegionFlag.Result.DENY) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() == Material.FIRE || event.getBlock().getType() == Material.FIRE) {
            for (Region region : NERegions.getRegionManager().getRegions()) {
                if (obeyRegion(event.getSource().getLocation()) == region || obeyRegion(event.getBlock().getLocation()) == region) {
                    if (region.inRegion(event.getSource().getLocation()) || region.inRegion(event.getBlock().getLocation())) {
                        if (region.getResult(RegionFlag.FIRE_SPREAD) == RegionFlag.Result.DENY) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            for (Region region : NERegions.getRegionManager().getRegions()) {
                if (obeyRegion(event.getTo()) == region || obeyRegion(event.getFrom()) == region) {
                    if (region.inRegion(event.getFrom()) || region.inRegion(event.getTo())) {
                        if (region.getResult(RegionFlag.ENDERPEARL) == RegionFlag.Result.DENY) {
                            event.getPlayer().sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "You're not allowed to use ender pearls in this region!"));
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
