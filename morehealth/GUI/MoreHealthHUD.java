package com.nohero.morehealth.GUI;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ARMOR;

import java.util.Random;

import com.nohero.morehealth.mod_moreHealthEnhanced;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.SharedMonsterAttributes;
//import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeHooks;
//import net.minecraftforge.event.ForgeSubscribe;

public class MoreHealthHUD extends Gui {
	
	private Minecraft mc;
    protected final Random rand = new Random();

	public MoreHealthHUD(Minecraft mc) {
		this.mc = Minecraft.getMinecraft();

		// TODO Auto-generated constructor stub
	}
	
    private void bind(ResourceLocation res)
    {
        mc.getTextureManager().bindTexture(res);
    }

	@SubscribeEvent
	public void modifyAirHUD(RenderGameOverlayEvent.Pre event)
	{
			if(event==null) return;
			if(event.type==null) return;
			if(event.type.equals(RenderGameOverlayEvent.ElementType.AIR))
			{
				if(mod_moreHealthEnhanced.renderCustomGUI)
				{
					if(!mod_moreHealthEnhanced.minimalisticGUI) //the minimal GUI does not need to adjust air bubbles
					{
						event.setCanceled(true);
					       mc.mcProfiler.startSection("air");
					        //added
					        ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
					        int width = res.getScaledWidth();
					        int height = res.getScaledHeight();
					        //
					        int left = width / 2 + 91;
					        int top = height - 49;
			
					        if (mc.thePlayer.isInsideOfMaterial(Material.water))
					        {
						        int level = ForgeHooks.getTotalArmorValue(mc.thePlayer);
						        if(level>0) top=top-9;
						        //if the player has armor, the air bubbles will display a level above.
						        //otherwise it will appear above food.
					        	
					            int air = mc.thePlayer.getAir();
					            int full = MathHelper.ceiling_double_int((double)(air - 2) * 10.0D / 300.0D);
					            int partial = MathHelper.ceiling_double_int((double)air * 10.0D / 300.0D) - full;
			
					            for (int i = 0; i < full + partial; ++i)
					            {
					                drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
					            }
					        }
				
			        mc.mcProfiler.endSection();
					}
				}
			}
	}
	
