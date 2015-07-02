package littleMaidMobX;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

public class LMM_EntityAIAvoidPlayer extends EntityAIBase implements
		LMM_IEntityAI {

	/** The entity we are attached to */
	protected LMM_EntityLittleMaid theMaid;
	protected EntityPlayer theMaster;
	protected float speedNormal;
	protected PathEntity avoidPath;
	/** The PathNavigate of our entity */
	protected PathNavigate entityPathNavigate;
	protected boolean isEnable;

	public boolean isActive;
	public int minDist;

	public LMM_EntityAIAvoidPlayer(LMM_EntityLittleMaid pEntityLittleMaid,
			float pSpeed, int pMinDist) {
		theMaid = pEntityLittleMaid;
		speedNormal = pSpeed;
		entityPathNavigate = pEntityLittleMaid.getNavigator();
		isActive = false;
		isEnable = false;
		minDist = pMinDist;
		setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (!isEnable || !isActive || !theMaid.isContract()) {
			isActive = false;
			return false;
		}

		theMaster = theMaid.mstatMasterEntity;

		// 不具合対策：プレイヤーがログアウトすると theMaster がNULLになって以降の処理でクラッシュ
		// http://forum.minecraftuser.jp/viewtopic.php?f=13&t=23347&start=180#p211806
		if(theMaster==null)
		{
			return false;
		}

		// 対象は見えるか？てかこれいらなくね？
		if (!theMaid.getEntitySenses().canSee(theMaster)) {
			return false;
		}

		// 移動先を策定
		Vec3 vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
				theMaid, minDist, 7, Vec3.createVectorHelper(theMaster.posX,
						theMaster.posY, theMaster.posZ));

		// 移動先が無い
		if (vec3d == null) {
			return false;
		}
		// 移動先の距離が近い
		if (theMaster.getDistanceSq(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord) < theMaid.mstatMasterDistanceSq) {
			return false;
		}
		
		avoidPath = entityPathNavigate.getPathToXYZ(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord);
		
		if (avoidPath == null) {
			return false;
		}

		return avoidPath.isDestinationSame(vec3d);
	}

	@Override
	public boolean continueExecuting() {
		if(theMaster==null) return false;
		return !entityPathNavigate.noPath() && theMaid.getDistanceSqToEntity(theMaster) < 144D;
	}

	@Override
	public void startExecuting() {
		entityPathNavigate.setPath(avoidPath, speedNormal);
	}

	@Override
	public void resetTask() {
		isActive = false;
	}

	public void setActive() {
		// 動作開始
		isActive = true;
	}

	// 実行可能フラグ
	@Override
	public void setEnable(boolean pFlag) {
		isEnable = pFlag;
	}

	@Override
	public boolean getEnable() {
		return isEnable;
	}

}
