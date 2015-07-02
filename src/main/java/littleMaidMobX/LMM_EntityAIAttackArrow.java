package littleMaidMobX;

import java.lang.reflect.Field;
import java.util.List;

import mmmlibx.lib.MMM_Helper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LMM_EntityAIAttackArrow extends EntityAIBase implements LMM_IEntityAI {

	protected boolean fEnable;
	
	protected LMM_EntityLittleMaid fMaid;
	protected EntityPlayer fAvatar;
	protected LMM_InventoryLittleMaid fInventory;
	protected LMM_SwingStatus swingState;
	protected World worldObj;
	protected EntityLivingBase fTarget;
	protected int fForget;
	/** ターゲットの体力が同じ間カウントアップする。メイドの位置が悪くダメージを与えられない場合に移動させるためのカウンタ  */
	protected int fTargetDamegeCounter;
	/** ターゲットの体力 */
	protected float fTargetHealth;
	/** true=右回り、false=左回り */
	protected boolean fTargetSearchDir;

	
	public LMM_EntityAIAttackArrow(LMM_EntityLittleMaid pEntityLittleMaid) {
		fMaid = pEntityLittleMaid;
		fAvatar = pEntityLittleMaid.maidAvatar;
		fInventory = pEntityLittleMaid.maidInventory;
		swingState = pEntityLittleMaid.getSwingStatusDominant();
		worldObj = pEntityLittleMaid.worldObj;
		fEnable = false;
		fTargetDamegeCounter = 0;
		fTargetHealth = 0;
		fTargetSearchDir = fMaid.getRNG().nextBoolean();
		setMutexBits(3);
	}
	
	public LMM_IEntityLittleMaidAvatarBase getAvatarIF()
	{
		return (LMM_IEntityLittleMaidAvatarBase)fAvatar;
	}
	
	@Override
	public boolean shouldExecute() {
		EntityLivingBase entityliving = fMaid.getAttackTarget();
		
		if (!fEnable || entityliving == null || entityliving.isDead) {
			fMaid.setAttackTarget(null);
			fMaid.setTarget(null);
			if (entityliving != null) {
				fMaid.getNavigator().clearPathEntity();
			}
			fTarget = null;
			return false;
		} else {
			fTarget = entityliving;
			return true;
		}
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		fMaid.playSound(fMaid.isBloodsuck() ? LMM_EnumSound.findTarget_B : LMM_EnumSound.findTarget_N, false);
		swingState = fMaid.getSwingStatusDominant();
	}

	@Override
	public boolean continueExecuting() {
		return shouldExecute() || (fTarget != null && !fMaid.getNavigator().noPath());
	}

	@Override
	public void resetTask() {
		fTarget = null;
	}

	@Override
	public void updateTask() {

		double backupPosX = fMaid.posX;
		double backupPosZ = fMaid.posZ;
		
		// プレイヤーに乗っていると射線にプレイヤーが入り、撃てなくなるため僅かに目標エンティティに近づける
		// 関数を抜ける前に元に戻す必要があるので途中で return しないこと
		if(fMaid.ridingEntity instanceof EntityPlayer)
		{
			double dtx = fTarget.posX - fMaid.posX;
			double dtz = fTarget.posZ - fMaid.posZ;
			double distTarget = MathHelper.sqrt_double(dtx*dtx + dtz*dtz);
			fMaid.posX += dtx / distTarget * 1.0;	// 1m 目標に近づける
			fMaid.posZ += dtz / distTarget * 1.0;	// 1m 目標に近づける
		}
		
		double lrange = 225D;
		double ldist = fMaid.getDistanceSqToEntity(fTarget);
		boolean lsee = fMaid.getEntitySenses().canSee(fTarget);
		
		// 視界の外に出たら一定時間で飽きる
		if (lsee) {
			fForget = 0;
		} else {
			fForget++;
		}
		
		// 攻撃対象を見る
		fMaid.getLookHelper().setLookPositionWithEntity(fTarget, 30F, 30F);
		
		if (ldist < lrange) {
			// 有効射程内
			double atx = fTarget.posX - fMaid.posX;
			double aty = fTarget.posY - fMaid.posY;
			double atz = fTarget.posZ - fMaid.posZ;
			if (fTarget.isEntityAlive()) {
				
				// ターゲットのHPに変化がある場合、攻撃を継続
				if(fTarget.getHealth() != fTargetHealth)
				{
					fTargetHealth = fTarget.getHealth();
					fTargetDamegeCounter = 0;
				}
				// ターゲットのHPに変化が無い場合、9秒間カウントする。カウント開始時に移動方向を反転する
				else if(fTargetDamegeCounter < 9 * 20)
				{
//					System.out.println("##" + fTargetDamegeCounter/20 + " : " + (fTargetSearchDir?"R":"L"));
					fTargetDamegeCounter++;
					if(fTargetDamegeCounter == 1)
					{
						fTargetSearchDir = !fTargetSearchDir;
					}
				}
				else
				{
					fTargetDamegeCounter = 0;
				}
				
				ItemStack litemstack = fMaid.getCurrentEquippedItem();
				// 敵とのベクトル
				double atl = atx * atx + aty * aty + atz * atz;
				double il = -1D;
				double milsq = 10D;
				Entity masterEntity = fMaid.getMaidMasterEntity();
				if (masterEntity != null && !fMaid.isPlaying()) {
					// 主とのベクトル
					double amx = masterEntity.posX - fMaid.posX;
					double amy = masterEntity.posY - fMaid.posY;//-2D
					double amz = masterEntity.posZ - fMaid.posZ;
					
					// この値が０～１ならターゲットとの間に主がいる
					il = (amx * atx + amy * aty + amz * atz) / atl;
					
					// 射線ベクトルと主との垂直ベクトル
					double mix = (fMaid.posX + il * atx) - masterEntity.posX;
					double miy = (fMaid.posY + il * aty) - masterEntity.posY;// + 2D;
					double miz = (fMaid.posZ + il * atz) - masterEntity.posZ;
					// 射線から主との距離
					milsq = mix * mix + miy * miy + miz * miz;
//					mod_LMM_littleMaidMob.Debug("il:%f, milsq:%f", il, milsq);
				}
				
				if (litemstack != null && !(litemstack.getItem() instanceof ItemFood) && !fMaid.weaponReload) {
					int lastentityid = worldObj.loadedEntityList.size();
					int itemcount = litemstack.stackSize;
					fMaid.mstatAimeBow = true;
					getAvatarIF().getValueVectorFire(atx, aty, atz, atl);
					// ダイヤ、金ヘルムなら味方への誤射を気持ち軽減
					boolean lcanattack = true;
					boolean ldotarget = false;
					double tpr = Math.sqrt(atl);
					Entity lentity = MMM_Helper.getRayTraceEntity(fMaid.maidAvatar, tpr + 1.0F, 1.0F, 1.0F);
					Item helmid = !fMaid.isMaskedMaid() ? null : fInventory.armorInventory[3].getItem();
					if (helmid == Items.diamond_helmet || helmid == Items.golden_helmet) {
						// 射線軸の確認
						if (lentity != null && fMaid.getIFF(lentity)) {
							lcanattack = false;
//							mod_LMM_littleMaidMob.Debug("ID:%d-friendly fire to ID:%d.", fMaid.entityId, lentity.entityId);
						}
					}
					if (lentity == fTarget) {
						ldotarget = true;
					}
					lcanattack &= (milsq > 3D || il < 0D);
					lcanattack &= ldotarget;
					// 射線上に味方がいれば横移動、4秒以上敵のHPが変わらない場合も横移動
					if (!lcanattack || fTargetDamegeCounter >= 4 * 20) {
						// 射撃位置を確保する
						double tpx = fMaid.posX;
						double tpy = fMaid.posY;
						double tpz = fMaid.posZ;
//						double tpr = Math.sqrt(atl) * 0.5D;
						tpr = tpr * 0.25D;
						if (fTargetSearchDir==false) {
							// 左回り
							tpx += (atz / tpr);
							tpz -= (atx / tpr);
						} else {
							// 右回り
							tpx -= (atz / tpr);
							tpz += (atx / tpr);
						}
						fMaid.getNavigator().tryMoveToXYZ(tpx, tpy, tpz, 1.0F);
					}
					else if (lsee & ldist < 100) {
						fMaid.getNavigator().clearPathEntity();
//						mod_LMM_littleMaidMob.Debug("Shooting Range.");
					}
					
					lcanattack &= lsee;
//            		mod_littleMaidMob.Debug(String.format("id:%d at:%d", entityId, attackTime));
					if (((fMaid.weaponFullAuto && !lcanattack) || (lcanattack && fMaid.getSwingStatusDominant().canAttack())) && getAvatarIF().getIsItemTrigger()) {
						// シュート
						// フルオート武器は射撃停止
						LMM_LittleMaidMobX.Debug("id:%d shoot.", fMaid.getEntityId());
						fAvatar.stopUsingItem();
						fMaid.setSwing(30, LMM_EnumSound.shoot);
					} else {
						// チャージ
						if (litemstack.getMaxItemUseDuration() > 500) {
//                			mod_littleMaidMob.Debug(String.format("non reload.%b", isMaskedMaid));
							// リロード無しの通常兵装
							if (!getAvatarIF().isUsingItemLittleMaid()) {
								// 構え
								if (!fMaid.weaponFullAuto || lcanattack) {
									// フルオート兵装の場合は射線確認
									int at = ((helmid == Items.iron_helmet) || (helmid == Items.diamond_helmet)) ? 26 : 16;
									if (swingState.attackTime < at) {
										fMaid.setSwing(at, LMM_EnumSound.sighting);
										litemstack = litemstack.useItemRightClick(worldObj, fAvatar);
										LMM_LittleMaidMobX.Debug("id:%d redygun.", fMaid.getEntityId());
									}
								} else {
									LMM_LittleMaidMobX.Debug(String.format("ID:%d-friendly fire FullAuto.", fMaid.getEntityId()));
								}
							}
						} 
						else if (litemstack.getMaxItemUseDuration() == 0) {
							// 通常投擲兵装
							if (swingState.canAttack() && !fAvatar.isUsingItem()) {
								if (lcanattack) {
									litemstack = litemstack.useItemRightClick(worldObj, fAvatar);
									// 意図的にショートスパンで音が鳴るようにしてある
									fMaid.mstatAimeBow = false;
									fMaid.setSwing(10, (litemstack.stackSize == itemcount) ? LMM_EnumSound.shoot_burst : LMM_EnumSound.Null);
									LMM_LittleMaidMobX.Debug(String.format("id:%d throw weapon.(%d:%f:%f)", fMaid.getEntityId(), swingState.attackTime, fMaid.rotationYaw, fMaid.rotationYawHead));
								} else {
									LMM_LittleMaidMobX.Debug(String.format("ID:%d-friendly fire throw weapon.", fMaid.getEntityId()));
								}
							}
						} else {
							// リロード有りの特殊兵装
							if (!getAvatarIF().isUsingItemLittleMaid()) {
								litemstack = litemstack.useItemRightClick(worldObj, fAvatar);
								LMM_LittleMaidMobX.Debug(String.format("%d reload.", fMaid.getEntityId()));
							}
							// リロード終了まで強制的に構える
							swingState.attackTime = 5;
						}
					}
//            		maidAvatarEntity.setValueRotation();
					getAvatarIF().setValueVector();
					// アイテムが亡くなった
					if (litemstack.stackSize <= 0) {
						fMaid.destroyCurrentEquippedItem();
						fMaid.getNextEquipItem();
					} else {
						fInventory.setInventoryCurrentSlotContents(litemstack);
					}
					
					// 発生したEntityをチェックしてmaidAvatarEntityが居ないかを確認
					List<Entity> newentitys = worldObj.loadedEntityList.subList(lastentityid, worldObj.loadedEntityList.size());
					boolean shootingflag = false;
					if (newentitys != null && newentitys.size() > 0) {
						LMM_LittleMaidMobX.Debug(String.format("new FO entity %d", newentitys.size()));
						for (Entity te : newentitys) {
							if (te.isDead) {
								shootingflag = true;
								continue;
							}
							try {
								// 飛翔体の主を置き換える
								Field fd[] = te.getClass().getDeclaredFields();
//                				mod_littleMaidMob.Debug(String.format("%s, %d", e.getClass().getName(), fd.length));
								for (Field ff : fd) {
									// 変数を検索しAvatarと同じ物を自分と置き換える
									ff.setAccessible(true);
									Object eo = ff.get(te);
									if (eo != null && eo.equals(fAvatar)) {
										ff.set(te, this.fMaid);
										LMM_LittleMaidMobX.Debug("Replace FO Owner.");
									}
								}
							}
							catch (Exception exception) {
								exception.printStackTrace();
							}
						}
					}
					// 既に命中していた場合の処理
					if (shootingflag) {
						for (Object obj : worldObj.loadedEntityList) {
							if (obj instanceof EntityCreature && !(obj instanceof LMM_EntityLittleMaid)) {
								EntityCreature ecr = (EntityCreature)obj;
								if (ecr.getEntityToAttack() == fAvatar) {
									ecr.setTarget(fMaid);
								}
							}
						}
					}
				}
			}
		} else {
			// 有効射程外
			if (fMaid.getNavigator().noPath()) {
				fMaid.getNavigator().tryMoveToEntityLiving(fTarget, 1.0);
			}
			if (fMaid.getNavigator().noPath()) {
				LMM_LittleMaidMobX.Debug("id:%d Target renge out.", fMaid.getEntityId());
				fMaid.setAttackTarget(null);
			}
			if (fMaid.weaponFullAuto && getAvatarIF().getIsItemTrigger()) {
				fAvatar.stopUsingItem();
			} else {
				fAvatar.clearItemInUse();
			}
			
		}
		

		// プレイヤーが射線に入らないように、変更したメイドさんの位置を元に戻す
		fMaid.posX = backupPosX;
		fMaid.posZ = backupPosZ;
	}

	@Override
	public void setEnable(boolean pFlag) {
		fEnable = pFlag;
	}

	@Override
	public boolean getEnable() {
		return fEnable;
	}

}
