package morehealth;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import cpw.mods.fml.common.FMLCommonHandler;

public class MoreHealthHooks {

	
	@ForgeSubscribe
	public void onEntityLivingDeath(LivingDeathEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			//bosses drop 1 heart contaienr each
			if (event.entity instanceof EntityDragon && mod_moreHealthEnhanced.HeartContainersAndPieces) {
				
				event.entity.entityDropItem(new ItemStack(mod_moreHealthEnhanced.heartContainer),0.0F);
			}
			if (event.entity instanceof EntityWither && mod_moreHealthEnhanced.HeartContainersAndPieces) {
				
				event.entity.entityDropItem(new ItemStack(mod_moreHealthEnhanced.heartContainer),0.0F);
				//EntityItem entityItem;
				
				//entityItem = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, new ItemStack(mod_moreHealthEnhanced.heartContainer,1,0));
				//event.entity.worldObj.spawnEntityInWorld(entityItem);
			
			}
		}
	}
	
}
