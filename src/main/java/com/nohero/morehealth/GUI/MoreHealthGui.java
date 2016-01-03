package com.nohero.morehealth.GUI;

import com.nohero.morehealth.EventHandlers.PlayerHandlerHelper;
import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
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
		drawCenteredString(mc.fontRenderer, "Current Hearts: "+ (int) player.getHealth()/2.0, mc.displayWidth / 4, 12, 0xe0e0e0);
		drawCenteredString(mc.fontRenderer, "Max Hearts: "+ (int) player.getMaxHealth()/2.0, mc.displayWidth / 4, 22, 0xe0e0e0);
		drawCenteredString(mc.fontRenderer, "Hearts from RPG: "+ stats.count, mc.displayWidth / 4, 32, 0xe0e0e0); //stats.count counts how many entries of the level ramp is used (aka how many hearts gained through rpg mode0
		int extraHearts = 0;
		for(int i =0; i<stats.oldArmorSet.length; i++){
			extraHearts+= EnchantmentHelper.getEnchantmentLevel(mod_moreHealthEnhanced.heartArmorEnchantID, stats.oldArmorSet[i]);
		}
		drawCenteredString(mc.fontRenderer, "Hearts from Enchantment: "+ (extraHearts), mc.displayWidth / 4, 42, 0xe0e0e0);

		double health = PlayerHandlerHelper.calculateTotalMoreHealthContribution(player, stats);
		double myModAmount= Math.abs(20+mod_moreHealthEnhanced.healthMod.getAmount());
		double heartsFromContainers = stats.heartContainers;
		if(myModAmount - health != 0){
			//some missing health not accounted for (for old accounts before the stats.heartContainer variable was introduced)
			heartsFromContainers += myModAmount - health;
		}
		drawCenteredString(mc.fontRenderer, "Hearts from Heart Containers: "+ (heartsFromContainers), mc.displayWidth / 4, 52, 0xe0e0e0);

		IAttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
		attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
		drawCenteredString(mc.fontRenderer, "Health from other sources: " + (int) (player.getMaxHealth()-20) + "", mc.displayWidth / 4, 62, 0xe0e0e0);
		attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);



	}
}
