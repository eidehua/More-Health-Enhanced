package com.nohero.morehealth.GUI;

import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

/**
 * Created by Ed Xue on 12/4/2014.
 */
public class MoreHealthGui extends GuiScreen {
	public static KeyBinding keyBinding;


	public static int id = 1020;
	public MoreHealthGui(){

	}
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		EntityPlayer player = mc.thePlayer;
		PlayerStats stats = PlayerStats.getPlayerStats(player.getCommandSenderName());
		drawCenteredString(mc.fontRenderer, "More Health Stats", mc.displayWidth / 4, 2, 0xe0e0e0);
		drawCenteredString(mc.fontRenderer, "Current Hearts: "+ player.getHealth()/2.0, mc.displayWidth / 4, 12, 0xe0e0e0);
		drawCenteredString(mc.fontRenderer, "Max Hearts: "+ player.getMaxHealth()/2.0, mc.displayWidth / 4, 22, 0xe0e0e0);
		drawCenteredString(mc.fontRenderer, "Hearts from RPG: "+ stats.count, mc.displayWidth / 4, 32, 0xe0e0e0); //stats.count counts how many entries of the level ramp is used (aka how many hearts gained through rpg mode0
		int extraHearts = 0;
		for(int i =0; i<stats.oldArmorSet.length; i++){
			extraHearts+= EnchantmentHelper.getEnchantmentLevel(mod_moreHealthEnhanced.heartArmorEnchantID, stats.oldArmorSet[i]);
		}
		drawCenteredString(mc.fontRenderer, "Hearts from Enchantment: "+ (extraHearts), mc.displayWidth / 4, 42, 0xe0e0e0);
		drawCenteredString(mc.fontRenderer, "Hearts from Heart Containers: "+ (stats.healthmod/2.0-stats.count-extraHearts), mc.displayWidth / 4, 52, 0xe0e0e0);
		//stats.healthmod/2.0 = how many hearts gained in total from my mod. Subtract out stats.count to get hearts from containers only.





	}
}
