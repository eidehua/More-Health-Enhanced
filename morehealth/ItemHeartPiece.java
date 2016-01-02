package morehealth;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public class ItemHeartPiece extends Item
{
  public ItemHeartPiece(int x)
  {
    super(x);
    this.setCreativeTab(CreativeTabs.tabMisc);
    this.setUnlocalizedName("heartPiece");
  }
  
  @Override
  public void registerIcons(IconRegister par1IconRegister)
  {
      this.itemIcon = par1IconRegister.registerIcon("morehealth:" + "heartPiece");
  }
//   public String getTextureFile()
//   {
//           return "/morehealth/HeartItems.png";
//   }
}