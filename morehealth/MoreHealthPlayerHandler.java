package morehealth;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.common.FMLCommonHandler;


public class MoreHealthPlayerHandler implements IPlayerTracker {

	public static final UUID moreHealthID= UUID.fromString("e3723b50-7cc6-11e3-baa7-0800200c9a66");
	private static int add=0;
	public static ConcurrentHashMap<String, MoreHealthPlayerStats> playerStats = new ConcurrentHashMap<String, MoreHealthPlayerStats>();

	
	
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		// TODO Auto-generated method stub
		NBTTagCompound tags = player.getEntityData();
		MoreHealthPlayerStats stats=new MoreHealthPlayerStats();
		if(!tags.hasKey("MoreHealth 1")) //new user
		{
			tags.setCompoundTag("MoreHealth 1", new NBTTagCompound());
			setupFirstTime(player, tags,stats);
			//setup for new players, then updated health for them
		}
		

		//stats.player=new WeakReference<EntityPlayer>(player);
		//stats.hpmax=player.maxHealth;
		
		//these two need to save start and level array even though they are 
		//universal variables because if it changes, it needs to be "updated"
		stats.start=tags.getCompoundTag("MoreHealth 1").getInteger("start");
		stats.LevelArray=tags.getCompoundTag("MoreHealth 1").getIntArray("LevelArray");
//		stats.hpmax=tags.getCompoundTag("MoreHealth").getInteger("hpmax");
		
		//System.out.println("hi "+tags.getCompoundTag("MoreHealth").getInteger("hpmax"));
		
		stats.count=tags.getCompoundTag("MoreHealth 1").getInteger("count");
		stats.previousLevel=tags.getCompoundTag("MoreHealth 1").getInteger("previousLevel");
		stats.healthmod=tags.getCompoundTag("MoreHealth 1").getDouble("healthModifier");
				
		//update health for changes in config
		if (stats.start!=mod_moreHealthEnhanced.StartingHearts||!Arrays.equals(stats.LevelArray,mod_moreHealthEnhanced.LevelRampInt))//start gets set to default config, 10. If player changes it, heart values need to be recalculated.
		{
	   		  // if(worldObj.isRemote) //if the world obj IS remote, return (so update health wont run on both server and client, will only run for "server" part 
			updateHealth(player, stats, tags); //if the client is calling and the server does not, problems occur-- health does not get updated correctly.
		}
		double healthModifier=stats.healthmod;
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
		
