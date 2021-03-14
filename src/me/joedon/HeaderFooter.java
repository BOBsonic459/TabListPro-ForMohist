package me.joedon;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HeaderFooter {

    private me.joedon.TabListPro plugin;
    static int fastestUpdateRateReq = 1;

    public static ArrayList<String> headerAnimation;
    public static ArrayList<String> footerAnimation;
    public Map<String, String> headerAndFooterTabText = new HashMap<>();

    public HeaderFooter(me.joedon.TabListPro plugin){
        this.plugin = plugin;
    }

    public void tabHeader() {

        Bukkit.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int headerIndex = 0;

            public void run() {
                if (plugin.getConfig().getBoolean("header-enabled")) {
                    if (headerIndex < headerAnimation.size()) {
                        String headerText = headerAnimation.get(headerIndex);
                        if (me.joedon.TabListPro.placeholderapi) {
                            headerText = PlaceholderAPI.setPlaceholders(null, headerText);
                        }
                        headerAndFooterTabText.put("header", headerText);
                    } else {
                        headerIndex = 0;
                        String headerText = headerAnimation.get(headerIndex);
                        if (me.joedon.TabListPro.placeholderapi) {
                            headerText = PlaceholderAPI.setPlaceholders(null, headerText);
                        }
                        headerAndFooterTabText.put("header", headerText);
                    }
                    headerIndex++;
                }
            }
        }, 0, plugin.getConfig().getInt("header-interval"));
    }

    public void tabFooter() {
        Bukkit.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int footerIndex = 0;

            public void run() {
                if (plugin.getConfig().getBoolean("footer-enabled")) {
                    if (footerIndex < footerAnimation.size()) {
                        String footerText = footerAnimation.get(footerIndex);
                        if (me.joedon.TabListPro.placeholderapi) {
                            footerText = PlaceholderAPI.setPlaceholders(null, footerText);
                        }
                        headerAndFooterTabText.put("footer", footerText);
                    } else {
                        footerIndex = 0;
                        String footerText = footerAnimation.get(footerIndex);
                        if (me.joedon.TabListPro.placeholderapi) {
                            footerText = PlaceholderAPI.setPlaceholders(null, footerText);
                        }
                        headerAndFooterTabText.put("footer", footerText);
                    }
                    footerIndex++;
                }
            }
        }, 0, plugin.getConfig().getInt("footer-interval"));
    }

    public void tabRefresh() {

        if (plugin.getConfig().getInt("header-interval") >= plugin.getConfig().getInt("footer-interval")) {
            fastestUpdateRateReq = plugin.getConfig().getInt("footer-interval") - 1;
        } else {
            fastestUpdateRateReq = plugin.getConfig().getInt("header-interval") - 1;
        }
        if(fastestUpdateRateReq == 0){
            fastestUpdateRateReq = 1;
        }

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (plugin.getConfig().getBoolean("header-enabled") || plugin.getConfig().getBoolean("footer-enabled")) {
                for (Player ppl : Bukkit.getServer().getOnlinePlayers()) {
                    if (ppl != null) {
                        if(plugin.getConfig().getBoolean("header-enabled") && plugin.getConfig().getBoolean("footer-enabled")){
                            plugin.tabV.sendTabHF(ppl, plugin.colorString(headerAndFooterTabText.get("header")), plugin.colorString(headerAndFooterTabText.get("footer")));
                        }else if (!plugin.getConfig().getBoolean("header-enabled")) {
                            if (headerAndFooterTabText.get("footer") != null) {
                                plugin.tabV.sendTabHF(ppl, "", plugin.colorString(headerAndFooterTabText.get("footer")));
                            }
                        } else if (!plugin.getConfig().getBoolean("footer-enabled")) {
                            if (headerAndFooterTabText.get("header") != null) {
                                plugin.tabV.sendTabHF(ppl, plugin.colorString(headerAndFooterTabText.get("header")), "");
                            }
                        }else{
                            break;
                        }
                    }
                }
            }
        }, 0L, fastestUpdateRateReq);
    }

}
