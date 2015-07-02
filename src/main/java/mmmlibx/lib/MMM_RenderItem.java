package mmmlibx.lib;

import java.util.Random;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;

public class MMM_RenderItem extends RenderItem {

	private Random random;


	public MMM_RenderItem() {
		super();
		random = new Random();
	}

	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		if (entity instanceof EntityItem) {
			EntityItem ei = (EntityItem)entity;
			Item litem = ei.getEntityItem().getItem();
			if (MMM_ItemRenderManager.isEXRender(litem)) {
				if (MMM_ItemRenderManager.getEXRender(litem).renderItemWorld(ei, d, d1, d2, f, f1)) {
					return;
				}
			}
		}
		
		super.doRender(entity, d, d1, d2, f, f1);
	}

	/*
	@Override
	public void renderItemIntoGUI(FontRenderer pFontrenderer, RenderEngine pRenderengine, ItemStack pItemStack, int pX, int pY) {
		try {
			Method lmethod;
			lmethod = pItemStack.getItem().getClass().getMethod("renderItemIntoGUI", FontRenderer.class, RenderEngine.class, int.class, int.class, int.class, int.class, int.class);
			if ((Boolean)lmethod.invoke(null, pFontrenderer, pRenderengine, pItemStack, pX, pY)) {
				return;
			}
		} catch (Exception e) {
		}
		super.renderItemIntoGUI(pFontrenderer, pRenderengine, pItemStack, pX, pY);
	}
*/
}
