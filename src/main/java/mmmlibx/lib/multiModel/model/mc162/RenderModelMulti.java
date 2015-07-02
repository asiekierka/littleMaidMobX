package mmmlibx.lib.multiModel.model.mc162;

import mmmlibx.lib.Client;
import mmmlibx.lib.ITextureEntity;
import mmmlibx.lib.MMMLib;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderModelMulti extends RenderLiving {

	public ModelBaseSolo modelMain;
	public ModelBaseDuo modelFATT;
	public IModelCaps fcaps;



	public RenderModelMulti(float pShadowSize) {
		super(null, pShadowSize);
		modelFATT = new ModelBaseDuo(this);
		modelFATT.isModelAlphablend = MMMLib.cfg_isModelAlphaBlend;
		modelFATT.isRendering = true;
		modelMain = new ModelBaseSolo(this);
		modelMain.isModelAlphablend = MMMLib.cfg_isModelAlphaBlend;
		modelMain.capsLink = modelFATT;
		mainModel = modelMain;
		setRenderPassModel(modelFATT);
	}

	protected int showArmorParts(EntityLivingBase par1EntityLiving, int par2, float par3) {
		// アーマーの表示設定
		modelFATT.renderParts = par2;
		modelFATT.renderCount = 0;
		ItemStack is = par1EntityLiving.getEquipmentInSlot(par2 + 1);
		if (is != null && is.stackSize > 0) {
			modelFATT.showArmorParts(par2);
			return is.isItemEnchanted() ? 15 : 1;
		}
		
		return -1;
	}
	@Override
	protected int shouldRenderPass(EntityLivingBase par1EntityLiving, int par2, float par3) {
		return showArmorParts((EntityLivingBase)par1EntityLiving, par2, par3);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityliving, float f) {
		Float lscale = (Float)modelMain.getCapsValue(IModelCaps.caps_ScaleFactor);
		if (lscale != null) {
			GL11.glScalef(lscale, lscale, lscale);
		}
	}

	public void setModelValues(EntityLivingBase par1EntityLiving, double par2,
			double par4, double par6, float par8, float par9, IModelCaps pEntityCaps) {
		if (par1EntityLiving instanceof ITextureEntity) {
			ITextureEntity ltentity = (ITextureEntity)par1EntityLiving;
			modelMain.model = ltentity.getTextureData().textureModel[0];
			modelFATT.modelInner = ltentity.getTextureData().textureModel[1];
			modelFATT.modelOuter = ltentity.getTextureData().textureModel[2];
//			modelMain.model = ((TextureBox)ltentity.getTextureBox()[0]).models[0];
			modelMain.textures = ltentity.getTextures(0);
//			modelFATT.modelInner = ((TextureBox)ltentity.getTextureBox()[1]).models[1];
//			modelFATT.modelOuter = ((TextureBox)ltentity.getTextureBox()[1]).models[2];
			modelFATT.textureInner = ltentity.getTextures(1);
			modelFATT.textureOuter = ltentity.getTextures(2);
			modelFATT.textureInnerLight = ltentity.getTextures(3);
			modelFATT.textureOuterLight = ltentity.getTextures(4);
			modelFATT.textureLightColor = (float[])modelFATT.getCapsValue(IModelCaps.caps_textureLightColor, pEntityCaps);
		}
		modelMain.setEntityCaps(pEntityCaps);
		modelFATT.setEntityCaps(pEntityCaps);
		modelMain.setRender(this);
		modelFATT.setRender(this);
		modelMain.showAllParts();
		modelFATT.showAllParts();
		modelMain.isAlphablend = true;
		modelFATT.isAlphablend = true;
		modelMain.renderCount = 0;
		modelFATT.renderCount = 0;
		modelMain.lighting = modelFATT.lighting = par1EntityLiving.getBrightnessForRender(par8);
		
		modelMain.setCapsValue(IModelCaps.caps_heldItemLeft, (Integer)0);
		modelMain.setCapsValue(IModelCaps.caps_heldItemRight, (Integer)0);
		modelMain.setCapsValue(IModelCaps.caps_onGround, renderSwingProgress(par1EntityLiving, par9));
		modelMain.setCapsValue(IModelCaps.caps_isRiding, par1EntityLiving.isRiding());
		modelMain.setCapsValue(IModelCaps.caps_isSneak, par1EntityLiving.isSneaking());
		modelMain.setCapsValue(IModelCaps.caps_aimedBow, false);
		modelMain.setCapsValue(IModelCaps.caps_isWait, false);
		modelMain.setCapsValue(IModelCaps.caps_isChild, par1EntityLiving.isChild());
		modelMain.setCapsValue(IModelCaps.caps_entityIdFactor, 0F);
		modelMain.setCapsValue(IModelCaps.caps_ticksExisted, par1EntityLiving.ticksExisted);
	}

//	public void renderModelMulti(EntityLivingBase par1EntityLiving, double par2,
	public void renderModelMulti(EntityLiving par1EntityLiving, double par2,
			double par4, double par6, float par8, float par9, IModelCaps pEntityCaps) {
		setModelValues(par1EntityLiving, par2, par4, par6, par8, par9, pEntityCaps);
		// TODO:1.6.2-MCP805 なぜか変なとこに飛んでループする
//		super.func_130000_a(par1EntityLiving, par2, par4, par6, par8, par9);
		super.doRender(par1EntityLiving, par2, par4, par6, par8, par9);
	}

	@Override
	public void doRender(EntityLiving par1EntityLiving, double par2,
			double par4, double par6, float par8, float par9) {
		fcaps = (IModelCaps)par1EntityLiving;
		renderModelMulti(par1EntityLiving, par2, par4, par6, par8, par9, fcaps);
	}

	@Override
	protected void func_110827_b(EntityLiving par1EntityLiving, double par2,
			double par4, double par6, float par8, float par9) {
		// 縄の位置のオフセット
		// TODO：MCP-804対策
		float lf = 0F;
		if (modelMain.model != null && fcaps != null) {
			lf = modelMain.model.getLeashOffset(fcaps);
		}
		super.func_110827_b(par1EntityLiving, par2, par4 - lf, par6, par8, par9);
	}

	@Override
	protected void renderModel(EntityLivingBase par1EntityLiving, float par2,
			float par3, float par4, float par5, float par6, float par7) {
		if (!par1EntityLiving.isInvisible()) {
			modelMain.setArmorRendering(true);
		} else {
			modelMain.setArmorRendering(false);
		}
		// アイテムのレンダリング位置を獲得するためrenderを呼ぶ必要がある
		mainModel.render(par1EntityLiving, par2, par3, par4, par5, par6, par7);
	}

	@Override
	protected void renderEquippedItems(EntityLivingBase par1EntityLiving, float par2) {
		// ハードポイントの描画
		modelMain.renderItems(par1EntityLiving, this);
		renderArrowsStuckInEntity(par1EntityLiving, par2);
	}

	@Override
	protected void renderArrowsStuckInEntity(EntityLivingBase par1EntityLiving, float par2) {
		Client.renderArrowsStuckInEntity(par1EntityLiving, par2, this, modelMain.model);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity var1) {
		// テクスチャリソースを返すところだけれど、基本的に使用しない。
		return null;
	}

	@Override
	protected int getColorMultiplier(EntityLivingBase par1EntityLivingBase,
			float par2, float par3) {
		modelMain.renderCount = 16;
		return super.getColorMultiplier(par1EntityLivingBase, par2, par3);
	}

	@Override
	protected int inheritRenderPass(EntityLivingBase par1EntityLivingBase,
			int par2, float par3) {
		int li = super.inheritRenderPass(par1EntityLivingBase, par2, par3);
		modelFATT.renderCount = 16;
		return li;
	}

}
