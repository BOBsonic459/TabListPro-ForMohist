package me.joedon;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.Hash;
import me.clip.ezrankspro.EZRanksPro;
import me.clip.placeholderapi.PlaceholderAPI;
import me.joedon.scoreboard.EPScoreboard;
import me.joedon.scoreboard.UpdatePlayers;
import me.joedon.scoreboard.UpdateScoreboard;
import me.joedon.configs.TLUserConfigs;
import me.joedon.tlpversiondetectors.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabListPro extends JavaPlugin implements Listener, CommandExecutor {

    public static boolean placeholderapi = false;

    public EPScoreboard epsb;
    public UpdateScoreboard usb;
    public UpdatePlayers up;
    private NewVersionDetector112 this7;
    public TabV tabV;

    public BukkitTask id = null;

    public interface TabV {
        void sendTabHF(Player player, String header, String footer);
    }

    public void onEnable() {
        apiinstance = this;

        usb = new UpdateScoreboard();
        up = new UpdatePlayers();
        epsb = new EPScoreboard(this);

        loadResource("config.yml");
        getConfig().options().copyDefaults(false);
        reloadConfig();

        HeaderFooter.headerAnimation = new ArrayList<>(getConfig().getStringList("header"));
        HeaderFooter.footerAnimation = new ArrayList<>(getConfig().getStringList("footer"));
        HeaderFooter hf = new HeaderFooter(this);
        epsb.permOrder = new ArrayList<>(getConfig().getStringList("sortByPerms"));
        epsb.groupKeys = getConfig().getConfigurationSection("groups").getKeys(false);

        up.updatePlaceholderAPIPlaceholders();
        usb.updateboard();

        up.rechecking();

        String implVersion;
        try {
            implVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            implVersion = "unknown";
        }
        switch (implVersion) {
            case "v1_12_R1":
                this.tabV = new NewVersionDetector112(this7);
                Bukkit.getServer().getPluginManager().registerEvents(this, this);
                break;
            default:
                this.setEnabled(false);
                System.out.println("[TabListPro] CRITICAL ERROR: TabListPro failed to startup! Cause: " + implVersion + " is not supported! Please contact Joedon on the Spigot forums and this will be resolved quickly.");
                return;
        }

        Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), this);
        getCommand("tablistpro").setExecutor(new Commands(epsb));
        getCommand("tlp").setExecutor(new Commands(epsb));

        // set group Maps
        for (Player ppl : Bukkit.getServer().getOnlinePlayers()) {
            String ppluuid = ppl.getUniqueId().toString();
            TLUserConfigs cm = new TLUserConfigs(this, ppl);
            FileConfiguration f = cm.getConfig();
            if (!ppl.hasPlayedBefore()) {
                epsb.group.put(ppluuid, getConfig().getString("default-group"));
                epsb.groupTemp.put(ppluuid, getConfig().getString("default-group"));
            } else {
                epsb.group.put(ppluuid, f.getString("group"));
                epsb.groupTemp.put(ppluuid, f.getString("groupTemp"));
            }
            cm.reload();
            cm.saveConfig();
        }

        // check for PlaceholderAPI installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            System.out.println("[TabListPro]: PlaceholderAPI this not found! PlaceholderAPI placeholders will not work!");
            placeholderapi = false;
        } else {
            System.out.println("[TabListPro]: PlaceholderAPI successfully detected. Feel free to use any PlaceholderAPI placeholders!");
            placeholderapi = true;
        }

        hf.tabHeader();
        hf.tabFooter();
        hf.tabRefresh();

        if(Bukkit.getServer().getPluginManager().isPluginEnabled("EZRanksPro")){
            EPScoreboard.ezapi = EZRanksPro.getAPI();
        }
    }

    private void loadResource(String resource) {

        File folder = this.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);

        try {
            if (!resourceFile.exists() && resourceFile.createNewFile()) {
                try (InputStream in = this.getResource(resource);
                     OutputStream out = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public void onDisable() {
        for (Player ppl : Bukkit.getServer().getOnlinePlayers()) {
            String ppluuid = ppl.getUniqueId().toString();
            TLUserConfigs cm = new TLUserConfigs(this, ppl);
            FileConfiguration f = cm.getConfig();
            f.set("group", epsb.group.get(ppluuid));
            f.set("groupTemp", epsb.groupTemp.get(ppluuid));
            cm.reload();
            cm.saveConfig();
        }
    }

    public void loadAnimations() {
        for (String keys : getConfig().getConfigurationSection("groups").getKeys(false)) {
            if (getConfig().getStringList("groups." + keys + ".display").size() > 0) {
                epsb.animations.put(keys, getConfig().getStringList("groups." + keys + ".display"));
            }
        }
    }

    private static final char COLOR_CHAR = ChatColor.COLOR_CHAR;
    private static final String startTag = "#";
    private static final String endTag = "";

    public static String colorString(String message){

            final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
            Matcher matcher = hexPattern.matcher(message);
            StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
            while (matcher.find())
            {
                String group = matcher.group(1);
                matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                        + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                        + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                        + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
                );
            }

            String hexTranslated = matcher.appendTail(buffer).toString();

            return ChatColor.translateAlternateColorCodes('&', hexTranslated);
    }

    // API

    private static TabListPro apiinstance;

    public static TabListPro getInstance(){
        return apiinstance;
    }

    public String setPlayerTabGroup(Player p, String groupID){
        if (p != null) {
            for (String keys : epsb.groupKeys) {
                if (keys.replaceAll("groups\\.", "").equalsIgnoreCase(groupID)) {
                    TLUserConfigs cm = new TLUserConfigs(JavaPlugin.getPlugin(TabListPro.class), p);
                    FileConfiguration f = cm.getConfig();
                    f.set("group", groupID);
                    cm.reload();
                    cm.saveConfig();
                    epsb.group.put(p.getUniqueId().toString(), groupID);
                    JavaPlugin.getPlugin(TabListPro.class).up.checkGroupUpdate(p);
                    JavaPlugin.getPlugin(TabListPro.class).usb.updateboard();
                    return "[TabListPro] Successfully set " + p.getName() + "'s group to " + groupID + "!";
                }
            }
            return "[TabListPro] Group " + groupID + " not found in TabListPro's config.";
        } else {
            return "[TabListPro] The player is null.";
        }
    }

    // ONLY FOR USE WITH EPTags, hardcoded much
    private static Map<String, List<String>> groupAnimationsStripped = new HashMap<>();
    public List<String> getGroupAnimationStripped(String groupID){
        if(!groupAnimationsStripped.containsKey(groupID.toLowerCase())) {
            List<String> groupAnimation = new ArrayList<>();
            for (String keys : epsb.groupKeys) {
                if (keys.replaceAll("groups\\.", "").equalsIgnoreCase(groupID.toLowerCase())) {
                    groupAnimation = getConfig().getStringList("groups." + keys + ".display");
                }
            }

            for (int i = 0; i < groupAnimation.size(); i++) {
                try {
                    groupAnimation.set(i, groupAnimation.get(i).substring(groupAnimation.get(i).lastIndexOf("%") + 2));
                }catch(Exception obe){
                    groupAnimation.set(i, "");
                }
            }

            groupAnimationsStripped.put(groupID.toLowerCase(), groupAnimation);
        }

        return groupAnimationsStripped.get(groupID.toLowerCase());
    }


    private final static String ESSENTIALS_CHAT_FORMAT = "{prestige} {DISPLAYNAME}&r{EP_CHATTAG}&8&l »&r&7 {MESSAGE}";
    private final static String EXAMPLE_MESSAGE = "Hello!";
    public static String chatStr(Player player, String tagSuffix){
        String chatFormat;

        if (!tagSuffix.equals("")) {
            chatFormat = colorString(ESSENTIALS_CHAT_FORMAT.replaceAll("\\{prestige}", PlaceholderAPI.setPlaceholders(player, "%ezprestige_prestigetag%")).
                    replaceAll("\\{DISPLAYNAME}", player.getDisplayName()).replaceAll("\\{EP_CHATTAG}", tagSuffix).replaceAll("\\{MESSAGE}", EXAMPLE_MESSAGE));

        }else{
            chatFormat = colorString(ESSENTIALS_CHAT_FORMAT.replaceAll("\\{prestige}", PlaceholderAPI.setPlaceholders(player, "%ezprestige_prestigetag%")).
                    replaceAll("\\{DISPLAYNAME}", player.getDisplayName()).replaceAll(" \\{EP_CHATTAG}", tagSuffix).replaceAll("\\{MESSAGE}", EXAMPLE_MESSAGE));

        }

        return chatFormat;
    }


    private static Map<String, Map<Player, List<String>>> groupAnimations = new HashMap<>();
    public List<String> getGroupAnimation(String groupID, Player player){
        return new ArrayList<>();
//        groupAnimations.putIfAbsent(groupID, new HashMap<>());
//        groupAnimations.get(groupID).putIfAbsent(player, Lists.newArrayList());
//        if(groupAnimations.get(groupID).get(player).isEmpty()) {
//            List<String> groupAnimation = new ArrayList<>();
//            for (String keys : epsb.groupKeys) {
//                if (keys.replaceAll("groups\\.", "").equalsIgnoreCase(groupID)) {
//                    groupAnimation = getConfig().getStringList("groups." + keys + ".display");
//                }
//            }
//
//            for (int i = 0; i < groupAnimation.size(); i++) {
//                groupAnimation.set(i, PlaceholderAPI.setPlaceholders(player, groupAnimation.get(i)));
//            }
//
//            groupAnimations.get(groupID).put(player, groupAnimation);
//        }
//
//        return groupAnimations.get(groupID).get(player);
    }

    public int getGroupAnimationSpeed(){
        return epsb.updateSpeedGlobal;
    }
}