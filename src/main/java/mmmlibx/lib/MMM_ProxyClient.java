package mmmlibx.lib;

import littleMaidMobX.LMM_OldZipTexturesLoader;

import java.io.File;

public class MMM_ProxyClient extends MMM_ProxyCommon
{`
	public boolean isClient()
	{
		return true;
	}

	public void addTextureToOldZipLoader(String name, File file) {
		LMM_OldZipTexturesLoader.keys.put(name, file);
	}
}
