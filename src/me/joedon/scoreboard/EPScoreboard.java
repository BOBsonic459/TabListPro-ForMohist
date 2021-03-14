package me.joedon.scoreboard;

import me.clip.ezrankspro.EZAPI;
import me.clip.ezrankspro.EZRanksPro;
import me.clip.placeholderapi.PlaceholderAPI;
import me.joedon.TabListPro;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class EPScoreboard {

    private TabListPro plugin;

    public EPScoreboard(TabListPro plugin){
        this.plugin = plugin;
    }

    public int updateSpeedGlobal;
    public int updateFrame = 0;
    public int biggestAnimationList = 0;

    public Map<String, String> group = new HashMap<>();
    public Map<String, String> groupTemp = new HashMap<>();

    public Set<String> groupKeys;
    public HashMap<String, List<String>> animations = new HashMap<>();

    private static HashMap<String, String> playerPrefixPlaceheld = new HashMap<>();
    private static HashMap<String, String> playerSuffixPlaceheld = new HashMap<>();

    public void updatePlayerPrefixSuffixPlaceholderString(Player p) {
        String uuid = p.getUniqueId().toString();

        try {
            if (me.joedon.TabListPro.placeholderapi) {
                playerPrefixPlaceheld.put(uuid, PlaceholderAPI.setPlaceholders(p, plugin.getConfig().getString("prefix")));
                playerSuffixPlaceheld.put(uuid, PlaceholderAPI.setPlaceholders(p, plugin.getConfig().getString("suffix")));
            } else {
                playerPrefixPlaceheld.put(uuid, plugin.getConfig().getString("prefix"));
                playerSuffixPlaceheld.put(uuid, plugin.getConfig().getString("suffix"));
            }
        } catch (Exception e) {
        }
    }

    public void setTeam(Scoreboard sb, Player p) {

        String team = getTeam(p);
        Team user = sb.getTeam(team);
        String uuid = p.getUniqueId().toString();

        if (playerPrefixPlaceheld.get(uuid) == null || playerSuffixPlaceheld.get(uuid) == null) {
            updatePlayerPrefixSuffixPlaceholderString(p);
        }

        if (user == null) {
            sb.registerNewTeam(team);
            user = sb.getTeam(team);
        }

        // remove entry before adding again, in case of the team not being new
        user.removeEntry(p.getName());
        if (!user.hasEntry(p.getName())) {
//            for(Team t : p.getScoreboard().getTeams()){
//                t.removeEntry(p.getName());
//            }
            user.addEntry(p.getName());
        }

        if (plugin.getConfig().getBoolean("use-displayname")) {
            //USING DISPLAY NAME
            p.setPlayerListName(plugin.colorString(playerPrefixPlaceheld.get(uuid) + p.getDisplayName() + playerSuffixPlaceheld.get(uuid)));
        }
    }


    //custom prestige and rank sorting
    public final boolean EP_VERSION = false;
    private final boolean HIGHEST_PRESTIGES_FIRST = true;
    //IF THIS IS 10+, add '0's in front when sorting (normal sorts already have)
    private final int MAX_PRESTIGES = 88;
    public static EZAPI ezapi = null;

    private final List<String> RANKS = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

    public ArrayList<String> permOrder = new ArrayList<>();

    public String getTeam(Player p) {
        if(!EP_VERSION) {
            for (int i = 0; i < permOrder.size(); i++) {
                if (p.hasPermission(permOrder.get(i))) {
                    //fixes sorting when next x10 up (9 -> 10, 99 -> 100, etc.)
                    if(i >= 999) {
                        //5555TLPS
                        return i + "TLPS";
                    }else if(i >= 99){
                        //0555TLPS
                        return "0" + i + "TLPS";
                    }else if(i >= 9){
                        //0055TLPS
                        return "00" + i + "TLPS";
                    }else{
                        //0005TLPS
                        return "000" + i + "TLPS";
                    }
                }
            }
            //only can be numbers in sorting, so setting 'A' + (...) to be last
            return "zzzzzzzzzzzzzzzz";


            //-=- PRIVATE EMERALDPRISONMC.COM VERSION -=-
        }else{
            if(ezapi == null){
                return "";
            }

            //infinit_dark (Owner)
            if(p.getUniqueId().toString().equals("c8cf896c-bfc9-406d-b2a6-8999b86b0a9d")){
                return "00000000000TLPS";
            }

            if(p.isOp()){
                return "0000000000TLPS";
            }

            // 'A'-Rank
            String rank = ezapi.getCurrentRank(p).substring(0, 1);
            // if Z, -> A.
            String invertedRank = RANKS.get(RANKS.size() - RANKS.indexOf(rank) - 1).toUpperCase();

            int prestige = Integer.parseInt(PlaceholderAPI.setPlaceholders(p, "%ezprestige_prestige%"));

//            System.out.println("max " + MAX_PRESTIGES);
//            System.out.println("prestige " + prestige);
//            System.out.println(MAX_PRESTIGES - prestige);
//            System.out.println("invert: " + invertedRank);

            if(p.hasPermission("epc.helper")){
                if (HIGHEST_PRESTIGES_FIRST) {
                    return MAX_PRESTIGES - prestige >= 10 ?
                            "00" + (MAX_PRESTIGES - prestige) + invertedRank :
                            "000" + (MAX_PRESTIGES - prestige) + invertedRank;
                }else{
                    return prestige >= 10 ?
                            "00" + (prestige) + invertedRank :
                            "000" + (prestige) + invertedRank;
                }
            }

            if (HIGHEST_PRESTIGES_FIRST) {

                //  -> MAX_PRESTIGES - prestige >= 10 {
                // P40, Z -> 48A
                // P7, Z -> 81A
                // P7, A -> 81Z
                //  ->  } else {
                // P87, Z -> 01A
                // P82, Z -> 06A

                return MAX_PRESTIGES - prestige >= 10 ?
                        (MAX_PRESTIGES - prestige) + invertedRank :
                        "0" + (MAX_PRESTIGES - prestige) + invertedRank;

            } else {

                // P2, Z -> 02A
                // P40, Z -> 40A
                // P87, A -> 87Z

                return prestige >= 10 ?
                        (prestige) + invertedRank :
                        "0" + (prestige) + invertedRank;

            }
        }
    }
}