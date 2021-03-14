package me.joedon.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.joedon.TabListPro;
import me.joedon.configs.TLUserConfigs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class UpdateScoreboard {

    private TabListPro plugin = JavaPlugin.getPlugin(TabListPro.class);

    public void updateboard() {
        plugin.loadAnimations();

        try {
            plugin.id.cancel();
        } catch (Exception ignored) { }

        plugin.epsb.updateSpeedGlobal = plugin.getConfig().getInt("name-animation");
        plugin.epsb.biggestAnimationList = 0;

        plugin.epsb.groupKeys = plugin.getConfig().getConfigurationSection("groups").getKeys(false);

        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.up.checkGroupUpdate(p);
        }

        //NOT USING DISPLAY NAME
        plugin.id = Bukkit.getServer().getScheduler().runTaskTimer(plugin, () -> {

            if (plugin.getConfig().getBoolean("use-displayname")) {
                plugin.id.cancel();
                return;
            }

            //System.out.println(updateSpeedGlobal + " speed");
            for (Player p : Bukkit.getOnlinePlayers()) {
                String uuid = p.getUniqueId().toString();

                if (plugin.epsb.group.get(uuid) == null) {
                    TLUserConfigs cm = new TLUserConfigs(plugin, p);
                    FileConfiguration f = cm.getConfig();
                    if (f.getString("group") == null) {
                        plugin.epsb.group.put(uuid, plugin.getConfig().getString("default-group"));
                        plugin.epsb.groupTemp.put(uuid, plugin.getConfig().getString("default-group"));
                    } else {
                        f.set("group", plugin.epsb.group.get(uuid));
                        f.set("groupTemp", plugin.epsb.groupTemp.get(uuid));
                        cm.reload();
                        cm.saveConfig();
                    }
                }

                plugin.epsb.groupTemp.putIfAbsent(uuid, plugin.getConfig().getString("default-group"));

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    List<String> list = plugin.epsb.animations.get(plugin.epsb.groupTemp.get(uuid));

                    if(list == null){
                        plugin.epsb.groupTemp.put(uuid, plugin.getConfig().getString("default-group"));
                    }

                    String playerTabText;
                    if(list == null){
                        return;
                    }
                    if (plugin.epsb.updateFrame < list.size()) {
                        if (TabListPro.placeholderapi) {
                            playerTabText = PlaceholderAPI.setPlaceholders(p, list.get(plugin.epsb.updateFrame));
                            p.setPlayerListName(plugin.colorString(playerTabText.replaceAll("%player%", p.getName())/*.replaceAll("%placeholder%", playerPlaceheld.get(uuid))*/.replaceAll("%player_displayname%", p.getDisplayName())));
                        } else {
                            playerTabText = list.get(plugin.epsb.updateFrame);
                            p.setPlayerListName(plugin.colorString(playerTabText.replaceAll("%player%", p.getName())/*.replaceAll("%placeholder%", playerPlaceheld.get(uuid))*/.replaceAll("%player_displayname%", p.getDisplayName())));
                        }
                    } else {
                        if (TabListPro.placeholderapi) {
                            playerTabText = PlaceholderAPI.setPlaceholders(p, list.get(list.size() - 1));
                            p.setPlayerListName(plugin.colorString(playerTabText/*.replaceAll("%placeholder%", playerPlaceheld.get(uuid))*/.replaceAll("%player%", p.getName()).replaceAll("%player_displayname%", p.getDisplayName())));
                        } else {
                            playerTabText = list.get(list.size() - 1);
                            p.setPlayerListName(plugin.colorString(playerTabText/*.replaceAll("%placeholder%", playerPlaceheld.get(uuid))*/.replaceAll("%player%", p.getName()).replaceAll("%player_displayname%", p.getDisplayName())));
                        }
                    }

                    //get biggest animation sequence size
                    int size = plugin.getConfig().getStringList("groups." + plugin.epsb.groupTemp.get(uuid) + ".display").size();
                    if (size > 0) {
                        if (size > plugin.epsb.biggestAnimationList) {
                            plugin.epsb.biggestAnimationList = size;
                        }
                    }
                });
            }

            plugin.epsb.updateFrame++;

            if (plugin.epsb.updateFrame >= plugin.epsb.biggestAnimationList) {
                plugin.epsb.updateFrame = 0;
            }
        }, 0, plugin.epsb.updateSpeedGlobal);
    }

}
