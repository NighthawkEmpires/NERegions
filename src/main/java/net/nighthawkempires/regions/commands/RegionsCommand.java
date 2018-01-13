package net.nighthawkempires.regions.commands;

import net.nighthawkempires.core.language.Lang;
import net.nighthawkempires.regions.NERegions;
import net.nighthawkempires.regions.region.Region;
import net.nighthawkempires.regions.region.flag.RegionFlag;
import net.nighthawkempires.regions.selection.SelectionClipboard;
import net.nighthawkempires.regions.selection.SelectionType;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegionsCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!sender.hasPermission("ne.regions")) {
                player.sendMessage(Lang.NO_PERM.getServerMessage());
                return true;
            }

            if (args.length == 0) {
                String[] help = new String[] {
                        Lang.HEADER.getServerHeader(),
                        Lang.CMD_NAME.getCommandName("regions"),
                        Lang.FOOTER.getMessage(),
                        Lang.CMD_HELP.getCommand("regions", "list", "List all regions"),
                        Lang.CMD_HELP.getCommand("regions", "bypass [region]", "Set it to bypass a regions flags"),
                        Lang.CMD_HELP.getCommand("regions", "create [region]", "Create a new region"),
                        Lang.CMD_HELP.getCommand("regions", "delete [region]", "Delete a region"),
                        Lang.CMD_HELP.getCommand("regions", "info [region]", "Show info for a region"),
                        Lang.CMD_HELP.getCommand("regions", "priority [region] [priority]", "Set a region's priority" ),
                        Lang.CMD_HELP.getCommand("regions", "setflag [region] [flag] [result]", "Set a flag for a region"),
                        Lang.FOOTER.getMessage(),
                };
                player.sendMessage(help);
                return true;
            } else if (args.length == 1) {
                if (args[0].toLowerCase().equals("list")) {
                    StringBuilder builder = new StringBuilder();
                    if (!NERegions.getRegionManager().getRegions().isEmpty()) {
                        for (Region region : NERegions.getRegionManager().getRegions()) {
                            builder.append(ChatColor.GREEN).append(region.getName()).append(ChatColor.DARK_GRAY).append(", ");
                        }
                    } else {
                        builder.append(ChatColor.RED).append("There are no available regions...");
                    }
                    String regions = builder.substring(0, builder.length() - 2);
                    String[] info = new String[] {
                            Lang.HEADER.getServerHeader(),
                            Lang.LIST.getListName("Regions"),
                            Lang.FOOTER.getMessage(),
                            ChatColor.DARK_GRAY + " - " + regions,
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
                    if (NERegions.getRegionManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region already exists!"));
                        return true;
                    }

                    SelectionClipboard clipboard = NERegions.getSelectionManager().getClipboard(player.getUniqueId());
                    clipboard.setSlotRestore(player.getItemInHand());
                    clipboard.setSelecting(true);
                    clipboard.setType(SelectionType.REGION);
                    clipboard.setName(name);
                    player.setItemInHand(NERegions.getSelectionTool());
                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "Select the two location in which you want the region to be located within.  After you are done with selection" +
                            " the item you had in hand will be restored."));
                } else if (args[0].toLowerCase().equals("bypass")) {
                    String name = args[1];
                    if (!NERegions.getRegionManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    Region region = NERegions.getRegionManager().getRegion(name);
                    if (region.getBypass().contains(player.getUniqueId())) {
                        region.getBypass().remove(player.getUniqueId());
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have " + ChatColor.RED + "" + ChatColor.UNDERLINE + "disabled " + ChatColor.GRAY + "bypass for region "
                                + ChatColor.BLUE + region.getName() + ChatColor.GRAY + "."));
                    } else {
                        region.getBypass().add(player.getUniqueId());
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have " + ChatColor.GREEN + "" + ChatColor.UNDERLINE + "enabled " + ChatColor.GRAY + "bypass for region "
                                + ChatColor.BLUE + region.getName() + ChatColor.GRAY + "."));
                    }
                } else if (args[0].toLowerCase().equals("delete")) {
                    String name = args[1];
                    if (!NERegions.getRegionManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You deleted region " + ChatColor.BLUE + NERegions.getRegionManager().getRegion(name).getName() + ChatColor.GRAY + "."));
                    NERegions.getRegionManager().deleteRegion(NERegions.getRegionManager().getRegion(name).getName());
                } else if (args[0].toLowerCase().equals("info")) {
                    String name = args[1];
                    if (!NERegions.getRegionManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    Region region = NERegions.getRegionManager().getRegion(name);
                    if (region == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    StringBuilder builder = new StringBuilder();
                    ChatColor color = ChatColor.GRAY;
                    for (RegionFlag flag : RegionFlag.values()) {
                        RegionFlag.Result result = region.getResult(flag);
                        if (result == RegionFlag.Result.ALLOW) {
                            color = ChatColor.GREEN;
                        } else if (result == RegionFlag.Result.DENY) {
                            color = ChatColor.RED;
                        } else if (result == RegionFlag.Result.IGNORE) {
                            color = ChatColor.GRAY;
                        }
                        builder.append(color).append(flag.name()).append(ChatColor.DARK_GRAY).append(", ");
                    }
                    String flags = builder.substring(0, builder.length() - 2);
                    String[] info = new String[]{
                            Lang.HEADER.getServerHeader(),
                            ChatColor.DARK_GRAY + "Region" + ChatColor.GRAY + ": " + ChatColor.BLUE + region.getName(),
                            Lang.FOOTER.getMessage(),
                            ChatColor.DARK_GRAY + "Region Type" + ChatColor.GRAY + ": " + ChatColor.GREEN + region.getType().name(),
                            ChatColor.DARK_GRAY + "Priority" + ChatColor.GRAY + ": " + ChatColor.GOLD + region.getPriority(),
                            ChatColor.DARK_GRAY + "Flags" + ChatColor.GRAY + ": " + flags,
                            Lang.FOOTER.getMessage(),
                    };
                    player.sendMessage(info);
                } else {
                    player.sendMessage(Lang.SYNTAX_ERROR.getServerMessage());
                    return true;
                }
            } else if (args.length == 3) {
                if (args[0].toLowerCase().equals("priority")) {
                    String name = args[1];
                    if (!NERegions.getRegionManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    Region region = NERegions.getRegionManager().getRegion(name);

                    if (region == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    if (!NumberUtils.isDigits(args[2])) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That's not a valid integer!"));
                        return true;
                    }

                    int priority = Integer.parseInt(args[2]);
                    region.setPriority(priority);
                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have set the priority in region " + ChatColor.BLUE + region.getName() + ChatColor.GRAY + " to " + ChatColor.GOLD + priority + ChatColor.GRAY + "."));
                }
            } else if (args.length == 4) {
                if (args[0].toLowerCase().equals("setflag")) {
                    String name = args[1];
                    if (!NERegions.getRegionManager().exists(name)) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    Region region = NERegions.getRegionManager().getRegion(name);

                    if (region == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region does not exist!"));
                        return true;
                    }

                    RegionFlag flag = null;
                    for (RegionFlag flags : RegionFlag.values()) {
                        if (flags.name().toLowerCase().equals(args[2].toLowerCase())) {
                            flag = flags;
                        }
                    }

                    if (flag == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That region flag does not exist!"));
                        return true;
                    }

                    RegionFlag.Result result = null;
                    for (RegionFlag.Result results : RegionFlag.Result.values()) {
                        if (results.name().toLowerCase().equals(args[3].toLowerCase())) {
                            result = results;
                        }
                    }

                    if (result == null) {
                        player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.RED + "That is not a valid result, the results are ALLOW, DENY, and IGNORE."));
                        return true;
                    }

                    ChatColor color = ChatColor.GRAY;
                    if (result == RegionFlag.Result.ALLOW) {
                        color = ChatColor.GREEN;
                    } else if (result == RegionFlag.Result.DENY) {
                        color = ChatColor.RED;
                    } else if (result == RegionFlag.Result.IGNORE) {
                        color = ChatColor.GRAY;
                    }

                    region.setFlag(flag, result);
                    player.sendMessage(Lang.CHAT_TAG.getServerMessage(ChatColor.GRAY + "You have set region flag " + ChatColor.BLUE + flag.name() + ChatColor.GRAY + " to " + color + result.name()
                            + ChatColor.GRAY + "."));
                } else {
                    player.sendMessage(Lang.SYNTAX_ERROR.getServerMessage());
                    return true;
                }
            } else {
                player.sendMessage(Lang.SYNTAX_ERROR.getServerMessage());
                return true;
            }
        }
        return true;
    }
}
