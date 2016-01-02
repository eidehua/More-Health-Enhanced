package com.nohero.morehealth;


import com.nohero.morehealth.Enchantments.ArmorHealthEnchantment;
import com.nohero.morehealth.EventHandlers.ForgeEventHandler;
import com.nohero.morehealth.EventHandlers.PlayerHandler;
import com.nohero.morehealth.GUI.MoreHealthGui;
import com.nohero.morehealth.GUI.MoreHealthGuiHandler;
import com.nohero.morehealth.GUI.MoreHealthHUD;
import com.nohero.morehealth.Items.ItemCursedHeart;
import com.nohero.morehealth.Items.ItemHeart;
import com.nohero.morehealth.Items.ItemHeartPiece;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.lwjgl.input.Keyboard;

@Mod(modid = mod_moreHealthEnhanced.modid, name = mod_moreHealthEnhanced.name, version = mod_moreHealthEnhanced.version, guiFactory = "com.nohero.morehealth.GUI.MoreHealthGuiFactory")
public class mod_moreHealthEnhanced{
	
	public static final String modid = "morehealth";
	public static final String name = "More Health Forge";
	public static final String version = "6.2";

	public static final String guiOptions="gui options";
	
	public static int[] LevelRampInt;
	
	public static int StartingHearts = 10;

	public static int MaxHearts = -1;

	public static String LevelRamp = ("1,5,10,15,20,25,30,34,38,42,46,50,53,56,59,62,64,66,68,70,72,74,76,78,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,102,104,106,108,110,120,130,140,150,160,170,180,190,200,220,240,260,280,300,320,340,360,380,400,500,600,700,800,1000,1500,2000,2500,3000,9001");

	public static boolean HeartContainersAndPieces = true;

	public static boolean RpgMode = true;

	public static boolean Enchantments = true;

	public static Item heartContainer;
	private static Item heartPiece;
	private static Item cursedHeart;

	private static double multiply =1.0;
	public static boolean hcMode=false; 
	//public static final String modid = "nohero_moreHealth";
	
	public static boolean renderCustomGUI=true;
	public static boolean minimalisticGUI=false;
	
	private static Property startHearts;
	private static Property maxHearts;	
	private static Property levelRamp;
	private static Property heartItems;
	private static Property rpg;
	private static Property multiplier;
	private static Property hardcore;
	private static Property enchantment;
	private static Property customGui;
	private static Property minimalGui;
	private static Property armorEnchantmentID;
	private static Property guiKeyBinding;

	public static AttributeModifier healthMod;

	public static int heartArmorEnchantID=120; //the enchantment ID for hearts -- should be configurable
	//(ID, weight)
	//weight from 1-10, 1 being rare, 10 being more common
	//Adds my enchantment to the possible enchantments list
	public static Enchantment armorEnchantment = null;

	@Mod.Instance(modid)
	public static mod_moreHealthEnhanced instance;

	public static Configuration config;
	private boolean guiOpened=false;

	@EventHandler
	public void  preInit(FMLPreInitializationEvent event) {
		//Minecraft mc = FMLClientHandler.instance().getClient();
		config= new Configuration(event.getSuggestedConfigurationFile());
		// loading the configuration from its file
		config.load();
		updateConfig();
	}

