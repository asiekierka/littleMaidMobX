package mmmlibx.lib;

import mmmlibx.lib.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.EXTRescaleNormal;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class MMM_GuiTextureSelect extends GuiScreen {

	private String screenTitle = "Texture Select";
	protected GuiScreen owner;
	protected MMM_GuiTextureSlot selectPanel;
	protected GuiButton modeButton[] = new GuiButton[2];
	public ITextureEntity target;
	public int canSelectColor;
	public int selectColor;
	protected boolean toServer;


	public MMM_GuiTextureSelect(GuiScreen pOwner, ITextureEntity pTarget, int pColor, boolean pToServer) {
		owner = pOwner;
		target = pTarget;
		canSelectColor = pColor;
		selectColor = pTarget.getColor();
		toServer = pToServer;
	}

	@Override
	public void initGui() {
		selectPanel = new MMM_GuiTextureSlot(this);
		selectPanel.registerScrollButtons(3, 4);
		buttonList.add(modeButton[0] = new GuiButton(100, width / 2 - 55, height - 55, 80, 20, "Texture"));
		buttonList.add(modeButton[1] = new GuiButton(101, width / 2 + 30, height - 55, 80, 20, "Armor"));
		buttonList.add(new GuiButton(200, width / 2 - 10, height - 30, 120, 20, "Select"));
		modeButton[0].enabled = false;
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1) {
			mc.displayGuiScreen(owner);
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		selectPanel.drawScreen(par1, par2, par3);
		drawCenteredString(mc.fontRenderer, StatCollector.translateToLocal(screenTitle), width / 2, 4, 0xffffff);
		
		super.drawScreen(par1, par2, par3);
		
		GL11.glPushMatrix();
		GL11.glEnable(EXTRescaleNormal.GL_RESCALE_NORMAL_EXT);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		MMM_TextureBox lbox = selectPanel.getSelectedBox();
		GL11.glTranslatef(width / 2 - 115F, height - 5F, 100F);
		GL11.glScalef(60F, -60F, 60F);
		selectPanel.entity.renderYawOffset = -25F;
		selectPanel.entity.rotationYawHead = -10F;
		ResourceLocation ltex[];
		if (selectPanel.mode) {
			selectPanel.entity.textureData.textureBox[0] = selectPanel.blankBox;
			selectPanel.entity.textureData.textureBox[1] = lbox;
			selectPanel.entity.setTextureNames("default");
		} else {
			selectPanel.entity.textureData.textureBox[0] = lbox;
			selectPanel.entity.textureData.textureBox[1] = selectPanel.blankBox;
			selectPanel.entity.setColor(selectColor);
			selectPanel.entity.setTextureNames();
		}
		RenderManager.instance.renderEntityWithPosYaw(selectPanel.entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
		for (int li = 0; li < 16; li++) {
			if (lbox.hasColor(li)) {
				break;
			}
		}
		GL11.glDisable(EXTRescaleNormal.GL_RESCALE_NORMAL_EXT);
		GL11.glPopMatrix();

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		switch (par1GuiButton.id) {
		case 100:
			modeButton[0].enabled = false;
			modeButton[1].enabled = true;
			selectPanel.setMode(false);
			break;
		case 101:
			modeButton[0].enabled = true;
			modeButton[1].enabled = false;
			selectPanel.setMode(true);
			break;
		case 200:
			boolean lflag = false;
			target.setColor(selectColor);
			if (selectPanel.texsel[0] > -1) {
				target.getTextureBox()[0] = selectPanel.getSelectedBox(false);
			}
			if (selectPanel.texsel[1] > -1) {
				target.getTextureBox()[1] = selectPanel.getSelectedBox(true);
			}
			target.getTextureData().setTextureNames();
			if (toServer) {
				MMM_TextureManager.instance.postSetTexturePack(target, selectColor, target.getTextureBox());
			} else {
				MMM_TextureBox lboxs[] = new MMM_TextureBox[2];
				lboxs[0] = (MMM_TextureBox)target.getTextureBox()[0];
				lboxs[1] = (MMM_TextureBox)target.getTextureBox()[1];
				target.setTexturePackName(lboxs);
			}
			System.out.println(String.format("select: %d(%d/%s), %d(%d/%s)",
					selectPanel.texsel[0], target.getTextureIndex()[0], target.getTextureBox()[0].textureName,
					selectPanel.texsel[1], target.getTextureIndex()[1], target.getTextureBox()[1].textureName));
			mc.displayGuiScreen(owner);
			break;
		}
	}

}
