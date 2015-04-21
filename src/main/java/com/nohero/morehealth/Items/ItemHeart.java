package com.nohero.morehealth.Items;

import com.nohero.morehealth.EventHandlers.PlayerHandler;
import com.nohero.morehealth.PlayerStats;
import com.nohero.morehealth.mod_moreHealthEnhanced;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
//import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
//import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;


public class ItemHeart extends Item
{
	public ItemHeart()
	{
		super();
		setUnlocalizedName(mod_moreHealthEnhanced.modid+"_heartContainer");
		setTextureName(mod_moreHealthEnhanced.modid+":heartContainer");
		this.setCreativeTab(CreativeTabs.tabMisc);
		//setMaxStackSize(1);
		maxStackSize = 64;
		//this.setCreativeTab(CreativeTabs.tabInventory);
	}
	
   public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
	   Side side = FMLCommonHandler.instance().getEffectiveSide();
	   if(side.isClient()) return itemstack;
	   
	   if(mod_moreHealthEnhanced.HeartContainersAndPieces==false)
	   {
		   entityplayer.addChatComponentMessage(new ChatComponentText("Can't use, please turn heart containers on in the config!"));
		   return itemstack;
	   }
		itemstack.stackSize--;
		//Need to add in max health feature again later
		if(mod_moreHealthEnhanced.MaxHearts== -1 || mod_moreHealthEnhanced.MaxHearts == 0){
			//don't do the below check if MaxHearts cap is turned off (-1 or 0)
		}
		else if(entityplayer.getMaxHealth()+2>mod_moreHealthEnhanced.MaxHearts*2) //check- in this case, heart container acts as a full heal item
		{
			//entityplayer.maxHealth=mod_moreHealthEnhanced.MaxHearts*2;
			entityplayer.addChatComponentMessage(new ChatComponentText("Your Life is fully replenished!"));
			entityplayer.setHealth(entityplayer.getMaxHealth());
			return itemstack;

		}
			//otherwise it acts like a heart container
			//sets max health to be 2 greater than current max health
			//entityplayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(entityplayer.getMaxHealth()+2);
			
			//no need for try catch, since I add an modifier when player installs.
			double updatedModifier=2;
			try{
			updatedModifier=entityplayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(PlayerHandler.moreHealthID).getAmount()+2.0;
			}
			catch (Exception e) {
				// TODO: handle exception
			}
			PlayerHandler.addHealthModifier(entityplayer, updatedModifier);
			//removed code smell

			entityplayer.addChatComponentMessage(new ChatComponentText("Your Life has increased by one and is also now fully replenished!"));
			PlayerStats stats = PlayerStats.getPlayerStats(entityplayer.getCommandSenderName());
			stats.healthmod=mod_moreHealthEnhanced.healthMod.getAmount();

		entityplayer.setHealth(entityplayer.getMaxHealth());
		return itemstack;
  }
}