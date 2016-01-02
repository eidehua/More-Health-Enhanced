package com.nohero.morehealth.Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
//import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemHeartPiece extends Item
{
  public ItemHeartPiece()
  {
    super();
    this.setCreativeTab(CreativeTabs.tabMisc);
	 setTextureName("morehealth:heartPiece");
	 this.setUnlocalizedName("heartPiece");
  }

}