package com.nohero.morehealth.EventHandlers;

import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;

/**
 * Created by Ed Xue on 11/6/2014.
 */
public class PlayerHandlerHelper {
	//is it good idea to move these private methods to a helper class? since they are void?

	public static void updateHealth(EntityPlayer player, PlayerStats stats, NBTTagCompound tags) {
		NBTTagCompound moreHealthTag = (NBTTagCompound) tags.getTag("MoreHealth 1");

		if(stats.start!= mod_moreHealthEnhanced.StartingHearts) //deals with the case of an existing user CHANGING the config file.
		{
			stats.start=mod_moreHealthEnhanced.StartingHearts;
			player.addChatComponentMessage(new ChatComponentText("Starting Hearts sucessfully changed!"));
			moreHealthTag.setInteger("start", stats.start);
		}
		if(!Arrays.equals(stats.LevelArray, mod_moreHealthEnhanced.LevelRampInt))
		//when you start up the game, levelarray gets reset (its hard to save this variable for now) but nothing should really change.
		{
			stats.LevelArray=mod_moreHealthEnhanced.LevelRampInt;
			player.addChatComponentMessage(new ChatComponentText("Level Ramp sucessfully changed!"));
			moreHealthTag.setIntArray("LevelArray", stats.LevelArray);
		}
		stats.hpmax=stats.start*2;
		if(mod_moreHealthEnhanced.MaxHearts==-1 || mod_moreHealthEnhanced.MaxHearts==0)
		{
			//no cap means don't deal with maxhp
		}
		else
		{
			if(stats.hpmax>mod_moreHealthEnhanced.MaxHearts*2) //check
				stats.hpmax=mod_moreHealthEnhanced.MaxHearts*2;
		}
		moreHealthTag.setInteger("hpmax", stats.hpmax);

		player.setHealth(player.getMaxHealth());

		if(mod_moreHealthEnhanced.RpgMode==false)
		{
			player.addChatComponentMessage(new ChatComponentText("Heart Container mode enable!"));
			return;
		}

		//allows start to update and hp max to update, but stops the array.
		stats.count=0;
		int addHealth=0;
		for(int i=0;i<mod_moreHealthEnhanced.LevelRampInt.length;i++) //only for first run though, aka hearts are retained after death.
		{
			if(player.experienceLevel>=mod_moreHealthEnhanced.LevelRampInt[i])// && mod_moreHealthEnhanced.hpmax==start*2) redundant code, hpmax set to start*2
			{ //levemrampint[i] is the actual level, while i itself is the array position
				stats.count++; //if explevel is 1, and levelrampint[0] is 1, then the if statement occurs. The count should increase in this process.
				//thus, when the player levels to 2 and levelrampint[1] is 2, increaselevel works and the update is applies.
				addHealth+=2;
			}
			else
			break; //breaks out
		}
		stats.hpmax=stats.start*2;
		if(mod_moreHealthEnhanced.MaxHearts==-1 || mod_moreHealthEnhanced.MaxHearts==0)
		{
			//no cap means don't mess with the variable addHealth (the total modifier from starting health can be infinite)
		}
		else
		{
			if(addHealth>mod_moreHealthEnhanced.MaxHearts*2) //check
			addHealth=mod_moreHealthEnhanced.MaxHearts*2;
			}
		moreHealthTag.setInteger("hpmax", stats.hpmax);


		//healthModifier=stats.hpmax-20; //with the now updated hpmax
		double newMax=stats.start*2+addHealth;
		//newMax calculates the player's starting health+ extra health from xp system.
		//eg if the player updates config with say 10 levels of xp (2 extra hearts) and sets start to 5 hearts, this would happen:
		//10+4 (stats.start*2+ addHealth); So now this is the player's new maxhealth

		double healthModifier=newMax-20; //in case above, healthModifier is now -6. Minecraft gives player 20-6 health=14 health.

		mod_moreHealthEnhanced.healthMod=new AttributeModifier(PlayerHandler.moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		IAttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
	//	player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.hpmax);  //setting hpmax=start*2 will let the update health method below work properly.
		player.setHealth(player.getMaxHealth());

		//if we updated the person's health
		if(stats.count>0)
		{
			player.addChatComponentMessage(new ChatComponentText("Your Life has increased and is also now fully replenished!"));
		}
		if(mod_moreHealthEnhanced.RpgMode==true && mod_moreHealthEnhanced.HeartContainersAndPieces==true)
		{
			player.addChatComponentMessage(new ChatComponentText("Enhanced mode activated! (RPG + Heart Containers)"));
		}
		else if(mod_moreHealthEnhanced.RpgMode==true)
		{
			player.addChatComponentMessage(new ChatComponentText("RPG mode enabled!"));
		}
		if(stats.start==3)
		{
			player.addChatComponentMessage(new ChatComponentText("Legend of Zelda <3"));
		}
		moreHealthTag.setInteger("count", stats.count);
		//saves potentially updated count
		try{
		stats.healthmod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(PlayerHandler.moreHealthID).getAmount();
		}
		catch (Exception e) {
		}		moreHealthTag.setDouble("healthModifier", stats.healthmod);

	}

	static void setupFirstTime(EntityPlayer player, NBTTagCompound tags, PlayerStats stats) {
		stats.start=mod_moreHealthEnhanced.StartingHearts;
		stats.LevelArray=mod_moreHealthEnhanced.LevelRampInt;

//		stats.hpmax=stats.start*2;
//		if(mod_moreHealthEnhanced.MaxHearts==-1 || mod_moreHealthEnhanced.MaxHearts==0)
//		{
//			//no cap means don't deal with maxhp
//		}
//		else
//		{
//			if(stats.hpmax>mod_moreHealthEnhanced.MaxHearts*2) //check
//				stats.hpmax=mod_moreHealthEnhanced.MaxHearts*2;
//		}
//		tags.getCompoundTag("MoreHealth").setInteger("hpmax",stats.hpmax);

        double healthModifier=stats.start*2-20;
        //if config has starting hearts= 10 (start=10), the healthModifier will be 0.
        //(aka we want minecraft to show 20+0 (base)+(more health modifier).
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(PlayerHandler.moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		IAttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);

        //initializes the modifier based on starting hearts.
		stats.count=0;
		stats.previousLevel=player.experienceLevel;
		try{
		stats.healthmod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(PlayerHandler.moreHealthID).getAmount();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		//Saving a bit different for a new player.
		NBTTagCompound moreHealthTag = (NBTTagCompound) tags.getTag("MoreHealth 1");
		moreHealthTag.setInteger("start", stats.start);
		if(mod_moreHealthEnhanced.RpgMode)
		{
			moreHealthTag.setIntArray("LevelArray", stats.LevelArray);
		}
		else
		{
			moreHealthTag.setIntArray("LevelArray", new int[] {-1});
			//since rpg mode is off, level array is set to -1 so it doesn't affect level up.
		}
		//tags.getCompoundTag("MoreHealth").setInteger("hpmax", stats.hpmax);
		moreHealthTag.setInteger("count", stats.count);
		moreHealthTag.setInteger("previousLevel", stats.previousLevel);
		moreHealthTag.setDouble("healthModifier", stats.healthmod);
		//after save, update player health
		updateHealth(player, stats, tags);
	}

	static void updatePlayerData(EntityPlayer player){
		PlayerStats stats= PlayerStats.getPlayerStats(player.getCommandSenderName());
		//changing dimensions causes modifiers to reset, so replace it here.
		double healthModifier=stats.healthmod;
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(PlayerHandler.moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		IAttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);

    	//player.setHealth(player.getMaxHealth());

	}

	public static void savePlayerData(EntityPlayer player, boolean loggedOut) {
		// TODO Auto-generated method stub
		PlayerStats stats = PlayerStats.getPlayerStats(player.getCommandSenderName());
		if (stats != null)
		{
			EntityPlayer realPlayer = stats.player; //fixes issues in Singleplayer, as mc.thePlayer isn't actually the EntityPlayer!
			//has the same name, but not same NBTTagCompound.
			NBTTagCompound entityPlayerTag;
			if(realPlayer!= null)
				entityPlayerTag = realPlayer.getEntityData();
			else
				entityPlayerTag = player.getEntityData();

			NBTTagCompound moreHealthTag = (NBTTagCompound) entityPlayerTag.getTag("MoreHealth 1");
			//tags can ONLY BE STORED WITH SETTAG, AND ARE STORED AS NBTBASE. THUS I NEED TO CAST IT BEFORE USAGE.

			moreHealthTag.setInteger("start", stats.start);
			if(mod_moreHealthEnhanced.RpgMode)
			{
				moreHealthTag.setIntArray("LevelArray", stats.LevelArray);
			}
			else
			{
				moreHealthTag.setIntArray("LevelArray", new int[] {-1});
				//since rpg mode is off, level array is set to -1 so it doesn't affect level up.
			}
			//entityPlayerTag.getCompoundTag("MoreHealth 1").setInteger("hpmax", stats.hpmax);
			moreHealthTag.setInteger("count", stats.count);
			moreHealthTag.setInteger("previousLevel", stats.previousLevel);
			//entityPlayerTag.getCompoundTag("MoreHealth").setInteger("hpmax", stats.hpmax);
			moreHealthTag.setDouble("healthModifier", stats.healthmod);
			//System.out.println("Mod: "+ stats.healthmod);
			//System.out.println(moreHealthTag.getDouble("healthModifier"));
			if (loggedOut) {
				PlayerHandler.playerStats.remove(player.getCommandSenderName());
			}
		}
	}

	static void loadPlayerData(EntityPlayer player, NBTTagCompound tags, PlayerStats stats) {
		//System.out.println(player.getCommandSenderName());
		if(!tags.hasKey("MoreHealth 1")) //new user
		{
			//System.out.println("NEW");
			tags.setTag("MoreHealth 1", new NBTTagCompound());
			NBTTagCompound temp = (NBTTagCompound) tags.getTag("MoreHealth 1");
			if(temp == null){
				//System.out.println("HI");
			}
			setupFirstTime(player, tags, stats);
			//setup for new players, then updated health for them
		}

		//stats.player=new WeakReference<EntityPlayer>(player);
		//stats.hpmax=player.maxHealth;
		//System.out.println("OLD");


		NBTTagCompound moreHealthTag = (NBTTagCompound) tags.getTag("MoreHealth 1");
		//tags can ONLY BE STORED WITH SETTAG, AND ARE STORED AS NBTBASE. THUS I NEED TO CAST IT BEFORE USAGE.

		//these two need to save start and level array even though they are
		//universal variables because if it changes, it needs to be "updated"
		stats.start=moreHealthTag.getInteger("start");
		stats.LevelArray=moreHealthTag.getIntArray("LevelArray");

		stats.count=moreHealthTag.getInteger("count");
		stats.previousLevel=moreHealthTag.getInteger("previousLevel");
		stats.healthmod=moreHealthTag.getDouble("healthModifier");
		//System.out.println(moreHealthTag.getDouble("healthModifier"));
	}
}
