package littleMaidMobX;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class LMM_OldZipTexturesLoader implements IResourcePack {

	public static Map<String, File> keys = new HashMap<String,File>();

	@Override
	public InputStream getInputStream(ResourceLocation arg0) throws IOException {
		if(resourceExists(arg0)){
			String key = arg0.getResourcePath();
			if(key.startsWith("/")) key = key.substring(1);
			File file = new File(keys.get(key));
			ZipFile zip = new ZipFile(file);
			InputStream i = zip.getInputStream(zip.getEntry(key));
			return i;
		}
		return null;
	}

	@Override
	public BufferedImage getPackImage() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public IMetadataSection getPackMetadata(IMetadataSerializer arg0,
		String arg1) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getPackName() {
		// TODO 自動生成されたメソッド・スタブ
		return "OldTexturesLoader";
	}

	@Override
	public Set<String> getResourceDomains() {
		// TODO 自動生成されたメソッド・スタブ
		return ImmutableSet.of("mmmlibx");
	}

	@Override
	public boolean resourceExists(ResourceLocation arg0) {
		// TODO 自動生成されたメソッド・スタブ
		String key = arg0.getResourcePath();
		if(key.startsWith("/")) key = key.substring(1);
		return keys.containsKey(key);
	}

}
