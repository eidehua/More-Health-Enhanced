package com.nohero.morehealth.GUI;

import com.nohero.morehealth.mod_moreHealthEnhanced;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Ed Xue on 12/4/2014.
 */
public class MoreHealthGuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ModConfigGUI.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

	public static class ModConfigGUI extends GuiConfig {

		public ModConfigGUI(GuiScreen parent) {
			super(parent,
					getConfigElements(),
					mod_moreHealthEnhanced.modid, false, false, GuiConfig.getAbridgedConfigPath(mod_moreHealthEnhanced.config.toString()));
		}

		private static List<IConfigElement> getConfigElements()
		{
			List<IConfigElement> list = new ArrayList<IConfigElement>();
			list.addAll(new ConfigElement(mod_moreHealthEnhanced.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
			list.addAll(new ConfigElement(mod_moreHealthEnhanced.config.getCategory(mod_moreHealthEnhanced.guiOptions)).getChildElements());
			return list;
		}
	}
}
