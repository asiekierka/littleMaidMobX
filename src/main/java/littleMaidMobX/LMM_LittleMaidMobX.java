package littleMaidMobX;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import mmmlibx.lib.FileManager;
import mmmlibx.lib.MMM_Config;
import mmmlibx.lib.MMM_Helper;
import mmmlibx.lib.MMM_TextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import network.W_Network;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.List;

@Mod(	modid = LMM_LittleMaidMobX.DOMAIN,
		name  = LMM_LittleMaidMobX.DOMAIN)
public class LMM_LittleMaidMobX {
	
	public static final String DOMAIN = "lmmx";

	public static String[] cfg_comment = {
		"spawnWeight = Relative spawn weight. The lower the less common. 10=pigs. 0=off",
		"spawnLimit = Maximum spawn count in the World.",
		"minGroupSize = Minimum spawn group count.",
		"maxGroupSize = Maximum spawn group count.",
		"canDespawn = It will despawn, if it lets things go. ",
		"checkOwnerName = At local, make sure the name of the owner. ",
		"antiDoppelganger = Not to survive the doppelganger. ",
		"enableSpawnEgg = Enable LMM SpawnEgg Recipe. ",
		"VoiceDistortion = LittleMaid Voice distortion.",
		"defaultTexture = Default selected Texture Packege. Null is Random",
		"DebugMessage = Print Debug Massages.",
		"DeathMessage = Print Death Massages.",
		"Dominant = Spawn Anywhere.",
		"Aggressive = true: Will be hostile, false: Is a pacifist",
		"IgnoreItemList = aaa, bbb, ccc: Items little maid to ignore",
//		"AchievementID = used Achievement index.(0 = Disable)",
//		"UniqueEntityId = UniqueEntityId(0 is AutoAssigned. max 255)"
	};
	
//	@MLProp(info="Relative spawn weight. The lower the less common. 10=pigs. 0=off")
	public static int cfg_spawnWeight = 5;
//	@MLProp(info="Maximum spawn count in the World.")
	public static int cfg_spawnLimit = 20;
//	@MLProp(info="Minimum spawn group count.")
	public static int cfg_minGroupSize = 1;
//	@MLProp(info="Maximum spawn group count.")
	public static int cfg_maxGroupSize = 3;
//	@MLProp(info="It will despawn, if it lets things go. ")
	public static boolean cfg_canDespawn = false;
//	@MLProp(info="At local, make sure the name of the owner. ")
	public static boolean cfg_checkOwnerName = false;
//	@MLProp(info="Not to survive the doppelganger. ")
	public static boolean cfg_antiDoppelganger = true;
//	@MLProp(info="Enable LMM SpawnEgg Recipe. ")
	public static boolean cfg_enableSpawnEgg = true;
	
	
//	@MLProp(info="LittleMaid Voice distortion.")
	public static boolean cfg_VoiceDistortion = true;
	
//	@MLProp(info="Default selected Texture Packege. Null is Random")
	public static String cfg_defaultTexture = "";
//	@MLProp(info="Print Debug Massages.")
	public static boolean cfg_PrintDebugMessage = false;
//	@MLProp(info="Print Death Massages.")
	public static boolean cfg_DeathMessage = true;
//	@MLProp(info="Spawn Anywhere.")
	public static boolean cfg_Dominant = false;
//	@MLProp(info="true: AlphaBlend(request power), false: AlphaTest(more fast)")
//	public static boolean AlphaBlend = true;
//	@MLProp(info="true: Will be hostile, false: Is a pacifist")
	public static boolean cfg_Aggressive = true;
	public static String cfg_IgnoreItemList = "arsmagica2";

	public static Achievement ac_Contract;
	
	@SidedProxy(
			clientSide = "littleMaidMobX.LMM_ProxyClient",
			serverSide = "littleMaidMobX.LMM_ProxyCommon")
	public static LMM_ProxyCommon proxy;

	@Instance(DOMAIN)
	public static LMM_LittleMaidMobX instance;
	
	public static LMM_ItemSpawnEgg spawnEgg;

	public static void Debug(String pText, Object... pVals) {
		// デバッグメッセージ
		if (cfg_PrintDebugMessage) {
			System.out.println(String.format("littleMaidMob-" + pText, pVals));
		}
	}

	public String getName() {
		return "littleMaidMobX";
	}

	public String getPriorities() {
		// MMMLibを要求
		return "required-after:mod_MMM_MMMLib";
	}

	public String getVersion() {
		return "1.7.2-x";
	}

	@EventHandler
	public void PreInit(FMLPreInitializationEvent evt)
	{
		FileManager.setSrcPath(evt.getSourceFile());
		MMM_Config.init();
		
		// MMMLibのRevisionチェック
//		MMM_Helper.checkRevision("6");
		MMM_Config.checkConfig(this.getClass());
		
		cfg_defaultTexture = cfg_defaultTexture.trim();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new LMM_GuiCommonHandler());
		
		MMM_TextureManager.instance.init();

		EntityRegistry.registerModEntity(LMM_EntityLittleMaid.class, "LittleMaidX", 0, instance, 80, 3, true);

