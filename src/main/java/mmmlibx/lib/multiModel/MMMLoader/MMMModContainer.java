package mmmlibx.lib.multiModel.MMMLoader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLConstructionEvent;

public class MMMModContainer extends DummyModContainer {
	public MMMModContainer() {
		super(new ModMetadata());
		ModMetadata lmeta = getMetadata();
		
		lmeta.modId		= "OldModelLoader";
		lmeta.name		= "OldModelLoader";
		lmeta.version	= "1.0";
		lmeta.authorList	= Arrays.asList("MMM");
		lmeta.description	= "The MultiModel before 1.6.2 is read.";
		lmeta.url			= "";
		lmeta.credits		= "";
		setEnabledState(true);
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		// これ付けないとDisableで判定されちゃう。
		return true;
	}

	@Override
	public Class<?> getCustomResourcePackClass() {
		// 古いリソースを読み込むためのリソースパック
		return MMMResourcePack.class;
	}
}
