package me.joedon;

import me.joedon.scoreboard.EPScoreboard;
import me.joedon.configs.TLUserConfigs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Commands implements CommandExecutor {

    private TabListPro plugin = JavaPlugin.getPlugin(TabListPro.class);
    private EPScoreboard epsb;
    Commands(EPScoreboard epsb){
        this.epsb = epsb;
    }

    private void commandReload() {
        plugin.getConfig().options().copyDefaults(false);
        plugin.reloadConfig();

        if (plugin.getConfig().getInt("header-interval") >= plugin.getConfig().getInt("footer-interval")) {
            HeaderFooter.fastestUpdateRateReq = plugin.getConfig().getInt("footer-interval");
        } else {
            HeaderFooter.fastestUpdateRateReq = plugin.getConfig().getInt("header-interval");
        }

        plugin.epsb.permOrder = new ArrayList<>(plugin.getConfig().getStringList("sortByPerms"));

        plugin.up.updatePlaceholderAPIPlaceholders();

        try {
            plugin.id.cancel();
        } catch (Exception ignored) { }

        plugin.up.resort();

        plugin.usb.updateboard();

        HeaderFooter.headerAnimation = new ArrayList<>(plugin.getConfig().getStringList("header"));
        HeaderFooter.footerAnimation = new ArrayList<>(plugin.getConfig().getStringList("footer"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            String uuid = p.getUniqueId().toString();
            if (p.hasPermission("tablistpro.reload")) {
                if (label.equalsIgnoreCase("tablistpro") || label.equalsIgnoreCase("tlp")) {
                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("reload")) {
                            commandReload();
                            p.sendMessage(ChatColor.GREEN + "TabListPro: Plugin successfully reloaded!");
                            return true;
                        } else {
                            p.sendMessage(ChatColor.RED + "[TabListPro] Usage: /tablistpro <set/reload> [player] [group]");
                            return true;
                        }
                    } else {
                        if (p.hasPermission("tablistpro.set")) {
                            if (args.length == 3) {
                                if (args[0].equalsIgnoreCase("set")) {
                                    if (Bukkit.getPlayer(args[1]) != null) {
                                        for (String keys : epsb.groupKeys) {
                                            if (keys.replaceAll("groups\\.", "").equalsIgnoreCase(args[2])) {
                                                TLUserConfigs cm = new TLUserConfigs(plugin, Bukkit.getPlayer(args[1]));
                                                FileConfiguration f = cm.getConfig();
                                                f.set("group", args[2]);
                                                cm.reload();
                                                cm.saveConfig();
                                                epsb.group.put(Bukkit.getPlayer(args[1]).getUniqueId().toString(), args[2]);
                                                plugin.up.checkGroupUpdate(Bukkit.getPlayer(args[1]));
                                                p.sendMessage(ChatColor.GREEN + "[TabListPro] Successfully set " + args[1] + "'s group to " + args[2] + "!");
                                                plugin.usb.updateboard();
                                                return true;
                                            }
                                        }
                                        p.sendMessage(ChatColor.RED + "[TabListPro] Group " + ChatColor.GOLD + args[2] + ChatColor.RED + " not found.");
                                        return true;
                                    } else {
                                        p.sendMessage(ChatColor.RED + "[TabListPro] The player " + args[1] + " is not online!");
                                        return true;
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "[TabListPro] Usage: /tablistpro <set/reload> [player] [group]");
                                    return true;
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "[TabListPro] Usage: /tablistpro <set/reload> [player] [group]");
                                return true;
                            }
                        } else {
                            p.sendMessage(plugin.colorString(plugin.getConfig().getString("no-permission")));
                            return true;
                        }
                    }
                }
            } else {
                p.sendMessage(plugin.colorString(plugin.getConfig().getString("no-permission")));
            }
        } else {
            if (sender instanceof CommandSender) {
                if (label.equalsIgnoreCase("tablistpro") || label.equalsIgnoreCase("tlp")) {
                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("reload")) {
                            commandReload();
                            System.out.println("[TabListPro] Plugin successfully reloaded!");
                            return true;
                        }
                    } else if (args.length == 3) {
                        if (args[0].equalsIgnoreCase("set")) {
                            if (Bukkit.getPlayer(args[1]) != null) {
                                for (String keys : epsb.groupKeys) {
                                    if (plugin.getConfig().getString("groups." + keys).contains(args[2])) {
                                        TLUserConfigs cm = new TLUserConfigs(plugin, Bukkit.getPlayer(args[1]));
                                        FileConfiguration f = cm.getConfig();
                                        if (f.getString("group") != null && f.getString("group").contains(args[2])) {
                                            System.out.println("[TabListPro] The player " + args[1] + " is already in the group " + args[2] + "!");
                                            return true;
                                        }
                                        f.set("group", args[2]);
                                        cm.reload();
                                        cm.saveConfig();
                                        epsb.group.put(Bukkit.getPlayer(args[1]).getUniqueId().toString(), args[2]);
                                        plugin.up.checkGroupUpdate(Bukkit.getPlayer(args[1]));
                                        System.out.println("[TabListPro] Set " + args[1] + "'s group to " + args[2] + "!");
                                        plugin.usb.updateboard();
                                        return true;
                                    }
                                }
                                System.out.println("[TabListPro] Group " + args[2] + " not found.");
                                return true;
                            } else {
                                System.out.println("[TabListPro] The player " + args[1] + " is not online!");
                                return true;
                            }
                        } else {
                            System.out.println("[TabListPro] Console usage: /tablistpro <set/reload> [player] [group]");
                            return true;
                        }
                    } else {
                        System.out.println("[TabListPro] Console usage: /tablistpro <set/reload> [player] [group]");
                        return true;
                    }
                    System.out.println("[TabListPro] Console usage: /tablistpro <set/reload> [player] [group]");
                    return true;
                }
            }
        }
        return true;
    }

}
