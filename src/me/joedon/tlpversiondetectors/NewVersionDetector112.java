package me.joedon.tlpversiondetectors;

import java.lang.reflect.Field;

import me.joedon.TabListPro;
import org.bukkit.entity.Player;

import me.joedon.TabListPro.TabV;

public class NewVersionDetector112 implements TabV{

	  public TabListPro plugin7;
		
	     public NewVersionDetector112(TabListPro plugin) {
	         this.plugin7 = plugin;
	     }
	
	 	public NewVersionDetector112(NewVersionDetector112 plugin12) {
		}

		public void sendTabHF(Player player, String header, String footer) {
			org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer craftplayer = (org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) player;
			/*net.minecraft.server.v1_12_R1.PlayerConnection connection = craftplayer.getHandle().playerConnection;*/
			net.minecraft.server.v1_12_R1.IChatBaseComponent headerJSON = net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}");
			net.minecraft.server.v1_12_R1.IChatBaseComponent footerJSON = net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}");
			net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter packet = new net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter();

				try {
					Field headerField = packet.getClass().getDeclaredField("a");
					headerField.setAccessible(true);
					headerField.set(packet, headerJSON);
					headerField.setAccessible(!headerField.isAccessible());

					Field footerField = packet.getClass().getDeclaredField("b");
					footerField.setAccessible(true);
					footerField.set(packet, footerJSON);
					footerField.setAccessible(!footerField.isAccessible());
				} catch (Exception e) {
					//e.printStackTrace();
				}

				//connection.sendPacket(packet);
			}
		}
