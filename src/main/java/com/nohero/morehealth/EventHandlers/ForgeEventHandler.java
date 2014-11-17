package com.nohero.morehealth.EventHandlers;

import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
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

	/**
	 * My function here will be called when any living entity has his living update event, but will only continue
	 * if the entity is a player.
	 * The main logic of the RPG side of the code resides in this function.
	 * It calculates what the player's health should be on every tick of the game (for the RPG system)
	 * @param event
	 */
	@SubscribeEvent		//Minecraft Forge Subscribe Event
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event)
	{
		if( event.entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			PlayerStats stats = PlayerStats.getPlayerStats(player.getCommandSenderName());
			//the player stats should be (one) for both the client and server, which allows us to communicate from server to client

			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if(side == Side.CLIENT && stats.needClientSideHealthUpdate){
				//update the client side on a dimension change
				PlayerHandlerHelper.savePlayerData(player, false);
				PlayerHandlerHelper.updatePlayerData(player);
				player.setHealth(player.getMaxHealth());
				stats.needClientSideHealthUpdate = false;
				//System.out.println("HO");
			}
			/**main logic for rpg heart gain.**/
			if(mod_moreHealthEnhanced.RpgMode==true){
				calculateHeartChange(player, stats);
			}

		}
	}

	private void calculateHeartChange(EntityPlayer player, PlayerStats stats) {

		if(levelIncreased(player, stats))
		{
			/*
			While the player is still able to earn hearts (based on the config file's level ramp)-- aka stats.count hasn't reached the end of the ramp array
			AND the player's exp level meets the level required for the (count)th's heart
			Continue to update the players health.
			EG Count = 3
			LevelRampInt = [0,10,20,30,40,50]
			LevelRampInt[3] = 30.
			So if player is level 40, then he gains one heart. The while loop iterates again, incrementing count.
			LevelRampInt[4] = 40. He still gets one heart. The while loop iterates once again, but now at 50 the
			condition is not satisfied
			 */
			while(stats.count<mod_moreHealthEnhanced.LevelRampInt.length && player.experienceLevel>=mod_moreHealthEnhanced.LevelRampInt[stats.count])
			{ //count<...levelrampInt.length keeps the rpg side in track the second part keeps this running while explevel>count and the final part helps fix bug if the player used a heart container. That causes the count to be less than levelrampint.length even when they hit max health.
				//stops updating health on level up if rpg mode is off.

					player.addChatComponentMessage(new ChatComponentText("Your Life has increased by one and is also now fully replenished!"));
					double updatedModifier=0;
					try{
						updatedModifier=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(PlayerHandler.moreHealthID).getAmount()+2.0;
					}
					catch (Exception e) {
					}
					mod_moreHealthEnhanced.healthMod=new AttributeModifier(PlayerHandler.moreHealthID, "More Health Heart Modifier", updatedModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
					IAttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
					attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
					attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);

					player.setHealth(player.getMaxHealth());
					stats.count++;				//sets count to the next xp level to gain a heart. Thus, this system deals with death/xp loss easily.
					//with xp loss, experience became not so good a tracker. However, count will always remain the same.
			}
			saveHeartChange(player, stats);
		}

	}

	/**
	 * Saves the changes made to the NBTTagCompound (persistent storage) from the changes to the player stats (game session storage)
	 * @param player
	 * @param stats
	 */
	private void saveHeartChange(EntityPlayer player, PlayerStats stats) {
		NBTTagCompound tags = player.getEntityData(); //saves changes made to nbt
		tags.getCompoundTag("MoreHealth 1").setInteger("count", stats.count);
		try{
			stats.healthmod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(PlayerHandler.moreHealthID).getAmount();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		tags.getCompoundTag("MoreHealth 1").setDouble("healthModifier", stats.healthmod);
		tags.getCompoundTag("MoreHealth 1").setInteger("previousLevel", stats.previousLevel);
		PlayerHandler.playerStats.put(player.getCommandSenderName(), stats);
	}

	/**
	 * Checks if the player has has a level increase since the last time his level was recorded into the stats
	 * data structure
	 * @param player
	 * @param stats
	 * @return
	 */
	private boolean levelIncreased(EntityPlayer player, PlayerStats stats) {
		boolean levelIncreased=false;
		if(stats.previousLevel!=player.experienceLevel)
		{
			stats.previousLevel=player.experienceLevel;
			levelIncreased=true;
		}
		return levelIncreased;
	}


}
