package littleMaidMobX;

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
}
