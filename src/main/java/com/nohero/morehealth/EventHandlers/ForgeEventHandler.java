package com.nohero.morehealth.EventHandlers;

import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Handles the events that require only Forge Events
 */
public class ForgeEventHandler {

	/**
	 * When an entity dies, my method here gets called with the event that occurred.
	 * If the entities happen to be the Dragon or Wither, they will drop heart containers (if users configured my mod to allow it)
	 * @param event
	 */
	@SubscribeEvent
	public void onEntityLivingDeath(LivingDeathEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			//bosses drop 1 heart container each
			//must be manually tested
			if(mod_moreHealthEnhanced.HeartContainersAndPieces){
				if(event.entity instanceof EntityDragon || event.entity instanceof EntityWither){
					event.entity.entityDropItem(new ItemStack(mod_moreHealthEnhanced.heartContainer),0.0F);
				}
			}
		}
	}

}