	/**
	 * Sets up the config for my mod. It's large because I have many options that users can change.
	 * I set up Properties to be the value from the config file.
	 */
	private void updateConfig() {


		multiplier=config.get(Configuration.CATEGORY_GENERAL, "Heart Item Multiplier", 1.0);
		multiplier.comment="This is the multiplier for chest heart item loot. Really useful to change on large servers. IF POSSIBLE, CHANGE BEFORE GENERATING WORLD. Multiplier changes only affects newly generated areas.";

		startHearts= config.get(Configuration.CATEGORY_GENERAL, "Starting Hearts", 10);
		startHearts.comment="The hearts you start with in all your worlds. Default 10.";

		maxHearts= config.get(Configuration.CATEGORY_GENERAL, "Max Hearts", -1);
		maxHearts.comment="The cap amount of hearts. Default (-1 or 0) means no cap.";

		levelRamp= config.get(Configuration.CATEGORY_GENERAL, "Level Ramp", "1,5,10,15,20,25,30,34,38,42,46,50,53,56,59,62,64,66,68,70,75,80,85,90,95,100,110,120,130,140,150,160,170,180,190,200,210,220,230,240,250,260,270,280,290,300,310,320,330,340,350,360,370,380,390,400,420,440,460,500");
		//60 extra hearts
		levelRamp.comment="The levels where you can the heart. Fully customizable in the fields below.";

		heartItems= config.get(Configuration.CATEGORY_GENERAL, "Heart Container and Pieces", true);
		heartItems.comment="in the field below, type true to enable heart items and type false to disable them. Default on.";

		rpg= config.get(Configuration.CATEGORY_GENERAL, "RPG Mode", true);
		rpg.comment="in the field below, type true to enable rpg mode and type false to disable them. Default on.";

		hardcore= config.get(Configuration.CATEGORY_GENERAL, "Hardcore Mode", false);
		hardcore.comment="Set to true to enable hardcore mode. After death, you restart back at your starting hearts value.";
		// saving the configuration to its file

		enchantment = config.get(Configuration.CATEGORY_GENERAL, "Enchantments", true);
		enchantment.comment = "Set false to remove the heart enchantment for armors";

		customGui= config.get(guiOptions, "More Health HUD", true);
		customGui.comment="By default, more health will customize the HUD so that heart rows are possible. Set this to false AND set minimal HUD to false if it is conflicting with one of your HUD/GUI mods that have their own heart HUD.";

		minimalGui=config.get(guiOptions, "Minimal HUD", true);
		minimalGui.comment="Set to true to enable minimal gui. Displays heart information in one row. A number should appear next to your hearts telling you what row you are on. Row 1= Hearts 1-10. Row 2=Hearts 11-20. Turn this on if there is a conflict with other HUD/GUI mods that DO NOT have their own heart HUD";

		armorEnchantmentID=config.get(Configuration.CATEGORY_GENERAL, "Armor Enchantments ID", 120);
		armorEnchantmentID.comment="Adjust the Armor Enchants ID in case of a conflict with other custom enchantments";

		guiKeyBinding = config.get(Configuration.CATEGORY_GENERAL, "More Health Stats Key", "H");
		guiKeyBinding.comment = "Set the key you want to use to open up the gui with More Health stats. Supports alphanumeric. WARNING! Will unbind if key was used before!";

		if(config.hasChanged())
			config.save();
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT )
		{
			Minecraft mc = FMLClientHandler.instance().getClient();
			MinecraftForge.EVENT_BUS.register(new MoreHealthHUD(mc));
		}
		MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
		FMLCommonHandler.instance().bus().register(instance);
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new MoreHealthGuiHandler());


		String keyName = guiKeyBinding.getString().toUpperCase();
		MoreHealthGui.keyBinding= new KeyBinding("key.hud.desc", Keyboard.getKeyIndex(keyName), "key.morehealth.category");

		ClientRegistry.registerKeyBinding(MoreHealthGui.keyBinding);

		setUpValuesFromProperties();

		if(minimalisticGUI)
		{
			renderCustomGUI=true; //makes sure this is set to true. It's still a custom gui, but a minimal one. 
		}

		if (!RpgMode) {
			LevelRampInt = new int[1];
			LevelRampInt[0] = -1; // stops RPG element

		} else {
			try {
				LevelRampInt = convertedStringArray(LevelRamp.split(","));
			} catch (Exception e) {
				System.out
						.println("There is a error in your config file. Make sure there is no extra ',' in the line for Ramp. ");
			}
		}
		if (HeartContainersAndPieces) {
			heartContainer = new ItemHeart();
			heartPiece = new ItemHeartPiece();
			cursedHeart = new ItemCursedHeart();

			GameRegistry.registerItem(heartContainer, "heartContainer");
			GameRegistry.registerItem(heartPiece, "heartPiece");
			GameRegistry.registerItem(cursedHeart, "cursedHeart");

			GameRegistry.addRecipe(new ItemStack(heartContainer, 1), new Object[] {
					"XX", "XX", Character.valueOf('X'), heartPiece });

			addChestLoot();
		}
		if(Enchantments){ //what happens to users who had enchanted items then turn off enchantments?
			armorEnchantment= new ArmorHealthEnchantment(heartArmorEnchantID, 4);
		}
	}

	/**
	 * For when a player changes config file in game
	 * @param eventArgs
	 */
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(modid))
			updateConfig(); //update our config file
			setUpValuesFromProperties(); //make in game changes
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onKeyEvent(InputEvent.KeyInputEvent event){
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;

		if(MoreHealthGui.keyBinding.isPressed()){

			player.openGui(instance, MoreHealthGui.id, mc.theWorld, 0,0,0);
		}
	}
	/**
	 * I take the properties and make them the types they need to be for me to use.
	 */
	private void setUpValuesFromProperties() {
		if(maxHearts.getInt()==0) maxHearts.set("-1");
		StartingHearts=startHearts.getInt();
		MaxHearts=maxHearts.getInt();
		LevelRamp=levelRamp.getString();
		HeartContainersAndPieces=heartItems.getBoolean(true); //defaults to true
		RpgMode=rpg.getBoolean(true);
		multiply=multiplier.getDouble(1.0); //multiplier for loot gen.
		hcMode=hardcore.getBoolean(false);
		Enchantments = enchantment.getBoolean(true);
		renderCustomGUI=customGui.getBoolean(true);//default heart gui.
		minimalisticGUI=minimalGui.getBoolean(true); //minimal gui set on default
		heartArmorEnchantID=armorEnchantmentID.getInt();
				/*In Keyboard
		String name = field.getName().substring(4);
		Aka the fields are "KEY_1" ... "KEY_H"
		so the key name is just the part without "KEY_"
		*/
		String keyName = guiKeyBinding.getString().toUpperCase();
		int keyIndex = Keyboard.getKeyIndex(keyName);
		if(MoreHealthGui.keyBinding.getKeyCode()!=keyIndex) {
			MoreHealthGui.keyBinding.setKeyCode(keyIndex);    //Supports alphanumeric characters
			KeyBinding.resetKeyBindingArrayAndHash();  		  //Reset all to "free up" old key binding in hash and "add new" key binding in.
			//When you want to change the key binding, need to reset all key bindings in the hash
			//The array stores each individual key binding class
			//Hash stores pairs of ("key", corresp class)
		}
	}

	private void addChestLoot() {
		ChestGenHooks dungeon=ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST);
		dungeon.addItem(new WeightedRandomChestContent(new ItemStack(heartContainer), 1, 1, (int) (6*multiply))); //1-1
		dungeon.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 1, 3, (int) (8*multiply))); //1-1

		ChestGenHooks desert= ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST);
		desert.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 2, 3,(int) (9*multiply))); //2-3
		desert.addItem(new WeightedRandomChestContent(new ItemStack(heartContainer), 1, 1, (int) (5*multiply))); //1-1

		ChestGenHooks jungle= ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST);
		jungle.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 2, 3, (int) (8*multiply))); //2-3
		jungle.addItem(new WeightedRandomChestContent(new ItemStack(heartContainer), 1, 1, (int) (5*multiply))); //1-1

		ChestGenHooks library= ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY);
		library.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 1, 2, (int) (5*multiply)));

		ChestGenHooks corridor= ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR);
		corridor.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 1, 3, (int) (8*multiply))); //2-3
		corridor.addItem(new WeightedRandomChestContent(new ItemStack(heartContainer), 1, 1, (int) (6*multiply))); //1-1

		ChestGenHooks blacksmith= ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH);
		blacksmith.addItem(new WeightedRandomChestContent(new ItemStack(heartContainer), 1, 1, (int) (8*multiply))); //1-1

		ChestGenHooks crossing= ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING);
		crossing.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 1, 2, (int) (7*multiply))); //2-4

		ChestGenHooks mineshaft= ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR);
		mineshaft.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 1, 2,(int) (7*multiply))); //2-4

		//ChestGenHooks testBonus= ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST);
		//testBonus.addItem(new WeightedRandomChestContent(new ItemStack(heartPiece), 1, 3, (int) (8*multiply))); //2-4
	}

	/**
	 * Add in the player tracker -- keeps the health modifier working through death/respawn/logout/login/changedimension
	 * @param evt
	 */
	@EventHandler
    public void postInit (FMLPostInitializationEvent evt)
    {

        playerTracker = new PlayerHandler();
		FMLCommonHandler.instance().bus().register(playerTracker);
		MinecraftForge.EVENT_BUS.register(playerTracker); //for the world unload function
	}
	
	public int[] convertedStringArray(String[] sarray) throws Exception {
		if (sarray != null) {
			int intarray[] = new int[sarray.length];
			for (int i = 0; i < sarray.length; i++) {
				intarray[i] = Integer.parseInt(sarray[i]);
			}
			return intarray;
		}
		return null;
	} // manikandanmv.wordpress.com string array to int array java.

	public static PlayerHandler playerTracker;

	public static void updateKeyBindings() {
		String keyName = guiKeyBinding.getString().toUpperCase();
		int keyIndex = Keyboard.getKeyIndex(keyName);
		if(MoreHealthGui.keyBinding.getKeyCode()!=keyIndex) {
			MoreHealthGui.keyBinding.setKeyCode(keyIndex);    //Supports alphanumeric characters
			KeyBinding.resetKeyBindingArrayAndHash();  		  //Reset all to "free up" old key binding in hash and "add new" key binding in.
			//When you want to change the key binding, need to reset all key bindings in the hash
			//The array stores each individual key binding class
			//Hash stores pairs of ("key", corresp class)
		}
	}
}
