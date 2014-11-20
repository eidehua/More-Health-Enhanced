package com.nohero.morehealth.Enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.util.StatCollector;

/**
 * Created by Ed Xue on 11/19/2014.
 */
public class ArmorHealthEnchantment extends Enchantment {


	public ArmorHealthEnchantment(int effectID, int weight) {
		super(effectID, weight, EnumEnchantmentType.armor);
		this.setName("Hearts");
	}

	/**
	 * Returns the minimal value of enchantability needed on the enchantment level passed.
	 */
	public int getMinEnchantability(int enchantLevel) {
		//I have enchant Levels of 1-5, so the min level of enchantability needed is 10 at I enchantment
		//and 30 at V enchantment
		return 5+ 5*enchantLevel;
	}

	/**
	 * Returns the maximum value of enchantability needed on the enchantment level passed.
	 */
	public int getMaxEnchantability(int enchantLevel) {
		return getMinEnchantability(enchantLevel) + 50;
	}

	/**
	 * Returns the maximum level that the enchantment can have.
	 */
	public int getMaxLevel() {
		return 5;
	}

	//Override to set the name easily without having to deal with the statcollector or StringTranslate
	//may not be supported by forge
	public String getTranslatedName(int p_77316_1_)
	{
		String s = "Hearts";
		return s + " " + StatCollector.translateToLocal("enchantment.level." + p_77316_1_);
	}
	/**
	 * CommandEnchant class
	 * processCommand method
	 * This is where an enchant is applied-- I need to grab the item and apply hearts based on the enchantment's enchant level
	 * itemstack.addEnchantment(enchantment, j); j = enchantment level
	 * I need to get the enchantment when an enchantment is applied, check if it's an armor health enchantment that is applied
	 * Then I have to update health based on enchantment level
	 * I can get the level from getEnchantmentLevel(enchantmentID, itemstack) (since an item(stack) can have more than one enchantment
	 * I need to update a players health when he EQUIPS or REMOVES a heart enchanted armor-- aka not on CommandEvent
	 */
	/** Through find usage (library files)
	 * public static final Enchantment respiration = new EnchantmentOxygen(5, 2);
	 * EnchantmentHelper has getRespiration(EntityLivingBase) to get the oxygen enchantments from that entity
	 * Then used in ENtityLivingBase to decreaseAirSupply based on (potentially) enchanted breathing times
	 */

	/**
	 * Obfuscated functions:
	 * public void func_151368_a(EntityLivingBase p_151368_1_, Entity p_151368_2_, int p_151368_3_) {}
	 * ^ when player hit something? (EnchantmentHelper DamageIterator)
	 *
	 * public void func_151367_b(EntityLivingBase p_151367_1_, Entity p_151367_2_, int p_151367_3_) {}
	 * ^ when something is hurt? (EnchantmentHelper HurtIterator)
	 */

	/**
	 * No need to override
	 * Our Enchantment should only work on armors
	 *  canApply(ItemStack itemstack) => default behavior (apply only on items of our type (EnumEnchantmentType.armor) works
	 *  canApplyAtEnchantingTable => yes we want it to apply at enchanting table
	 *
	 */
}
