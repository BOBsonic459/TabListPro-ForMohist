package me.joedon.configs;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TLUserConfigs {

	private final UUID u;
	private FileConfiguration fc;
	private File file;
	private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());

	public TLUserConfigs(me.joedon.TabListPro plugin2, Player p) {
		this.u = p.getUniqueId();
	}
	
	private TLUserConfigs(UUID u) {
		this.u = u;
	}

	public Player getOwner() {
		if (u == null)
			try {
				throw new Exception();
			} catch (Exception e) {
			}
		return Bukkit.getPlayer(u);
	}

	public UUID getOwnerUUID() {
		if (u == null)
			try {
				throw new Exception();
			} catch (Exception e) {
			}
		return u;
	}
	
	public JavaPlugin getInstance() {
		if (plugin == null)
			try {
				throw new Exception();
			} catch (Exception e) {
			}
		return plugin;
	}

	public boolean delete() {
		return getFile().delete();
	}

	public boolean exists() {
		if (fc == null || file == null) {
			File temp = new File(getDataFolder() + "/userdata/", getOwnerUUID() + ".yml");
			if (!temp.exists()) {
				return false;
			} else {
				file = temp;
			}
		}
		return true;
	}
	
	public File getDataFolder() {
		File dir = new File(TLUserConfigs.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
		File d = new File(dir.getParentFile().getPath(), getInstance().getName());
		if (!d.exists()) {
			d.mkdirs();
		}
		return d;
	}

	public File getFile() {
		if (file == null) {
			file = new File(getDataFolder() + "/userdata/", getOwnerUUID() + ".yml");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
				}
			}
		}
		return file;
	}

	public FileConfiguration getConfig() {
		if (fc == null) {
			fc = YamlConfiguration.loadConfiguration(getFile());
		}
		return fc;
	}

	public void reload() {
		if (file == null) {
			file = new File(getDataFolder(), "data.yml");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
				}
			}
			fc = YamlConfiguration.loadConfiguration(file);
		}
	}

	public void saveConfig() {
		try {
			getConfig().save(getFile());
		} catch (IOException e) {
		}
	}
}
