package morehealth;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public class ItemHeart extends Item
{
	public ItemHeart(int x)
	{
		super(x);
		this.setCreativeTab(CreativeTabs.tabMisc);
		maxStackSize = 1;
		this.setUnlocalizedName("heart_container");
	}
	

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon("morehealth:" + "heart_container");
	}
	
   public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
	   Side side = FMLCommonHandler.instance().getEffectiveSide();
	   if(side.isClient()) return itemstack;
	   
	   if(mod_moreHealthEnhanced.HeartContainersAndPieces==false)
	   {
		   entityplayer.addChatMessage("Can't use, please turn heart containers on in the config!");
		   return itemstack;
	   }
		itemstack.stackSize--;
/**		if(entityplayer.getMaxHealth()+2>mod_moreHealthEnhanced.MaxHearts*2) //check- in this case, heart container acts as a full heal item
		{
			//entityplayer.maxHealth=mod_moreHealthEnhanced.MaxHearts*2;
			entityplayer.addChatMessage("Your Life is fully replenished!");

		} **/
		//else //otherwise it acts like a heart container
		
			//sets max health to be 2 greater than current max health
			//entityplayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(entityplayer.getMaxHealth()+2);
			
			//no need for try catch, since I add an modifier when player installs.
			double updatedModifier=2;
			try{
			updatedModifier=entityplayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(MoreHealthPlayerHandler.moreHealthID).getAmount()+2.0;
			}
			catch (Exception e) {
				// TODO: handle exception
			}
			mod_moreHealthEnhanced.healthMod=new AttributeModifier(MoreHealthPlayerHandler.moreHealthID, "More Health Heart Modifier", updatedModifier,0); //note modifier can be negative, as a +0=20 health (10 hearts) start. The player may choose to start at less hearts, so less than 20 health.
			AttributeInstance attributeinstance = entityplayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
	        attributeinstance.removeModifier(mod_moreHealthEnhanced.healthMod);
	        attributeinstance.applyModifier(mod_moreHealthEnhanced.healthMod);
			entityplayer.addChatMessage("Your Life has increased by one and is also now fully replenished!");
			//MoreHealthPlayerHandler.add(2); //adds to hpmax;
			MoreHealthPlayerStats stats = getPlayerStats(entityplayer.username);
			stats.healthmod=mod_moreHealthEnhanced.healthMod.getAmount();
			//stats.hpmax+=2;

		



		entityplayer.setHealth(entityplayer.getMaxHealth());
		return itemstack;
  }
	
	private MoreHealthPlayerStats getPlayerStats(String username) {
		// TODO Auto-generated method stub
		MoreHealthPlayerStats stats = MoreHealthPlayerHandler.playerStats.get(username);
		if(stats==null)
		{
			stats=new MoreHealthPlayerStats();
			{
				MoreHealthPlayerHandler.playerStats.put(username,stats);
			}
		}
		return stats;
	}
//   public String getTextureFile()
//   {
//           return "/morehealth/HeartItems.png";
//   }
}