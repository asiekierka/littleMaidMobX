package mmmlibx.lib;

import java.io.File;
import java.util.List;

import mmmlibx.lib.guns.GunsBase;
import mmmlibx.lib.multiModel.MMMLoader.MMMTransformer;
import mmmlibx.lib.multiModel.texture.MultiModelManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import network.W_Message;
import network.W_Network;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;

@Mod(	modid	= "MMMLibX",
		name	= "MMMLibX",
		version	= "1.7.x-srg-1")
public class MMMLib {

	public static boolean cfg_isModelAlphaBlend = true;
/**	public static final int cfg_startVehicleEntityID = 0;	Forgeには不要	*/
	public static boolean isDebugMessage = true;
	public static boolean isModelAlphaBlend = true;


	@SidedProxy(
			clientSide = "mmmlibx.lib.MMM_ProxyClient",
			serverSide = "mmmlibx.lib.MMM_ProxyCommon")
	public static MMM_ProxyCommon proxy;

	public static void Debug(String pText, Object... pData) {
		// デバッグメッセージ
		if (isDebugMessage) {
			System.out.println(String.format("MMMLib-" + pText, pData));
		}
	}
	public static void Debug(boolean isRemote, String pText, Object... pData) {
		// デバッグメッセージ
		if (isDebugMessage) {
			System.out.println(String.format("["+(isRemote? "Client":"Server")+"]MMMLib-" + pText, pData));
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent pEvent) {

		// MMMLibが立ち上がった時点で旧モデル置き換えを開始
		MMMTransformer.isEnable = true;
		
		// コンフィグの解析・設定
		File configFile = pEvent.getSuggestedConfigurationFile();
		Configuration lconf = new Configuration(configFile);
		lconf.load();
		isDebugMessage		= lconf.get("MMMLib", "isDebugMessage", false).getBoolean(false);
		isModelAlphaBlend	= lconf.get("MMMLib", "isModelAlphaBlend", true).getBoolean(true);
		cfg_isModelAlphaBlend = isModelAlphaBlend;
		
		String ls;
/* TODO ★
		ls = "DestroyAll";
		lconf.addCustomCategoryComment(ls, "Package destruction of the fixed range is carried out.");
		DestroyAllManager.isDebugMessage = lconf.get(ls, "isDebugMessage", false).getBoolean(false);
		
		ls = "GunsBase";
		lconf.addCustomCategoryComment(ls, "Basic processing of a firearm.");
		GunsBase.isDebugMessage = lconf.get(ls, "isDebugMessage", false).getBoolean(false);
		
		ls = "MoveScreen";
		lconf.addCustomCategoryComment(ls, "The position of a window is automatically moved to a start-up.");
 		MoveWindow.isMoveWindow	= lconf.get(ls, "isMoveWindow", false).getBoolean(false);
		MoveWindow.windowPosX	= lconf.get(ls, "windowPosX", 20).getInt(20);
		MoveWindow.windowPosY	= lconf.get(ls, "windowPosY", 50).getInt(50);
		
		ls = "EzRecipes";
		lconf.addCustomCategoryComment(ls, "Append Recipes from JSON.");
		EzRecipes.isDebugMessage = lconf.get(ls, "isDebugMessage", false).getBoolean(false);
*/
		lconf.save();
		
		// 独自スクリプトデコーダー
// TODO ★		(new MMMDecorder()).execute();

		MMM_StabilizerManager.init();

		// テクスチャパックの構築
		MMM_TextureManager.instance.loadTextures();
		// ロード
		if (MMM_Helper.isClient) {
			// テクスチャパックの構築
//			MMM_TextureManager.loadTextures();
			MMM_StabilizerManager.loadStabilizer();
			// テクスチャインデックスの構築
			Debug("Localmode: InitTextureList.");
			MMM_TextureManager.instance.initTextureList(true);
		} else {
			MMM_TextureManager.instance.loadTextureServer();
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent pEvent) {
		if (pEvent.getSide() == Side.CLIENT) {
// TODO ★			MoveWindow.setPosition();
		}
	}

	@Mod.EventHandler
	public void loaded(FMLPostInitializationEvent pEvent) {
		// 独自スクリプトデコーダー
//		EzRecipes.init();
		// 
		GunsBase.initAppend();
		
		// 旧モデル用変換開始
		MMMTransformer.isEnable = true;
		MultiModelManager.instance.execute();
		
		// TODO test
		List<File> llist = FileManager.getAllmodsFiles(getClass().getClassLoader(), true);
		for (File lf : llist) {
			Debug("targetFiles: %s", lf.getAbsolutePath());
		}
		
		
		try {
			Class<?> lc = ReflectionHelper.getClass(getClass().getClassLoader(), "net.minecraft.entity.EntityLivingBase");
			Debug("test-getClass: %s", lc.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void serverCustomPayload(EntityPlayer playerEntity, W_Message var2)
	{
		// サーバ側の動作
		byte lmode = var2.data[0];
		int leid = 0;
		Entity lentity = null;
		if ((lmode & 0x80) != 0) {
			leid = MMM_Helper.getInt(var2.data, 1);
			lentity = MMM_Helper.getEntity(var2.data, 1, playerEntity.worldObj);
			if (lentity == null) return;
		}
		Debug("MMM|Upd Srv Call[%2x:%d].", lmode, leid);
//		byte[] ldata;
		
		switch (lmode) {
		case MMM_Statics.Server_SetTexturePackIndex:
			// サーバー側のEntityに対してテクスチャインデックスを設定する
			MMM_TextureManager.instance.reciveFromClientSetTexturePackIndex(lentity, var2.data);
			break;
		case MMM_Statics.Server_GetTextureIndex:
			// サーバー側での管理番号の問い合わせに対して応答する
			MMM_TextureManager.instance.reciveFromClientGetTexturePackIndex(playerEntity, var2.data);
			break;
		case MMM_Statics.Server_GetTexturePackName:
			// 管理番号に対応するテクスチャパック名を返す。
			MMM_TextureManager.instance.reciveFromClientGetTexturePackName(playerEntity, var2.data);
			break;
		}
	}

	public static void sendToClient(EntityPlayer player, byte[] ldata)
	{
		W_Network.sendPacketToPlayer(1, player, ldata);
	}
}
