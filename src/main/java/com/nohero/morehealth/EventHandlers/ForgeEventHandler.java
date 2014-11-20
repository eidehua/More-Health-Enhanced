package com.nohero.morehealth.EventHandlers;

import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
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
				player.setHealth(player.getHealth());
				stats.needClientSideHealthUpdate = false;
				if(stats.justLoggedIn && stats.loggedOutHealth !=0){
					//sets up the client side for a player that just logged in (and after loggedOutHealth is grabbed from NBT)
					//if player doesn't have loggedOutHealth in nbt, it's fine since we don't have to deal with that player's health
					player.setHealth(stats.loggedOutHealth);
					stats.justLoggedIn = false;

				}
				//System.out.println("HO");
			}
			/**main logic for rpg heart gain.**/
			if(mod_moreHealthEnhanced.RpgMode==true){
				calculateHeartChange(player, stats);
			}

			//check player's armor item slots (1-4: Armor)
			//should work when a player enters (all "oldarmor" = null, then his heart bonus gets set properly)
			if(side == Side.SERVER && mod_moreHealthEnhanced.Enchantments) {
				//logic for armor enchantments
				calculateEnchantmentChanges(player, stats);
			}
			//save all changes to NBT and the player's stats data structure
			saveHeartChange(player, stats);

		}
	}

	/**
	 * Looks through a player's current inventory armor comparing it with the previous tick/event's armor.
	 * Updates the player's health modifiers.
	 * Need a client side update, so set stats.needClientSideHealthUpdate to be true
	 * @param player
	 * @param stats
	 */
	private void calculateEnchantmentChanges(EntityPlayer player, PlayerStats stats) {
		if(stats.needClientSideHealthUpdate){ //prevents race conditions of server thread and client thread.
			//wait for client side to update before calculating new armor health changes
			//System.out.println("client update");
			//return;
		}
		if(stats.loggedOutHealth == 0){
			return; //wait for player to be set up
		}
		int armorHealth = 0;
		for(int i = 1; i <=4; i++){
			ItemStack currentArmor = player.getEquipmentInSlot(i);
			ItemStack oldArmor = stats.oldArmorSet[i-1]; //equipmentinslot 1-4 corrspond with oldArmorset 0-3

			double currentMaxHealthMod = 0;
			try{
				currentMaxHealthMod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(PlayerHandler.moreHealthID).getAmount();
			}
			catch (Exception e) {
				//don't do enchantment changes until player is loaded in
				return;
			}
			if(oldArmor == currentArmor){
				//do nothing, armor hasn't changed
			}
			else if(currentArmor == null && oldArmor != null){
				//an armor was unequipped!
				int extraHearts = EnchantmentHelper.getEnchantmentLevel(mod_moreHealthEnhanced.heartArmorEnchantID, oldArmor);
				//1 heart = 2 health.
				if(extraHearts>0) {
					int extraHealth = extraHearts * 2;
					//add (-)extraHealth (aka subtract)
					PlayerHandler.addHealthModifier(player, currentMaxHealthMod-extraHealth);
					player.addChatComponentMessage(new ChatComponentText("Removing the armor causes the extra " + extraHearts + " enchanted hearts to fade away."));
					//System.out.println(currentMaxHealthMod+","+extraHealth);
					//player.addChatComponentMessage(new ChatComponentText("You now have "+ player.getMaxHealth()));
					stats.needClientSideHealthUpdate = true;
				}

			}
			else if(oldArmor == null  && currentArmor != null){
				//an armor was equipped (when there was nothing before)
				int extraHearts = EnchantmentHelper.getEnchantmentLevel(mod_moreHealthEnhanced.heartArmorEnchantID, currentArmor);
				if(extraHearts>0) {
					int extraHealth = extraHearts *2;
					PlayerHandler.addHealthModifier(player, currentMaxHealthMod+extraHealth); //changes the health modifier to this new one
					if(!stats.justLoggedIn) {
						player.addChatComponentMessage(new ChatComponentText("Equipping the armor binds an extra " + extraHearts + " enchanted hearts to your soul."));
					}
					//System.out.println(currentMaxHealthMod+","+extraHealth);
					//player.addChatComponentMessage(new ChatComponentText("You now have "+ player.getMaxHealth()));
					stats.needClientSideHealthUpdate = true;
					armorHealth += extraHealth;
				}
			}
			else{
				//both are non null, and they are not equal to each other.
				int oldHealth = 2* EnchantmentHelper.getEnchantmentLevel(mod_moreHealthEnhanced.heartArmorEnchantID, oldArmor);
				int newHealth = 2* EnchantmentHelper.getEnchantmentLevel(mod_moreHealthEnhanced.heartArmorEnchantID, currentArmor);
				int healthChange = newHealth - oldHealth;
				PlayerHandler.addHealthModifier(player,currentMaxHealthMod+healthChange);
				//Adds the change in health (can be positive and negative)
				if(healthChange>0){
					//player overall gained hearts
					player.addChatComponentMessage(new ChatComponentText("Equipping the stronger new armor binds an extra " + healthChange + " enchanted hearts to your soul."));
					stats.needClientSideHealthUpdate = true;

				}
				if(healthChange<0){
					//player overall lost hearts
					player.addChatComponentMessage(new ChatComponentText("Equipping the weaker new armor releases an extra " + healthChange + " enchanted hearts."));
					stats.needClientSideHealthUpdate = true;
				}
			}
			//update old Armor piece to be the current one
			stats.oldArmorSet[i-1] = currentArmor;
		}
		//after checking the armor pieces, if the player net lost health, his max health is updated but his current health is not
		if(player.getHealth()>player.getMaxHealth()){
			player.setHealth(player.getMaxHealth());
		}
		//If player just logged in
		if(stats.justLoggedIn){
			//stats.justLoggedIn = false;
			//if a player just logged in, set health equal to his loggedOutHealth
			//System.out.println(armorHealth);
			//System.out.println(stats.loggedOutHealth);
			player.setHealth(stats.loggedOutHealth);
			stats.needClientSideHealthUpdate = true;
		}
	}

	/**
	 * CommandEnchant class
	 * processCommand method
	 * This is where an enchant is applied-- I need to grab the item and apply hearts based on the enchantment's enchant level
	 * itemstack.addEnchantment(enchantment, j); j = enchantment level
	 * I need to get the enchantment when an enchantment is applied, check if it's an armor health enchantment that is applied
	 * Then I have to update health based on enchantment level
	 * I can get the level from getEnchantmentLevel(enchantmentID, itemstack) (since an item(stack) can have more than one enchantment
	 * I need to update a players health when he EQUIPS or REMOVES a heart enchanted armor-- aka not on CommandEvent
	 */
	private void calculateHeartChange(EntityPlayer player, PlayerStats stats) {

		if(mod_moreHealthEnhanced.MaxHearts== -1 || mod_moreHealthEnhanced.MaxHearts == 0){
			//don't do the below check if MaxHearts cap is turned off (-1 or 0)
		}
		else if(player.getMaxHealth()+2>mod_moreHealthEnhanced.MaxHearts*2) //check- in this case, don't continue adding to max health
		{
			//CASE: player gets more health through heart container system, so RPG system doesn't exceed the cap
			return;

		}

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
			//saveHeartChange(player, stats);
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
