package com.nohero.morehealth;

import com.nohero.morehealth.EventHandlers.PlayerHandler;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerStats {
	
	//public WeakReference<EntityPlayer> player;
	//public int level;

	public  int[] LevelArray;
	public  int start;
	public  int hpmax; //tracks max health of each player
	public  int count; //tracks current position in LevelRamp array.
	public 	int previousLevel; //previous exp lvl of player.
	public double healthmod;
	public EntityPlayer player; //the player we are referring to
	public boolean needClientSideHealthUpdate = false;

	public static PlayerStats getPlayerStats(String username) {
		// TODO Auto-generated method stub
		PlayerStats stats = PlayerHandler.playerStats.get(username);
		if(stats==null)
		{
			stats=new PlayerStats();
			{
				PlayerHandler.playerStats.put(username,stats);
			}
		}
		return stats;
	}
}