	@SubscribeEvent
	public void modifyArmorHUD(RenderGameOverlayEvent.Pre event)
	{
		if(event==null) return;
		if(event.type==null) return;

		if(event.type.equals(RenderGameOverlayEvent.ElementType.ARMOR))
		{
			if(mod_moreHealthEnhanced.minimalisticGUI)
			{
				GuiIngameForge.left_height+=10;
			}
			if(mod_moreHealthEnhanced.renderCustomGUI)
			{
				if(!mod_moreHealthEnhanced.minimalisticGUI) //don't need to move armor with the minimal gui.
				{
					event.setCanceled(true);
			        mc.mcProfiler.startSection("armor");
			        //added
			        ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
			        int width = res.getScaledWidth();
			        int height = res.getScaledHeight();
			        //
			        
			        //armor shifted right 100 units(pixels probably)
			        int left = width / 2 - 91+100;
			        int top = height - 49;
	
			        int level = ForgeHooks.getTotalArmorValue(mc.thePlayer);
			              
			        for (int i = 1; level > 0 && i < 20; i += 2)
			        {
			            if (i < level)
			            {
			                drawTexturedModalRect(left, top, 34, 9, 9, 9);
			            }
			            else if (i == level)
			            {
			                drawTexturedModalRect(left, top, 25, 9, 9, 9);
			            }
			            else if (i > level)
			            {
			                drawTexturedModalRect(left, top, 16, 9, 9, 9);
			            }
			            left += 8;
			        }
	
			        mc.mcProfiler.endSection();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void modifyHealthHUD(RenderGameOverlayEvent.Pre evt)
	{
		if(evt==null) return;
		if(evt.type==null) return;

		if(evt.type.equals(RenderGameOverlayEvent.ElementType.HEALTH))
		{
			if(mod_moreHealthEnhanced.minimalisticGUI)
			{
				evt.setCanceled(true);
				
		       	mc.mcProfiler.startSection("health");

		        boolean highlight = mc.thePlayer.hurtResistantTime / 3 % 2 == 1;

		        if (mc.thePlayer.hurtResistantTime < 10)
		        {
		            highlight = false;
		        }
		        //added
		        ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
		        int width = res.getScaledWidth();
		        int height = res.getScaledHeight();
		        //
		        //System.out.println("hi2");
		        int health = MathHelper.ceiling_float_int(this.mc.thePlayer.getHealth());
		        //System.out.println(health);
		        int healthLast = MathHelper.ceiling_float_int(mc.thePlayer.prevHealth);
		        int left = width / 2 - 91;
		        int top = height - 39;
		        int colorX=left-7;
		        int colorY=top+1; //a little lower
		        int regen = -1;
		        if (mc.thePlayer.isPotionActive(Potion.regeneration))
		        {
		        	//edits
		            regen = mc.ingameGUI.getUpdateCounter() % 25;
		        }
		        
				//NBTTagCompound tags = mc.thePlayer.getEntityData();
				int row= (health-1)/20; // 16/20 (8 hearts) truncates to 0= row 0.[1,20] is included in row 0.
				//this "row" if off by the displayed row by (-1)
		        for (int i = row*10; i < row*10+10; ++i)
		        {
		        	//if(mod_moreHealthEnhanced.minimalisticGUI && health>20)
		        	//	break;
		        	//if the player has more than 10 hearts (currently!), don't render first row!
		        	
                    if ((i + 1) * 2 > mc.thePlayer.getMaxHealth())
						//i+1=hearts *2=health
		        	//if(health<=20)
    				continue;  //doesn't display empty extra hearts
                    
		            int idx = i * 2 + 1;
		            int iconX = 16;
		            if (mc.thePlayer.isPotionActive(Potion.poison)) iconX += 36;
		            else if (mc.thePlayer.isPotionActive(Potion.wither)) iconX += 72;

		            int x = left + i * 8-(80*row);
		            int y = top;
		            if (health <= 4) y = top + rand.nextInt(2);
		            if (i == regen) y -= 2;

		            byte iconY = 0;
		            if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) iconY = 5;

		            drawTexturedModalRect(x, y, 16 + (highlight ? 9 : 0), 9 * iconY, 9, 9);

		            if (highlight)
		            {
		                if (idx < healthLast)
		                    drawTexturedModalRect(x, y, iconX + 54, 9 * iconY, 9, 9);
		                else if (idx == healthLast)
		                    drawTexturedModalRect(x, y, iconX + 63, 9 * iconY, 9, 9);
		            }

		            if (idx < health)
		                drawTexturedModalRect(x, y, iconX + 36, 9 * iconY, 9, 9);
		            else if (idx == health)
		                drawTexturedModalRect(x, y, iconX + 45, 9 * iconY, 9, 9);
		            
		            if(mod_moreHealthEnhanced.minimalisticGUI)
		            {
		            	int displayedRow=row+1;
				        String text = ""+displayedRow;
				        int adjustedColorX=colorX;
				        if(displayedRow>=10)
				        	adjustedColorX-=6;
				        if(displayedRow>=100) //if displayedRow>=100, it is also >=10, so -8 will happen twice
				        	adjustedColorX-=6;
				        if(displayedRow>=1000)
				        	adjustedColorX-=6;
				        if(displayedRow>=10000){
				        	text="9999+";
				        	adjustedColorX-=6;
				        }
				        FontRenderer fontrenderer=mc.fontRenderer;
						fontrenderer.drawString(text, adjustedColorX + 1, colorY, 0);
						fontrenderer.drawString(text, adjustedColorX - 1, colorY, 0);
			            fontrenderer.drawString(text, adjustedColorX, colorY + 1, 0);
			            fontrenderer.drawString(text, adjustedColorX, colorY - 1, 0);
				        fontrenderer.drawString(text, adjustedColorX, colorY, 0xf00000, false);
	
				        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				        bind(Gui.icons);
			           // TextureUtil.bindTexture("/gui/icons.png");
		            }
		            
		        }
		        mc.mcProfiler.endSection();
			}
		}
	}
    
}
