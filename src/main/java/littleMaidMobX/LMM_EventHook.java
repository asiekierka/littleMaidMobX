package littleMaidMobX;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class LMM_EventHook
{
	@SubscribeEvent
	public void onEntityItemPickupEvent(EntityItemPickupEvent event)
	{
		if(event.entityPlayer instanceof LMM_EntityLittleMaidAvatar)
		{
			if(event.item!=null && LMM_LittleMaidMobX.isMaidIgnoreItem(event.item.getEntityItem()))
			{
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntitySpawned(EntityJoinWorldEvent event)
	{
		if (event.entity instanceof EntityArrow) {
			EntityArrow arrow = (EntityArrow) event.entity;
			if (arrow.shootingEntity instanceof LMM_IEntityLittleMaidAvatarBase) {
				LMM_IEntityLittleMaidAvatarBase avatar = (LMM_IEntityLittleMaidAvatarBase) arrow.shootingEntity;
				/* if (arrow.isDead) {
					for (Object obj : arrow.worldObj.loadedEntityList) {
						if (obj instanceof EntityCreature && !(obj instanceof LMM_EntityLittleMaid)) {
							EntityCreature ecr = (EntityCreature)obj;
							if (ecr.getEntityToAttack() == avatar) {
								ecr.setTarget(avatar.getMaid());
							}
						}
					}
				} */
				arrow.shootingEntity = avatar.getMaid();
				LMM_LittleMaidMobX.Debug("Set "+event.entity.getClass()+" field shootingEntity from avator to maid");
			}
		}
	}
}