		/* langファイルに移動
		ModLoader.addLocalization("entity.LittleMaidX.name", "LittleMaidX");
		ModLoader.addLocalization("entity.LittleMaidX.name", "ja_JP", "リトルメイド");
		*/
		// アイテム自体は登録しておき、レシピを隠して無効化
		spawnEgg = new LMM_ItemSpawnEgg();
		spawnEgg.setUnlocalizedName(DOMAIN + ":spawn_lmmx_egg");
		spawnEgg.setTextureName(DOMAIN + ":spawn_lmmx_egg");
		GameRegistry.registerItem(spawnEgg, "spawn_lmmx_egg");
		if (cfg_enableSpawnEgg) {
			// 招喚用レシピを追加
			GameRegistry.addRecipe(new ItemStack(spawnEgg, 1), new Object[] {
				"scs",
				"sbs",
				" e ",
				Character.valueOf('s'), Items.sugar,
				Character.valueOf('c'), new ItemStack(Items.dye, 1, 3),
				Character.valueOf('b'), Items.slime_ball,
				Character.valueOf('e'), Items.egg,
			});
		}
		
		ac_Contract = new Achievement("achievement.contract", "contract", 0, 0, Items.cake, null).initIndependentStat().registerStat();
		Achievement[] achievements = new Achievement[] { ac_Contract };
		AchievementPage.registerAchievementPage(new AchievementPage("LittleMaidX", achievements));

		if (MMM_Helper.isClient) {
			// 名称変換テーブル
			/* langファイルに移動
			ModLoader.addLocalization("littleMaidMob.text.Health", "Health");
			ModLoader.addLocalization("littleMaidMob.text.Health", "ja_JP", "メイド強度");
			ModLoader.addLocalization("littleMaidMob.text.AP", "AP");
			ModLoader.addLocalization("littleMaidMob.text.AP", "ja_JP", "メイド装甲");
			ModLoader.addLocalization("littleMaidMob.text.STATUS", "Status");
			ModLoader.addLocalization("littleMaidMob.text.STATUS", "ja_JP", "メイド状態");
			*/
			
			// デフォルトモデルの設定
			proxy.init();
		}
		
		// AIリストの追加
		LMM_EntityModeManager.init();
		
		// アイテムスロット更新用のパケット
		W_Network.init(DOMAIN);

		// TODO ★ サウンドのロードを早くするテスト
		proxy.loadSounds();
		
//		Debug("GUID-sneak: %s", LMM_EntityLittleMaid.maidUUIDSneak.toString());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		proxy.postInit();
		// カンマ区切りのアイテム名のリストを配列にして設定
		// "aaa, bbb,ccc  " -> "aaa" "bbb" "ccc"
		ignoreItemList = cfg_IgnoreItemList.trim().split("\\s*,\\s*");
		
		MinecraftForge.EVENT_BUS.register(new LMM_EventHook());
		
		// デフォルトモデルの設定
		MMM_TextureManager.instance.setDefaultTexture(LMM_EntityLittleMaid.class, MMM_TextureManager.instance.getTextureBox("default_Orign"));
		
		// Dominant
		BiomeGenBase[] biomeList = null;
		if(cfg_spawnWeight > 0) {
			if (cfg_Dominant)
			{
				biomeList = BiomeGenBase.getBiomeGenArray();
			}
			else
			{
				// 通常スポーン設定バイオームは適当
				biomeList = new BiomeGenBase[]{
						BiomeGenBase.desert,
						BiomeGenBase.plains,
						BiomeGenBase.savanna,
						BiomeGenBase.mushroomIsland,
						BiomeGenBase.forest,
						BiomeGenBase.birchForest,
						BiomeGenBase.swampland,
						BiomeGenBase.taiga,
				};
			}
			for(BiomeGenBase biome : biomeList)
			{
				if(biome!=null)
				{
					EntityRegistry.addSpawn(LMM_EntityLittleMaid.class,
							cfg_spawnWeight, cfg_minGroupSize, cfg_maxGroupSize, EnumCreatureType.creature, biome);
				}
			}
		}
		
		// モードリストを構築
		LMM_EntityModeManager.loadEntityMode();
		LMM_EntityModeManager.showLoadedModes();
		
		// サウンドのロード
// TODO ★		proxy.loadSounds();
		
		// IFFのロード
		LMM_IFF.loadIFFs();
	}
	

	// 特定のMODのアイテムを持つとクラッシュする不具合対策====================================
	private static String ignoreItemList[] = new String[]{};

	public static boolean isMaidIgnoreItem(ItemStack item)
	{
		return item!=null && item.getItem()!=null && isMaidIgnoreItem(item.getItem());
	}
	public static boolean isMaidIgnoreItem(Item item)
	{
		if(item!=null)
		{
			String name = Item.itemRegistry.getNameForObject(item);
			for(String ignoreItemName : ignoreItemList)
			{
				if(name.indexOf(ignoreItemName) != -1)
				{
					return true;
				}
			}
		}
		return false;
	}
	// 特定のMODのアイテムを持つとクラッシュする不具合対策====================================
}
