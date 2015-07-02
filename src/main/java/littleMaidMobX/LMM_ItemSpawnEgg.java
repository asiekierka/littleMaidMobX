package littleMaidMobX;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LMM_ItemSpawnEgg extends Item
{
	public LMM_ItemSpawnEgg()
	{
		this.setHasSubtypes(true);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
	{
		if (par3World.isRemote)
		{
			return true;
		}
		else
		{
			Block block = par3World.getBlock(par4, par5, par6);
			par4 += Facing.offsetsXForSide[par7];
			par5 += Facing.offsetsYForSide[par7];
			par6 += Facing.offsetsZForSide[par7];
			double d0 = 0.0D;

			if (par7 == 1 && block.getRenderType() == 11)
			{
				d0 = 0.5D;
			}

			Entity entity = spawnMaid(par3World, par1ItemStack.getItemDamage(), (double)par4 + 0.5D, (double)par5 + d0, (double)par6 + 0.5D);

			if (entity != null)
			{
				if (entity instanceof EntityLivingBase && par1ItemStack.hasDisplayName())
				{
					((EntityLiving)entity).setCustomNameTag(par1ItemStack.getDisplayName());
				}

				if (!par2EntityPlayer.capabilities.isCreativeMode)
				{
					--par1ItemStack.stackSize;
				}
			}

			return true;
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		if (par2World.isRemote)
		{
			return par1ItemStack;
		}
		else
		{
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(par2World, par3EntityPlayer, true);

			if (movingobjectposition == null)
			{
				return par1ItemStack;
			}
			else
			{
				if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
				{
					int i = movingobjectposition.blockX;
					int j = movingobjectposition.blockY;
					int k = movingobjectposition.blockZ;

					if (!par2World.canMineBlock(par3EntityPlayer, i, j, k))
					{
						return par1ItemStack;
					}

					if (!par3EntityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, par1ItemStack))
					{
						return par1ItemStack;
					}

					if (par2World.getBlock(i, j, k) instanceof BlockLiquid)
					{
						Entity entity = spawnMaid(par2World, par1ItemStack.getItemDamage(), (double)i, (double)j, (double)k);

						if (entity != null)
						{
							if (entity instanceof EntityLivingBase && par1ItemStack.hasDisplayName())
							{
								((EntityLiving)entity).setCustomNameTag(par1ItemStack.getDisplayName());
							}

							if (!par3EntityPlayer.capabilities.isCreativeMode)
							{
								--par1ItemStack.stackSize;
							}
						}
					}
				}

				return par1ItemStack;
			}
		}
	}

	public static Entity spawnMaid(World par0World, int par1, double par2, double par4, double par6)
	{
		EntityLiving entity = null;
		try {
			entity = new LMM_EntityLittleMaid(par0World);
			entity.setLocationAndAngles(par2, par4, par6, MathHelper.wrapAngleTo180_float(par0World.rand.nextFloat() * 360.0F), 0.0F);
			entity.rotationYawHead = entity.rotationYaw;
			entity.renderYawOffset = entity.rotationYaw;
			entity.onSpawnWithEgg(null);
			par0World.spawnEntityInWorld(entity);
			entity.playLivingSound();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entity;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item par1, CreativeTabs par2, List par3)
	{
		par3.add(new ItemStack(par1, 1));
	}
}
