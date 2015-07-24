package littleMaidMobX;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class LMM_EntityMode_Ripper extends LMM_EntityModeBase {

	public static final int mmode_Ripper	= 0x0081;
	public static final int mmode_TNTD		= 0x00c1;
	public static final int mmode_Detonator	= 0x00c2;
	
	public int timeSinceIgnited;
	public int lastTimeSinceIgnited;
	

	public LMM_EntityMode_Ripper(LMM_EntityLittleMaid pEntity) {
		super(pEntity);
		timeSinceIgnited = -1;
	}

	@Override
	public int priority() {
		return 3100;
	}

	@Override
	public void init() {
	}

	@Override
	public void addEntityMode(EntityAITasks pDefaultMove, EntityAITasks pDefaultTargeting) {
		// Ripper:0x0081
		EntityAITasks[] ltasks = new EntityAITasks[2];
		ltasks[0] = new EntityAITasks(owner.aiProfiler);
		ltasks[1] = new EntityAITasks(owner.aiProfiler);

		ltasks[0].addTask(1, owner.aiSwiming);
		ltasks[0].addTask(2, owner.func_70907_r());
		ltasks[0].addTask(3, owner.aiJumpTo);
		ltasks[0].addTask(4, owner.aiAttack);
		ltasks[0].addTask(5, owner.aiPanic);
		ltasks[0].addTask(6, owner.aiBeg);
		ltasks[0].addTask(7, owner.aiBegMove);
		ltasks[0].addTask(8, owner.aiAvoidPlayer);
//		ltasks[0].addTask(7, pentitylittlemaid.aiCloseDoor);
//		ltasks[0].addTask(8, pentitylittlemaid.aiOpenDoor);
//		ltasks[0].addTask(9, pentitylittlemaid.aiCollectItem);
		ltasks[0].addTask(10, owner.aiFollow);
//		ltasks[0].addTask(11, new EntityAILeapAtTarget(pentitylittlemaid, 0.3F));
		ltasks[0].addTask(11, owner.aiWander);
		ltasks[0].addTask(12, new EntityAIWatchClosest(owner, EntityLivingBase.class, 10F));
		ltasks[0].addTask(12, new EntityAILookIdle(owner));

		ltasks[1].addTask(1, new LMM_EntityAINearestAttackableTarget(owner, EntityCreeper.class, 0, true));
		ltasks[1].addTask(2, new LMM_EntityAINearestAttackableTarget(owner, EntityTNTPrimed.class, 0, true));
		ltasks[1].addTask(3, new LMM_EntityAINearestAttackableTarget(owner, EntitySheep.class, 0, true));

		owner.addMaidMode(ltasks, "Ripper", mmode_Ripper);

		// TNT-D:0x00c1
		EntityAITasks[] ltasks2 = new EntityAITasks[2];
		ltasks2[0] = ltasks[0];
		ltasks2[1] = new EntityAITasks(owner.aiProfiler);
		ltasks2[1].addTask(1, new LMM_EntityAINearestAttackableTarget(owner, EntityCreeper.class, 0, true));
		ltasks2[1].addTask(2, new LMM_EntityAINearestAttackableTarget(owner, EntityTNTPrimed.class, 0, true));
		
		owner.addMaidMode(ltasks2, "TNT-D", mmode_TNTD);
		
		// Detonator:0x00c2
		EntityAITasks[] ltasks3 = new EntityAITasks[2];
		ltasks3[0] = pDefaultMove;
		ltasks3[1] = new EntityAITasks(owner.aiProfiler);
		ltasks3[1].addTask(1, new LMM_EntityAINearestAttackableTarget(owner, EntityLivingBase.class, 0, true));

		owner.addMaidMode(ltasks3, "Detonator", mmode_Detonator);


	}

	@Override
	public void updateAITick(int pMode) {
		ItemStack litemstack = owner.maidInventory.getCurrentItem();
		if (litemstack != null
				&& (owner.getAttackTarget() instanceof EntityCreeper || owner.getEntityToAttack() instanceof EntityTNTPrimed)) {
			if (pMode == mmode_Ripper) {
				owner.setMaidMode("TNT-D");
				owner.maidOverDriveTime.setEnable(true);
			} else if (owner.getMaidModeInt() == mmode_TNTD && litemstack.getItem() instanceof ItemShears) {
				owner.maidOverDriveTime.setEnable(true);
			}
		}
		if (!owner.maidOverDriveTime.isEnable() && pMode == mmode_TNTD) {
			owner.setMaidMode("Ripper");
//    		getNextEquipItem();
		}
	}

	@Override
	public void onUpdate(int pMode) {
		// 自爆モード
		if (pMode == mmode_Detonator && owner.isEntityAlive()) {
			if (timeSinceIgnited < 0) {
				if (lastTimeSinceIgnited != timeSinceIgnited) {
					owner.getDataWatcher().updateObject(LMM_Statics.dataWatch_Free, Integer.valueOf(0));
				}
				else if (owner.getDataWatcher().getWatchableObjectInt(LMM_Statics.dataWatch_Free) == 1) {
					lastTimeSinceIgnited = timeSinceIgnited = 0;
				}
			}
			lastTimeSinceIgnited = timeSinceIgnited;
			if (timeSinceIgnited > -1) {
				// 最期の瞬間はセツナイ
				if (owner.isMovementCeased() || timeSinceIgnited > 22) {
					owner.getLookHelper().setLookPositionWithEntity(owner.getMaidMasterEntity(), 40F, 40F);
				}
				LMM_LittleMaidMobX.Debug(String.format("ID:%d(%s)-dom:%d(%d)", owner.getEntityId(), owner.worldObj.isRemote ? "C" : "W", owner.maidDominantArm, owner.maidInventory.currentItem));
				
				if (owner.maidInventory.isItemExplord(owner.maidInventory.currentItem) && timeSinceIgnited++ > 30) {
					// TODO:自爆威力を対応させたいけど無理ぽ？
					owner.maidInventory.decrStackSize(owner.maidInventory.currentItem, 1);
					// インベントリをブチマケロ！
					owner.maidInventory.dropAllItems(true);
					timeSinceIgnited = -1;
					owner.setDead();
					// Mobによる破壊の是非
					boolean lflag = owner.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
					owner.worldObj.createExplosion(owner, owner.posX, owner.posY, owner.posZ, 3F, lflag);
				}
			}
		}
	}

	@Override
	public boolean changeMode(EntityPlayer pentityplayer) {
		ItemStack litemstack = owner.maidInventory.getStackInSlot(0);
		if (litemstack != null) {
			if (litemstack.getItem() instanceof ItemShears) {
				owner.setMaidMode("Ripper");
				return true;
			}
			if (owner.maidInventory.isItemExplord(0)) {
				owner.setMaidMode("Detonator");
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean setMode(int pMode) {
		switch (pMode) {
		case mmode_Ripper :
			owner.setBloodsuck(false);
			return true;
		case mmode_TNTD :
			owner.setBloodsuck(false);
			return true;
		case mmode_Detonator :
			owner.setBloodsuck(true);
//			owner.aiPanic.
			timeSinceIgnited = -1;
			
			return true;
		}
		
		return false;
	}

	@Override
	public int getNextEquipItem(int pMode) {
		int li;
		ItemStack litemstack;
		
		// モードに応じた識別判定、速度優先
		switch (pMode) {
		case mmode_Ripper :
		case mmode_TNTD :
			for (li = 0; li < owner.maidInventory.maxInventorySize; li++) {
				litemstack = owner.maidInventory.getStackInSlot(li);
				if (litemstack == null) continue;
				
				// はさみ
				if (litemstack.getItem() instanceof ItemShears) {
					return li;
				}
			}
			break;
		case mmode_Detonator :
			for (li = 0; li < owner.maidInventory.maxInventorySize; li++) {
				// 爆発物
				if (owner.maidInventory.isItemExplord(li)) {
					return li;
				}
			}
			break;
		}
		
		return -1;
	}



	@Override
	public boolean attackEntityAsMob(int pMode, Entity pEntity) {
		if (pMode == mmode_Detonator) {
			// 通常殴り
			return false;
		}
		
		if (owner.getSwingStatusDominant().canAttack()) {
			ItemStack lis = owner.getCurrentEquippedItem();
			if (pEntity instanceof EntityCreeper) {
				// TODO:カットオフ
				// なんでPrivateにかえたし
				try {
					lis.damageItem((Integer)ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class,
							(EntityCreeper)pEntity, "field_70833_d", "timeSinceIgnited"), owner);
//							(EntityCreeper)pEntity, 1), owner.maidAvatar);
					ObfuscationReflectionHelper.setPrivateValue(EntityCreeper.class, (EntityCreeper)pEntity, 1, "field_70833_d", "timeSinceIgnited");
				} catch (Exception e) {
					e.printStackTrace();
				}
//				((EntityCreeper)pEntity).timeSinceIgnited = 0;
				owner.setSwing(20, LMM_EnumSound.attack_bloodsuck);
			} else if (pEntity instanceof EntityTNTPrimed) {
				pEntity.setDead();
				lis.damageItem(1, owner);
//				lis.damageItem(1, owner.maidAvatar);
				owner.setSwing(20, LMM_EnumSound.attack_bloodsuck);
			} else {
				owner.maidAvatar.interactWith(pEntity);
				owner.setSwing(20, LMM_EnumSound.attack);
			}
			if (lis.stackSize <= 0) {
				owner.maidInventory.setInventoryCurrentSlotContents(null);
				owner.getNextEquipItem();
			}
		}
		
		return true;
	}

	@Override
	public boolean isSearchEntity() {
		return true;
	}
	
	@Override
	public boolean checkEntity(int pMode, Entity pEntity) {
		if (owner.maidInventory.currentItem < 0) {
			return false;
		}
		switch (pMode) {
		case mmode_Detonator :
			return !owner.getIFF(pEntity);
		case mmode_Ripper :
			if (pEntity instanceof EntitySheep) {
				EntitySheep les = (EntitySheep)pEntity;
				if (!les.getSheared() && !les.isChild()) {
					return true;
				}
			}
		case mmode_TNTD :
			if (pEntity instanceof EntityCreeper) {
				return true;
			}
			if (pEntity instanceof EntityTNTPrimed) {
				return true;
			}
			break;
		}
		
		return false;
	}
	
	protected float setLittleMaidFlashTime(float f) {
		// 爆発カウントダウン発光時間
		if (timeSinceIgnited > -1) {
			return ((float)this.lastTimeSinceIgnited + (float)(this.timeSinceIgnited - this.lastTimeSinceIgnited) * f) / 28.0F;
		} else { 
			return 0F;
		}
	}
	
	@Override
	public int colorMultiplier(float pLight, float pPartialTicks) {
		float f2 = setLittleMaidFlashTime(pPartialTicks);
		
		if((int)(f2 * 10F) % 2 == 0) {
			return 0;
		}
		int i = (int)(f2 * 0.2F * 255F);
		if(i < 0)
		{
			i = 0;
		}
		if(i > 255)
		{
			i = 255;
		}
		LMM_LittleMaidMobX.Debug(String.format("%2x", i));
		char c = '\377';
		char c1 = '\377';
		char c2 = '\377';
		return i << 24 | c << 16 | c1 << 8 | c2;
	}
	
	@Override
	public boolean damageEntity(int pMode, DamageSource par1DamageSource, float par2) {
		// 起爆
		if (pMode == mmode_Detonator && owner.maidInventory.isItemExplord(owner.getCurrentEquippedItem())) {
			if (timeSinceIgnited == -1) {
				owner.playSound("random.fuse", 1.0F, 0.5F);
				owner.getDataWatcher().updateObject(LMM_Statics.dataWatch_Free, Integer.valueOf(1));
			}
//        	if (owner.entityToAttack == null)
			owner.setMaidWait(true);
		}
		
		return false;
	}

}
