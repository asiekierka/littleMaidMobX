package mmmlibx.lib.multiModel.model.mc162;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * マルチモデル用の基本クラス、これを継承していればマルチモデルとして使用できる。
 * Mincraftネイティブなクラスや継承関数などを排除して、難読化対策を行う。
 * 継承クラスではなくなったため、直接的な互換性はない。
 */
public abstract class ModelMultiBase extends ModelBase implements IModelCaps {

	public float heldItem[] = new float[] {0.0F, 0.0F};
	public boolean aimedBow;
	public boolean isSneak;
	public boolean isWait;
	
	public ModelRenderer mainFrame;
	public ModelRenderer HeadMount;
	public ModelRenderer HeadTop;
	public ModelRenderer Arms[];
	public ModelRenderer HardPoint[];
	
	public float entityIdFactor;
	public int entityTicksExisted;
	// 変数である意味ない？
	public float scaleFactor = 0.9375F;
	/**
	 * モデルが持っている機能群
	 */
	@SuppressWarnings("serial")
	private final Map<String, Integer> fcapsmap = new HashMap<String, Integer>() {{
		put("onGround",			caps_onGround);
		put("isRiding",			caps_isRiding);
		put("isSneak",			caps_isSneak);
		put("isWait",			caps_isWait);
		put("isChild",			caps_isChild);
		put("heldItemLeft",		caps_heldItemLeft);
		put("heldItemRight",	caps_heldItemRight);
		put("aimedBow",			caps_aimedBow);
		put("ScaleFactor", 		caps_ScaleFactor);
		put("entityIdFactor",	caps_entityIdFactor);
		put("dominantArm",	caps_dominantArm);
	}};



	public ModelMultiBase() {
		this(0.0F);
	}

	public ModelMultiBase(float pSizeAdjust) {
		this(pSizeAdjust, 0.0F, 64, 32);
	}

	public ModelMultiBase(float pSizeAdjust, float pYOffset, int pTextureWidth, int pTextureHeight) {
		isSneak = false;
		aimedBow = false;
		textureWidth = pTextureWidth;
		textureHeight = pTextureHeight;
		
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
//			MMMLib.Debug("ModelMulti.InitClient");
			// ハードポイント
			Arms = new ModelRenderer[2];
			HeadMount = new ModelRenderer(this, "HeadMount");
			HeadTop = new ModelRenderer(this, "HeadTop");
			
			initModel(pSizeAdjust, pYOffset);
		}
	}

	// 独自定義関数群

	/**
	 * モデルの初期化コード
	 */
	public abstract void initModel(float psize, float pyoffset);

	/**
	 * モデル指定詞に依らずに使用するテクスチャパック名。
	 * 一つのテクスチャに複数のモデルを割り当てる時に使う。
	 * @return
	 */
	public String getUsingTexture() {
		return null;
	}

	/**
	 *  身長
	 */
	@Deprecated
	public abstract float getHeight();
	/**
	 *  身長
	 */
	public float getHeight(IModelCaps pEntityCaps) {
		return getHeight();
	}
	/**
	 * 横幅
	 */
	@Deprecated
	public abstract float getWidth();
	/**
	 * 横幅
	 */
	public float getWidth(IModelCaps pEntityCaps) {
		return getWidth();
	}
	/**
	 * モデルのYオフセット
	 */
	@Deprecated
	public abstract float getyOffset();
	/**
	 * モデルのYオフセット
	 */
	public float getyOffset(IModelCaps pEntityCaps) {
		return getyOffset();
	}
	/**
	 * 上に乗せる時のオフセット高
	 */
	@Deprecated
	public abstract float getMountedYOffset();
	/**
	 * 上に乗せる時のオフセット高
	 */
	public float getMountedYOffset(IModelCaps pEntityCaps) {
		return getMountedYOffset();
	}

	/**
	 * ロープの取り付け位置調整用
	 * @return
	 */
	public float getLeashOffset(IModelCaps pEntityCaps) {
		return 0.4F;
	}

	/**
	 * アイテムを持っているときに手を前に出すかどうか。
	 */
	@Deprecated
	public boolean isItemHolder() {
		return false;
	}
	/**
	 * アイテムを持っているときに手を前に出すかどうか。
	 */
	public boolean isItemHolder(IModelCaps pEntityCaps) {
		return isItemHolder();
	}

	/**
	 * 表示すべきすべての部品
	 */
	@Deprecated
	public void showAllParts() {
	}
	/**
	 * 表示すべきすべての部品
	 */
	public void showAllParts(IModelCaps pEntityCaps) {
		showAllParts();
	}

	/**
	 * 部位ごとの装甲表示。
	 * @param parts
	 * 3:頭部。
	 * 2:胴部。
	 * 1:脚部
	 * 0:足部
	 * @param index
	 * 0:inner
	 * 1:outer
	 * @return
	 * 戻り値は基本 -1
	 */
	public int showArmorParts(int parts, int index) {
		return -1;
	}

	/**
	 * ハードポイントに接続されたアイテムを表示する
	 */
	public abstract void renderItems(IModelCaps pEntityCaps);

	public abstract void renderFirstPersonHand(IModelCaps pEntityCaps);



	// IModelCaps

	@Override
	public Map<String, Integer> getModelCaps() {
		return fcapsmap;
	}

	@Override
	public Object getCapsValue(int pIndex, Object ...pArg) {
		switch (pIndex) {
		case caps_onGround:
			return onGrounds;
		case caps_isRiding:
			return isRiding;
		case caps_isSneak:
			return isSneak;
		case caps_isWait:
			return isWait;
		case caps_isChild:
			return isChild;
		case caps_heldItemLeft:
			return heldItem[1];
		case caps_heldItemRight:
			return heldItem[0];
		case caps_aimedBow:
			return aimedBow;
		case caps_entityIdFactor:
			return entityIdFactor;
		case caps_ticksExisted:
			return entityTicksExisted;
		case caps_ScaleFactor:
			return scaleFactor;
		case caps_dominantArm:
			return dominantArm;
		}
		return null;
	}

	@Override
	public boolean setCapsValue(int pIndex, Object... pArg) {
		switch (pIndex) {
		case caps_onGround:
			for (int li = 0; li < onGrounds.length && li < pArg.length; li++) {
				onGrounds[li] = (Float)pArg[li];
			}
			return true;
		case caps_isRiding:
			isRiding = (Boolean)pArg[0];
			return true;
		case caps_isSneak:
			isSneak = (Boolean)pArg[0];
			return true;
		case caps_isWait:
			isWait = (Boolean)pArg[0];
			return true;
		case caps_isChild:
			isChild = (Boolean)pArg[0];
			return true;
		case caps_heldItemLeft:
			heldItem[1] = (Integer)pArg[0];
			return true;
		case caps_heldItemRight:
			heldItem[0] = (Integer)pArg[0];
			return true;
		case caps_aimedBow:
			aimedBow = (Boolean)pArg[0];
			return true;
		case caps_entityIdFactor:
			entityIdFactor = (Float)pArg[0];
			return true;
		case caps_ticksExisted:
			entityTicksExisted = (Integer)pArg[0];
			return true;
		case caps_ScaleFactor:
			scaleFactor = (Float)pArg[0];
			return true;
		case caps_dominantArm:
			dominantArm = (Integer)pArg[0];
			return true;
		}
		
		return false;
	}

}
