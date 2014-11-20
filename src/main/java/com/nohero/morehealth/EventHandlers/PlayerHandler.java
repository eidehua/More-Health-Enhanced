package com.nohero.morehealth.EventHandlers;

import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerHandler {
	//what uniquely identifies my health attribute modifier
	public static final UUID moreHealthID= UUID.fromString("e3723b50-7cc6-11e3-baa7-0800200c9a66");
	//the map with all current users' stats stored (for the session).
	public static ConcurrentHashMap<String, PlayerStats> playerStats = new ConcurrentHashMap<String, PlayerStats>();

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent loginEvent) {
		EntityPlayer player = loginEvent.player;
		NBTTagCompound tags = player.getEntityData();
		PlayerStats stats=new PlayerStats();

		PlayerHandlerHelper.loadPlayerData(player, tags, stats);	//loads a player from the saved data (or sets up new user)

		//update health for changes in config
		if (stats.start!= mod_moreHealthEnhanced.StartingHearts||!Arrays.equals(stats.LevelArray,mod_moreHealthEnhanced.LevelRampInt))//start gets set to default config, 10. If player changes it, heart values need to be recalculated.
		{
			PlayerHandlerHelper.updateHealth(player, stats, tags); //if the client is calling and the server does not, problems occur-- health does not get updated correctly.
		}

		double healthModifier=stats.healthmod;
		addHealthModifier(player, healthModifier);
		//System.out.println("stat: "+ healthModifier);
		//adds player stats to hash map
		stats.player = player;
		playerStats.put(player.getCommandSenderName(), stats);
	}

	public static void addHealthModifier(EntityPlayer player, double healthModifier) {
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		IAttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
		attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
		attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
	}

	/**
	 * Seems to only work for server log out. Need a workaround for single player
	 * @param event
	 */
	@SubscribeEvent
	public void onPlayerLogsout(PlayerEvent.PlayerLoggedOutEvent event) {
		EntityPlayer currentPlayer = event.player;
		//System.out.println("logged out");
		PlayerHandlerHelper.savePlayerData(currentPlayer, true);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onServerStop(FMLNetworkEvent.ClientDisconnectionFromServerEvent event){ // Fired at the client when a client disconnects.
		//Called on both client and/or dedicated server.
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT)
		{
			//Called only if you go into SP world, then quit, world is unloaded.
			Minecraft mc = FMLClientHandler.instance().getClient();
			EntityPlayer currentPlayer=mc.thePlayer;
			//System.out.println("client disconnect");
			PlayerHandlerHelper.savePlayerData(currentPlayer, true);
		}
	}
	//thanks http://www.minecraftforge.net/forum/index.php?topic=24451.0

	@SubscribeEvent //server side only AGAIN
	public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent changedDimensionEvent) {
		Side side = FMLCommonHandler.instance().getEffectiveSide();

		EntityPlayer currentPlayer = changedDimensionEvent.player;
		PlayerHandlerHelper.savePlayerData(currentPlayer, false);
		//changing dimension requires not only a save, but an update-- updates the health modifier, then applies it
		PlayerHandlerHelper.updatePlayerData(currentPlayer);
		//System.out.println("Max Health: "+currentPlayer.getMaxHealth());
		//currentPlayer.setHealth(currentPlayer.getMaxHealth());

		//set variable true so during OnLivingUpdate the user is forced to update the client side health
		PlayerStats stats = PlayerStats.getPlayerStats(currentPlayer.getCommandSenderName());
		stats.needClientSideHealthUpdate = true;
	}

//	@SubscribeEvent //client side
//	public void onWorldUnload(WorldEvent.Unload event) {
//		Side side = FMLCommonHandler.instance().getEffectiveSide();
//		if (side == Side.CLIENT)
//		{
//			//Called only if you go into SP world, then unload-- on log out, or on world change.
//			Minecraft mc = FMLClientHandler.instance().getClient();
//			EntityPlayer currentPlayer=mc.thePlayer;
//			System.out.println("client change world");
//			PlayerHandlerHelper.savePlayerData(currentPlayer, false);
//			//changing dimension requires not only a save, but an update-- updates the health modifier, then applies it
//			PlayerHandlerHelper.updatePlayerData(currentPlayer);
//			System.out.println("Max Health: "+currentPlayer.getMaxHealth());
//			currentPlayer.setHealth(currentPlayer.getMaxHealth());
//		}
//	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent respawnEvent) {
		EntityPlayer currentPlayer = respawnEvent.player;
		PlayerStats stats = PlayerStats.getPlayerStats(currentPlayer.getCommandSenderName());
		//since everything needed is either saves in the stats or in entityPlayer(maxhealth)
		//no need to save anything here (even in ntb)
		//NBTTagCompound tags = currentPlayer.getEntityData();

		//death causes modifiers to reset, so replace it here.
		double healthModifier=stats.healthmod;
		addHealthModifier(currentPlayer, healthModifier);

		//modifiers/health should stay the same.

		//currentPlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.hpmax);
		//only thing that could change is if he is in hardcore mode.
    	if(mod_moreHealthEnhanced.hcMode)
    	{
    		stats.count=0; //resets progress through level ramp
    		double baseHearts = currentPlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue();
    		healthModifier=(stats.start*2)-baseHearts; //start is in terms of hearts, need to convert to health.
			addHealthModifier(currentPlayer, healthModifier);
			//save the player data only if it's been changed (player is hc mode => respawn causes a change in my mod data)

		}
    	currentPlayer.setHealth(currentPlayer.getMaxHealth());
		//all players need update tag compound, as respawn creates a "new" player with unknown health tag compound
		NBTTagCompound tags = currentPlayer.getEntityData();
		NBTTagCompound tagTemp=new NBTTagCompound();
		tagTemp.setInteger("count", stats.count);
		//tagTemp.setInteger("hpmax", stats.hpmax);
		tagTemp.setInteger("start", stats.start);
		tagTemp.setInteger("previousLevel", stats.previousLevel);
		if(mod_moreHealthEnhanced.RpgMode)
		{
			tagTemp.setIntArray("LevelArray", stats.LevelArray);
		}
		else
		{
			tagTemp.setIntArray("LevelArray", new int[] {-1});
			//since rpg mode is off, level array is set to -1 so it doesn't affect level up.
		}
		tags.setTag("MoreHealth 1", tagTemp);
		try{
			stats.healthmod=currentPlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(moreHealthID).getAmount();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		tags.getCompoundTag("MoreHealth 1").setDouble("healthModifier", stats.healthmod);

		playerStats.put(currentPlayer.getCommandSenderName(), stats);
	}

}
