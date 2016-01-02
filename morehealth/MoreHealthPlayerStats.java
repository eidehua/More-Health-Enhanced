package morehealth;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;

public class MoreHealthPlayerStats {
	
	//public WeakReference<EntityPlayer> player;
	//public int level;

	public  int[] LevelArray;
	public  int start;
	public  int hpmax; //tracks max health of each player
	public  int count; //tracks current position in LevelRamp array.
	public 	int previousLevel; //previous exp lvl of player.
	//public  int healthModifier;
	public double healthmod;
	
	
}
