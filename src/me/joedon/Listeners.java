package me.joedon;

import me.joedon.configs.TLUserConfigs;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Listeners implements Listener {

    private TabListPro plugin = JavaPlugin.getPlugin(TabListPro.class);

    @EventHandler
    public void onJoinUpdateGroup(PlayerJoinEvent event) {
        Player ppl = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.up.resort();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.up.checkGroupUpdate(ppl));
        }, 20);
    }

    @EventHandler
    public void onLeaveSaveGroupAndRemoveTeam(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();
        TLUserConfigs cm = new TLUserConfigs(plugin, p);
        FileConfiguration f = cm.getConfig();
        f.set("group", plugin.epsb.group.get(uuid));
        f.set("groupTemp", plugin.epsb.groupTemp.get(uuid));
        cm.reload();
        cm.saveConfig();

        String team = plugin.epsb.getTeam(event.getPlayer());
        if(event.getPlayer().getScoreboard().getTeam(team) != null) {
            if (event.getPlayer().getScoreboard().getTeam(team).getSize() <= 1) {
                event.getPlayer().getScoreboard().getTeam(team).unregister();
            } else {
                event.getPlayer().getScoreboard().getTeam(team).removeEntry(event.getPlayer().getName());
            }
        }
    }

}
