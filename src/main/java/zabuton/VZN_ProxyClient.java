package zabuton;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class VZN_ProxyClient extends VZN_ProxyCommon
{
	public void RegistRenderer()
	{
		RenderingRegistry.registerEntityRenderingHandler(VZN_EntityZabuton.class, new VZN_RenderZabuton());
	}
}
