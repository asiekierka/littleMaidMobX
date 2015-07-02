package network;

import littleMaidMobX.LMM_LittleMaidMobX;
import littleMaidMobX.LMM_ProxyClient;
import littleMaidMobX.LMM_Net;
import mmmlibx.lib.MMMLib;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class W_MessageHandler implements IMessageHandler<W_Message, IMessage>
{
	@Override//IMessageHandlerのメソッド
	public IMessage onMessage(W_Message message, MessageContext ctx)
	{
		if(message.data != null)
		{
			if(ctx.side.isClient())
			{
				LMM_LittleMaidMobX.proxy.clientCustomPayload(message);
			}
			else
			{
				if(message.ch == 1)
				{
					MMMLib.serverCustomPayload(ctx.getServerHandler().playerEntity, message);
				}
				if(message.ch == 2)
				{
					LMM_Net.serverCustomPayload(ctx.getServerHandler().playerEntity, message);
				}
			}
		}
		return null;//本来は返答用IMessageインスタンスを返すのだが、旧来のパケットの使い方をするなら必要ない。
	}
}
