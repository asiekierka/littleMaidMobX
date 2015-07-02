package wrapper;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatisticsFile;

import com.mojang.authlib.GameProfile;

public interface W_ICommon
{
	public void		setOwner(EntityTameable entity, String name);
	public String	getOwnerName(IEntityOwnable entity);
	public GameProfile newGameProfile(String UUIDid, String name);
	public void		notifyAdmins(ICommandSender sender, ICommand cmd, int p_152374_2_, String s, Object ... p_152374_4_);
	public StatisticsFile getStatisticsFile(EntityPlayer player);
}
