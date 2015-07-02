package littleMaidMobX;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LMM_EntityAIBeg extends EntityAIBase {

	protected LMM_EntityLittleMaid littleMaid;
	protected EntityPlayer targetPlayer;
	protected double targetRangeSq;
	protected World worldObj;
	protected float effectiveRange;
	protected double effectiveRangeSq;
	protected int field_48347_e; // 興味を持っている時間？

	public LMM_EntityAIBeg(LMM_EntityLittleMaid pentityLittlemaid, float par2) {
		littleMaid = pentityLittlemaid;
		worldObj = pentityLittlemaid.worldObj;
		effectiveRange = par2;
		effectiveRangeSq = (double) par2 * (double) par2;
		setMutexBits(2);
	}

	@Override
	public boolean shouldExecute() {
		if (littleMaid.isContract()) {
			targetPlayer = littleMaid.mstatMasterDistanceSq > effectiveRangeSq ? null
					: littleMaid.mstatMasterEntity;
		} else {
			targetPlayer = worldObj.getClosestPlayerToEntity(littleMaid, effectiveRange);
		}
		
		if (targetPlayer == null) {
			return false;
		} else {
			return checkItem(targetPlayer);
		}
	}

	@Override
	public boolean continueExecuting() {
		// if (!targetPlayer.isEntityAlive()) {
		if (targetPlayer == null || !targetPlayer.isEntityAlive()) {
			return false;
		}
		
		if (littleMaid.isContract()) {
			targetRangeSq = littleMaid.mstatMasterDistanceSq;
		} else {
			targetRangeSq = littleMaid.getDistanceSqToEntity(targetPlayer);
		}
		
		if (targetRangeSq > effectiveRangeSq) {
			return false;
		} else {
			return field_48347_e > 0 && checkItem(targetPlayer);
		}
	}

	@Override
	public void startExecuting() {
		// littleMaid.setLooksWithInterest(true);
		field_48347_e = 40 + littleMaid.getRNG().nextInt(40);
		littleMaid.setLookSuger(true);
	}

	@Override
	public void resetTask() {
		littleMaid.setLooksWithInterest(false);
		// littleMaid.numTicksToChaseTarget = 0;
		littleMaid.setLookSuger(false);
	}

	@Override
	public void updateTask() {
		// 目で殺す
		littleMaid.getLookHelper().setLookPositionWithEntity(targetPlayer, 10F,
				littleMaid.getVerticalFaceSpeed());
		if (littleMaid.getNavigator().noPath()) {
			littleMaid.setLooksWithInterest(true);
		} else {
			littleMaid.setLooksWithInterest(false);
		}
		
//		field_48347_e--;
	}

	private boolean checkItem(EntityPlayer par1EntityPlayer) {
		ItemStack itemstack = par1EntityPlayer.inventory.getCurrentItem();
		
		if (itemstack == null) {
			return false;
		}
		return littleMaid.isBreedingItem(itemstack);
	}

	public EntityPlayer getPlayer() {
		return targetPlayer;
	}

	public double getDistanceSq() {
		return targetRangeSq;
	}

}
