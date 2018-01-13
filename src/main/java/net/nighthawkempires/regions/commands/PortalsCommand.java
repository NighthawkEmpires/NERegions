package net.nighthawkempires.regions.commands;

import net.nighthawkempires.core.language.Lang;
import net.nighthawkempires.regions.NERegions;
import net.nighthawkempires.regions.portal.Portal;
import net.nighthawkempires.regions.portal.destination.DestinationType;
import net.nighthawkempires.regions.selection.SelectionClipboard;
import net.nighthawkempires.regions.selection.SelectionType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortalsCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("ne.portals")) {
                player.sendMessage(Lang.NO_PERM.getServerMessage());
                return true;
            }

            if (args.length == 0) {
                String[] help = new String[] {
                        Lang.HEADER.getServerHeader(),
                        Lang.CMD_NAME.getCommandName("portals"),
                        Lang.FOOTER.getMessage(),
                        Lang.CMD_HELP.getCommand("portals", "list", "List all portals"),
                        Lang.CMD_HELP.getCommand("portals", "create [portal]", "Create a portal"),
                        Lang.CMD_HELP.getCommand("portals", "delete [portal]", "Delete a portal"),
                        Lang.CMD_HELP.getCommand("portals", "info [portal]", "Show info for a portal"),
                        Lang.CMD_HELP.getCommand("portals", "setdest [portal] [type] [destination]", "Set a portal's destination"),
                        Lang.FOOTER.getMessage(),
                };
                player.sendMessage(help);
            } else if (args.length == 1) {
                if (args[0].toLowerCase().equals("list")) {
                    StringBuilder builder = new StringBuilder();
                    if (!NERegions.getPortalManager().getPortals().isEmpty()) {
                        for (Portal portal : NERegions.getPortalManager().getPortals()) {
                            builder.append(ChatColor.GREEN).append(portal.getName()).append(ChatColor.DARK_GRAY).append(", ");
                        }
                    } else {
                        builder.append(ChatColor.RED).append("There are no available portals...");
                    }
                    String portals = builder.substring(0, builder.length() - 2);
                    String[] info = new String[] {
                            Lang.HEADER.getServerHeader(),
                            Lang.LIST.getListName("Portals"),
                            Lang.FOOTER.getMessage(),
                            ChatColor.DARK_GRAY + " - " + portals,
                            Lang.FOOTER.getMessage(),
                    };
                    player.sendMessage(info);
                } else {
                    player.sendMessage(Lang.SYNTAX_ERROR.getServerMessage());
                    return true;
                }
            } else if (args.length == 2) {
                if (args[0].toLowerCase().equals("create")) {
                    String name = args[1];
                    if (NERegions.getPortalManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That portal already exists!"));
                        return true;
                    }

                    SelectionClipboard clipboard = NERegions.getSelectionManager().getClipboard(player.getUniqueId());
                    clipboard.setSlotRestore(player.getItemInHand());
                    clipboard.setSelecting(true);
                    clipboard.setType(SelectionType.PORTAL);
                    clipboard.setName(name);
                    player.setItemInHand(NERegions.getSelectionTool());
                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "Select the two location in which you want the portal to be located within.  After you are done with selection" +
                            " the item you had in hand will be restored."));
                } else if (args[0].toLowerCase().equals("delete")) {
                    String name = args[1];
                    if (!NERegions.getPortalManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That portal does not exist!"));
                        return true;
                    }

                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You deleted portal " + ChatColor.BLUE + NERegions.getPortalManager().getPortal(name).getName() + ChatColor.GRAY + "."));
                    NERegions.getPortalManager().deletePortal(NERegions.getPortalManager().getPortal(name).getName());
                } else if (args[0].toLowerCase().equals("info")) {
                    String name = args[1];
                    if (!NERegions.getPortalManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That portal does not exist!"));
                        return true;
                    }

                    Portal portal = NERegions.getPortalManager().getPortal(name);
                    if (portal == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That portal does not exist!"));
                        return true;
                    }

                    String[] info = new String[] {
                            Lang.HEADER.getServerHeader(),
                            ChatColor.DARK_GRAY + "Portal" + ChatColor.GRAY + ": " + ChatColor.BLUE + portal.getName(),
                            Lang.FOOTER.getMessage(),
                            ChatColor.DARK_GRAY + "Portal Type" + ChatColor.GRAY + ": " + ChatColor.GREEN + portal.getType().name(),
                            ChatColor.DARK_GRAY + "Destination Type" + ChatColor.GRAY + ": " + ChatColor.GREEN + portal.getDestinationType().name(),
                            ChatColor.DARK_GRAY + "Destination" + ChatColor.GRAY + ": " + ChatColor.BLUE + portal.getDestination(),
                            Lang.FOOTER.getMessage(),
                    };
                    player.sendMessage(info);
                }
            } else if (args.length == 4) {
                if (args[0].toLowerCase().equals("setdest")) {
                    String name = args[1];
                    if (!NERegions.getPortalManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That portal does not exist!"));
                        return true;
                    }

                    Portal portal = NERegions.getPortalManager().getPortal(name);
                    if (portal == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That portal does not exist!"));
                        return true;
                    }

                    DestinationType type = null;
                    for (DestinationType types : DestinationType.values()) {
                        if (types.name().toLowerCase().equals(args[2].toLowerCase())) {
                            type = types;
                        }
                    }

                    if (type == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That is not a valid destination type, the types are SERVER and WARP."));
                        return true;
                    }
                    String destination = args[3];

                    NERegions.getPortalManager().setDestination(portal.getName(), type, destination);
                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have set the destination for portal " + ChatColor.BLUE + portal.getName() + ChatColor.GRAY
                            + " to " + ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + type.name() + ChatColor.DARK_GRAY + "|" + ChatColor.YELLOW + destination + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY + "."));

                } else {
                    player.sendMessage(Lang.SYNTAX_ERROR.getServerMessage());
                    return true;
                }
            } else {
                player.sendMessage(Lang.SYNTAX_ERROR.getServerMessage());
            }
        }
        return true;
    }
}
