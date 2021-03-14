package me.joedon.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.joedon.TabListPro;
import org.bukkit.Bukkit;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class UpdatePlayers {

    private TabListPro plugin = JavaPlugin.getPlugin(TabListPro.class);

    private Scoreboard statsScoreboard(Player p) {
        ////     Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Scoreboard scoreboard;
        if(Bukkit.getScoreboardManager().getMainScoreboard() != null) {
            //scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            scoreboard = p.getScoreboard();
        }else{
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective;
        if (scoreboard.getObjective(p.getName()) == null) {
            objective = scoreboard.registerNewObjective(p.getName(), "dummy");
            objective.setDisplayName("TLP Tab Sorting");
        }

//        if(scoreboard.getObjective(DisplaySlot.SIDEBAR) != null) {
//            scoreboard.getObjective(DisplaySlot.SIDEBAR).setDisplaySlot(DisplaySlot.SIDEBAR);
//        }

        return scoreboard;
    }

    public void rechecking() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            resort();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    checkGroupUpdate(p);
                }
            });
        }, 20, plugin.getConfig().getInt("update-sorting-and-groups"));
    }

    public void resort() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = statsScoreboard(p);
            for (Player ppl : Bukkit.getOnlinePlayers()) {
                plugin.epsb.setTeam(sb, ppl);
            }
            p.setScoreboard(sb);
        }
    }

    public void checkGroupUpdate(Player ppl) {
        try {
            String uuid = ppl.getUniqueId().toString();
            for (String keys : plugin.epsb.groupKeys) {
                plugin.epsb.group.putIfAbsent(uuid, plugin.getConfig().getString("default-group"));

                if (plugin.epsb.EP_VERSION) {
                    String tagID = PlaceholderAPI.setPlaceholders(ppl, "%eptab_tagId%");

                    //Joedon (Owner)
                    if (ppl.getUniqueId().toString().equals("c8cf896c-bfc9-406d-b2a6-8999b86b0a9d")) {
                        plugin.epsb.groupTemp.put(uuid, "owner");
                        plugin.epsb.updatePlayerPrefixSuffixPlaceholderString(ppl);
                        break;
                    }

                    if (ppl.isOp()) {
                        plugin.epsb.groupTemp.put(uuid, "op");
                        plugin.epsb.updatePlayerPrefixSuffixPlaceholderString(ppl);
                        break;
                    }

                    // if their EPTagID matches the TabListPro group, set it to that
                    if (tagID.equals("default")) {
                        plugin.epsb.groupTemp.put(uuid, "member");
                        plugin.epsb.updatePlayerPrefixSuffixPlaceholderString(ppl);
                        break;
                    } else if (tagID.equals(keys)) {
                        plugin.epsb.groupTemp.put(uuid, keys);
                        plugin.epsb.updatePlayerPrefixSuffixPlaceholderString(ppl);
                        break;
                    }

                } else {

                    //if perms to current Group and Group doesnt =\
                    if (ppl.hasPermission(plugin.getConfig().getString("groups." + keys + ".orHasPermission")) && !plugin.epsb.group.get(uuid).equalsIgnoreCase(keys)) {
                        plugin.epsb.groupTemp.put(uuid, keys);
                        plugin.epsb.updatePlayerPrefixSuffixPlaceholderString(ppl);
                        break;
                    }

                    //if player doesn't have permission to current Group and their temp Group == current and their temp Group != their main Group, reset to main Group
                    if (!ppl.hasPermission(plugin.getConfig().getString("groups." + keys + ".orHasPermission")) && plugin.epsb.groupTemp.get(uuid).equalsIgnoreCase(keys) && !plugin.epsb.groupTemp.get(uuid).equalsIgnoreCase(plugin.epsb.group.get(uuid))) {
                        plugin.epsb.groupTemp.put(uuid, plugin.epsb.group.get(uuid));
                        plugin.epsb.updatePlayerPrefixSuffixPlaceholderString(ppl);
                        break;
                    }

                    //if player = current Group, set temp to =
                    if (plugin.epsb.group.get(uuid).equalsIgnoreCase(keys)) {
                        plugin.epsb.groupTemp.put(uuid, keys);
                        plugin.epsb.updatePlayerPrefixSuffixPlaceholderString(ppl);
                        break;
                    }
                }
            }
        }catch(Exception ignored){ /* on   ->   if (!ppl.hasPermission(plugin.getConfig().getString("groups." + keys + ".orHasPermission")) && plugin.epsb.groupTemp.get(uuid).equalsIgnoreCase(keys) && !plugin.epsb.groupTemp.get(uuid).equalsIgnoreCase(plugin.epsb.group.get(uuid))) {*/ }
    }

    public void updatePlaceholderAPIPlaceholders() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                new EPScoreboard(plugin).updatePlayerPrefixSuffixPlaceholderString(p);
            }
        });
    }

}