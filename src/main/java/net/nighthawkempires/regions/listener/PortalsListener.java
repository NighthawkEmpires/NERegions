package net.nighthawkempires.regions.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.nighthawkempires.core.language.Lang;
import net.nighthawkempires.core.utils.CooldownUtil;
import net.nighthawkempires.core.utils.LocationUtil;
import net.nighthawkempires.regions.NERegions;
import net.nighthawkempires.regions.portal.Portal;
import net.nighthawkempires.regions.portal.destination.DestinationType;
import net.nighthawkempires.regions.selection.SelectionClipboard;
import net.nighthawkempires.regions.selection.SelectionType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PortalsListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("ne.portals")) {
            if (player.getItemInHand().equals(NERegions.getSelectionTool())) {
                if (NERegions.getSelectionManager().clipboards.containsKey(player.getUniqueId())) {
                    SelectionClipboard clipboard = NERegions.getSelectionManager().getClipboard(player.getUniqueId());
                    if (clipboard.isSelecting() && clipboard.getType() == SelectionType.PORTAL) {
                        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            Location location = event.getClickedBlock().getLocation();
                            clipboard.setPosition1(location);
                            player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have selected position 1 at " + LocationUtil.getLocationNameColored(location) + ChatColor.GRAY + "."));
                            if (clipboard.getPosition1() != null && clipboard.getPosition2() != null) {
                                clipboard.setSelecting(false);
                                clipboard.setConfirming(true);
                                player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "yes" + ChatColor.GRAY + " or " + ChatColor.RED + "no" + ChatColor.GRAY
                                        + " to confirm the creation of this portal."));
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            Location location = event.getClickedBlock().getLocation();
                            clipboard.setPosition2(location);
                            player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have selected position 2 at " + LocationUtil.getLocationNameColored(location) + ChatColor.GRAY + "."));
                            if (clipboard.getPosition1() != null && clipboard.getPosition2() != null) {
                                clipboard.setSelecting(false);
                                clipboard.setConfirming(true);
                                player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "yes" + ChatColor.GRAY + " or " + ChatColor.RED + "no" + ChatColor.GRAY
                                        + " to confirm the creation of this portal."));
                            }
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("ne.portals")) {
            if (NERegions.getSelectionManager().clipboards.containsKey(player.getUniqueId())) {
                SelectionClipboard clipboard = NERegions.getSelectionManager().getClipboard(player.getUniqueId());
                if (clipboard.isConfirming() && clipboard.getType() == SelectionType.PORTAL) {
                    if (event.getMessage().toLowerCase().contains("yes")) {
                        NERegions.getPortalManager().createPortal(clipboard.getName(), clipboard.getPosition1(), clipboard.getPosition2());
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have created portal " + ChatColor.BLUE + clipboard.getName() + ChatColor.GRAY + "."));
                    } else if (event.getMessage().toLowerCase().contains("no")) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have cancelled create of portal " + ChatColor.BLUE + clipboard.getName() + ChatColor.GRAY + "."));
                    }
                    player.setItemInHand(clipboard.getSlotRestore());
                    NERegions.getSelectionManager().deleteClipboard(player.getUniqueId());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() == Material.WATER || event.getSource().getType() == Material.STATIONARY_WATER || event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.STATIONARY_WATER) {
            for (Portal portal : NERegions.getPortalManager().getPortals()) {
                if (portal.inPortal(event.getBlock().getLocation()) || portal.inPortal(event.getSource().getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        for (Portal portal : NERegions.getPortalManager().getPortals()) {
            if (portal.inPortal(player)) {
                if (portal.getInside().contains(player.getUniqueId())) {
                    return;
                }
                if (!CooldownUtil.cooledDown(player.getUniqueId(), portal.getName())) {
                    return;
                }
                if (portal.getDestinationType() == null) {
                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "This portal does not have a destination set!"));
                    return;
                }

                if (portal.getDestinationType() == DestinationType.WARP) {
                    if (!LocationUtil.warpExists(portal.getDestination())) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "This portal does not have a destination set!"));
                        return;
                    }

                    portal.getInside().add(player.getUniqueId());
                    player.teleport(LocationUtil.getWarp(portal.getDestination()));
                    portal.getInside().remove(player.getUniqueId());
                } else if (portal.getDestinationType() == DestinationType.SERVER) {
                    try {
                        portal.getInside().add(player.getUniqueId());
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(portal.getDestination());
                        player.sendPluginMessage(NERegions.getPlugin(), "BungeeCord", out.toByteArray());
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "Sending you to Server " + ChatColor.BLUE + portal.getDestination() + ChatColor.GRAY + "."));
                        CooldownUtil.setCooldown(player.getUniqueId(), portal.getName(), 5);
                        portal.getInside().remove(player.getUniqueId());
                    } catch (Exception e) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "This portal does not have a destination set!"));
                        return;
                    }
                }
            }
        }
    }
}