		//adds player to hashmap
		playerStats.put(player.username, stats);
	}

	private void setupFirstTime(EntityPlayer player, NBTTagCompound tags, MoreHealthPlayerStats stats) {
		// TODO Auto-generated method stub
		//MoreHealthPlayerStats stats=new MoreHealthPlayerStats();
		//stats.player=new WeakReference<EntityPlayer>(player);
		stats.start=mod_moreHealthEnhanced.StartingHearts;
		stats.LevelArray=mod_moreHealthEnhanced.LevelRampInt;
				
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
		tags.getCompoundTag("MoreHealth").setInteger("hpmax",stats.hpmax);

		player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20);
		double baseHealth= player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue();
        //System.out.println(baseHealth); //base health != default health. 
        player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(baseHealth);
        
        double healthModifier=stats.start*2-20; 
        //if config has starting hearts= 10 (start=10), the healthModifier will be 0.
        //(aka we want minecraft to show 20+0 (base)+(more health modifier). 
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
        
        //initializes the modifier based on starting hearts. 
		stats.count=0;
		stats.previousLevel=player.experienceLevel;
		try{
		stats.healthmod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(moreHealthID).getAmount();
		}
		catch (Exception e) {
			// TODO: handle exception
		}	
		tags.getCompoundTag("MoreHealth 1").setInteger("start", stats.start);
		if(mod_moreHealthEnhanced.RpgMode)
		{
		tags.getCompoundTag("MoreHealth 1").setIntArray("LevelArray", stats.LevelArray);
		}
		else
		{
			tags.getCompoundTag("MoreHealth 1").setIntArray("LevelArray", new int[] {-1});
			//since rpg mode is off, level array is set to -1 so it doesn't affect level up.
		}
		//tags.getCompoundTag("MoreHealth").setInteger("hpmax", stats.hpmax);
		tags.getCompoundTag("MoreHealth 1").setInteger("count", stats.count);
		tags.getCompoundTag("MoreHealth 1").setInteger("previousLevel", stats.previousLevel);
		tags.getCompoundTag("MoreHealth 1").setDouble("healthModifier", stats.healthmod);
		updateHealth(player,stats,tags);
	}

	private void updateHealth(EntityPlayer player, MoreHealthPlayerStats stats, NBTTagCompound tags) {
		// TODO Auto-generated method stub
		if(stats.start!=mod_moreHealthEnhanced.StartingHearts) //deals with the case of an exisitng user CHANGING the config file.
		{	
			stats.start=mod_moreHealthEnhanced.StartingHearts;
			player.addChatMessage("Starting Hearts sucessfully changed!");
			tags.getCompoundTag("MoreHealth 1").setInteger("start", stats.start);
		}		
		if(!Arrays.equals(stats.LevelArray,mod_moreHealthEnhanced.LevelRampInt)) //when you start up the game, levelarray gets reset (its hard to save this variable for now) but nothing should really change.
		{
			stats.LevelArray=mod_moreHealthEnhanced.LevelRampInt;
			player.addChatMessage("Level Ramp sucessfully changed!");
			tags.getCompoundTag("MoreHealth 1").setIntArray("LevelArray", stats.LevelArray);
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
		tags.getCompoundTag("MoreHealth").setInteger("hpmax",stats.hpmax);
//		
		
		//No need to fix the health modifier-- it doesn't get affected by config changes. 
		
		//double healthModifier=stats.hpmax-20; //if player starts with 10 hearts (20 health), then the modifier would be 0 from minecraft's default of 20.
		// if player wanted to start with 5 hearts (10 health) the modifier would start at -10 of 20 default health.
		//player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.hpmax); //setting hpmax=start*2 will let the update health method below work properly.		
		
		//mod_moreHealthEnhanced.healthMod=new AttributeModifier(player.getPersistentID(), "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		//AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
       // attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
       // attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
        
		//player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.hpmax);  //setting hpmax=start*2 will let the update health method below work properly.

		player.setHealth(player.getMaxHealth());
		
		if(mod_moreHealthEnhanced.RpgMode==false)
		{
		//hpmax=mod_moreHealthEnhanced.start*2; //setting hpmax=mod_moreHealthEnhanced.start*2 will let the update health method below work properly.
		player.addChatMessage("Heart Container mode enable!");	
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
				//player.maxHealth+=2; //
				//stats.hpmax+=2;
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
		tags.getCompoundTag("MoreHealth").setInteger("hpmax",stats.hpmax);
		
		
		//healthModifier=stats.hpmax-20; //with the now updated hpmax
		double newMax=stats.start*2+addHealth;
		//newMax calculates the player's starting health+ extra health from xp system. 
		//eg if the player updates config with say 10 levels of xp (2 extra hearts) and sets start to 5 hearts, this would happen:
		//10+4 (stats.start*2+ addHealth); So now this is the player's new maxhealth
		
		double healthModifier=newMax-20; //in case above, healthModifier is now -6. Minecraft gives player 20-6 health=14 health. 
		
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
	//	player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.hpmax);  //setting hpmax=start*2 will let the update health method below work properly.
		player.setHealth(player.getMaxHealth());
		
		//if we updated the person's health
		if(stats.count>0)
		{
			player.addChatMessage("Your Life has increased and is also now fully replenished!");
		}
		if(mod_moreHealthEnhanced.RpgMode==true && mod_moreHealthEnhanced.HeartContainersAndPieces==true)
		{
			player.addChatMessage("Enhanced mode activated! (RPG + Heart Containers)");
		}
		else if(mod_moreHealthEnhanced.RpgMode==true)
		{
			player.addChatMessage("RPG mode enabled!");
		}
		if(stats.start==3)
		{		
			player.addChatMessage("Legend of Zelda <3");
		}
		
		tags.getCompoundTag("MoreHealth 1").setInteger("count", stats.count);
		//saves potentially updated count
		try{
		stats.healthmod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(moreHealthID).getAmount();
		}
		catch (Exception e) {
			// TODO: handle exception
		}		tags.getCompoundTag("MoreHealth 1").setDouble("healthModifier", stats.healthmod);

	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		// TODO Auto-generated method stub
		savePlayerData(player,true);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		// TODO Auto-generated method stub
		savePlayerData(player,false);
		//changing dimension requires not only a save, but an update
		updatePlayerData(player);
	}

	private void updatePlayerData(EntityPlayer player){
		MoreHealthPlayerStats stats=getPlayerStats(player.username);
		//changing dimensions causes modifiers to reset, so replace it here.
		double healthModifier=stats.healthmod;
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
        
    	//player.setHealth(player.getMaxHealth());

	}
	private void savePlayerData(EntityPlayer player, boolean loggedOut) {
		// TODO Auto-generated method stub
		MoreHealthPlayerStats stats = getPlayerStats(player.username);
		if (stats != null)
		{
			NBTTagCompound tags = player.getEntityData();
			tags.getCompoundTag("MoreHealth 1").setInteger("start", stats.start);
			if(mod_moreHealthEnhanced.RpgMode)
			{
				tags.getCompoundTag("MoreHealth 1").setIntArray("LevelArray", stats.LevelArray);
			}
			else
			{
				tags.getCompoundTag("MoreHealth 1").setIntArray("LevelArray", new int[] {-1});
				//since rpg mode is off, level array is set to -1 so it doesn't affect level up.
			}
			//tags.getCompoundTag("MoreHealth 1").setInteger("hpmax", stats.hpmax);
			tags.getCompoundTag("MoreHealth 1").setInteger("count", stats.count);
			tags.getCompoundTag("MoreHealth 1").setInteger("previousLevel", stats.previousLevel);
			//tags.getCompoundTag("MoreHealth").setInteger("hpmax", stats.hpmax);
			tags.getCompoundTag("MoreHealth 1").setDouble("healthModifier", stats.healthmod);
			if (loggedOut)
				playerStats.remove(player.username);
		}
	}
	
	private MoreHealthPlayerStats getPlayerStats(String username) {
		// TODO Auto-generated method stub
		MoreHealthPlayerStats stats = playerStats.get(username);
		if(stats==null)
		{
			stats=new MoreHealthPlayerStats();
			{
				playerStats.put(username,stats);
			}
		}
		return stats;
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		// TODO Auto-generated method stub
		MoreHealthPlayerStats stats = getPlayerStats(player.username);
		//since everything needed is either saves in the stats or in entityPlayer(maxhealth)
		//no need to save anything here (even in ntb)
		NBTTagCompound tags = player.getEntityData();
		//player.maxHealth=tags.getCompoundTag("MoreHealth").getInteger("hpmax");
		//System.out.println("hpmax" + stats.hpmax);
		//System.out.println(player.maxHealth);
		
		//double baseHealth=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue();
		//double mod
		
		//death causes modifiers to reset, so replace it here.
		double healthModifier=stats.healthmod;
		mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
		AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
        
		//modifiers/health should stay the same. 
		
		//player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.hpmax);
		//only thing that could change is if he is in hardcore mode.
    	if(mod_moreHealthEnhanced.hcMode)
    	{
    		stats.count=0; //resets progress through level ramp
    		
    		healthModifier=(stats.start*2)-20; //start is in terms of hearts, need to convert to health.
    		mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", healthModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
    		attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
            attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
            //resets healthmodifier. 
    		//player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.start*2); //resets max health
            
    		//stats.hpmax=stats.start*2;
    		//preparePlayerToSpawn() sets health 
			//NBTTagCompound tags = player.getEntityData();
			//tags.getCompoundTag("MoreHealth").setInteger("count", stats.count);
			//tags.getCompoundTag("MoreHealth").setInteger("hpmax", stats.hpmax);

    	}	
    	
    	player.setHealth(player.getMaxHealth());

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
		tags.setCompoundTag("MoreHealth 1", tagTemp);
		try{
		stats.healthmod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(moreHealthID).getAmount();
		}
		catch (Exception e) {
			// TODO: handle exception
		}		
		tags.getCompoundTag("MoreHealth 1").setDouble("healthModifier", stats.healthmod);

		playerStats.put(player.username, stats);

	}

	@ForgeSubscribe
	public void onLivingUpdate(LivingUpdateEvent event)
	{

		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if( event.entityLiving instanceof EntityPlayer)
		{

			EntityPlayer player = (EntityPlayer) event.entityLiving;
			
			//System.out.println("MaxHealth=" + player.maxHealth);
			MoreHealthPlayerStats stats = getPlayerStats(player.username);
			//System.out.println("hpmax" + stats.hpmax);
			//if(add>0)
			//{
			//stats.hpmax+=add;
			//add=0;
			//}
			
			

			/**main logic for rpg heart gain.**/
			boolean levelIncreased=false;
			if(stats.previousLevel!=player.experienceLevel)
			{
				stats.previousLevel=player.experienceLevel;
				levelIncreased=true;
			}
			if(mod_moreHealthEnhanced.RpgMode==true && levelIncreased) // &&(this instanceof EntityPlayerMP) ) //only entityPlayerMP updates onLiving
			{	
				
				while(stats.count<mod_moreHealthEnhanced.LevelRampInt.length && player.experienceLevel>=mod_moreHealthEnhanced.LevelRampInt[stats.count])
				{ //count<...levelrampInt.length keeps the rpg side in track the second part keeps this running while explevel>count and the final part helps fix bug if the player used a heart container. That causes the count to be less than levelrampint.length even when they hit max health.
						//stops updating health on level up if rpg mode is off.
					
					//continues to update health, as long as hpmax has not capped yet.
						if(player.experienceLevel>=mod_moreHealthEnhanced.LevelRampInt[stats.count])//keeping track of term makes it so you don't have to check hp max or highest level.
						//basically, when explevel=the level of the term (terms start from 0,1,2,3, etc) health is updated and counter goes up
						{
							player.addChatMessage("Your Life has increased by one and is also now fully replenished!");
							//stats.hpmax+=2;
							//player.getPersistenID();
							double updatedModifier=0;
							try{
								updatedModifier=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(moreHealthID).getAmount()+2.0;
							}
							catch (Exception e) {
								// TODO: handle exception
							}
							//double healthModifier=newMax-20;
							mod_moreHealthEnhanced.healthMod=new AttributeModifier(moreHealthID, "More Health Heart Modifier", updatedModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
							AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
					        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
					        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
					        
							//player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(stats.hpmax);
							player.setHealth(player.getMaxHealth());
							stats.count++;				//sets count to the next xp level to gain a heart. Thus, this system deals with death/xp loss easily.
							//mod_moreHealthEnhanced.saveData();
								//with xp loss, experience became not so good a tracker. However, count will always remain the same.
						}
						
				}
			  	//if(worldObj.isRemote) //only send packet on server side.
				levelIncreased=false;
			}
			NBTTagCompound tags = player.getEntityData(); //saves changes made to nbt
			tags.getCompoundTag("MoreHealth 1").setInteger("count", stats.count);
			try{
			stats.healthmod=player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(moreHealthID).getAmount();
			}
			catch (Exception e) {
				// TODO: handle exception
			}

			tags.getCompoundTag("MoreHealth 1").setDouble("healthModifier", stats.healthmod);

			//tags.getCompoundTag("MoreHealth").setInteger("hpmax", stats.hpmax);
			
			//System.out.println("hpmax before"+ tags.getCompoundTag("MoreHealth").getInteger("hpmax"));
			
			tags.getCompoundTag("MoreHealth 1").setInteger("previousLevel", stats.previousLevel);
			
	   		//handleHpMax(stats.hpmax, player); 
			playerStats.put(player.username, stats);

		}
	}
	
    private void handleHpMax(int temp, EntityPlayer player) { //send hpmax data from server to client
		// TODO Auto-generated method stub
    	ByteArrayOutputStream bos = new ByteArrayOutputStream(4);
    	DataOutputStream outputStream = new DataOutputStream(bos);
    	try {
    	        outputStream.writeInt(temp);
    	      
    	} catch (Exception ex) {
    	        ex.printStackTrace();
    	}

    	Packet250CustomPayload packet = new Packet250CustomPayload();
    	packet.channel = "MoreHealthhpmax";
    	packet.data = bos.toByteArray();
    	packet.length = bos.size();
 
    	PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
    	//although this should send packet from server to packet, it seems to be
    	//sending from client to client.
		
	}

	public static void add(int i) {
		// TODO Auto-generated method stub
		add+=2;
	}
    
	
}
