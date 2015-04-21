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
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.Random;


public class ItemCursedHeart extends Item
{
	String[] cursed = {"You have been cursed Mortal! HAHAHAHA!", "HAHAHA, You did not ask for this!", "You really fell for it!"};
	public ItemCursedHeart()
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

		entityplayer.addPotionEffect(new PotionEffect(20, 300, 0));
		//300 ticks = 15 sec. 1 damage every 40 tick, at most 7.5 health damage
		entityplayer.addChatComponentMessage(new ChatComponentText(getCursedMessage()));

		return itemstack;
	}

	/**
	 * Randomly displays one of the cursed messages
	 * @return
	 */
	private String getCursedMessage() {
		Random randomGenerator = new Random();
		int rand =  randomGenerator.nextInt(cursed.length);
		return cursed[rand];
	}

}