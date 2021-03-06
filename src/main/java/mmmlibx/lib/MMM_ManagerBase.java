package mmmlibx.lib;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.discovery.ASMDataTable;
import mmmlibx.lib.multiModel.MMMLoader.MMMModContainer;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class MMM_ManagerBase {
	protected abstract String getPreFix();
	/**
	 * 追加処理の本体
	 */
	protected abstract boolean append(Class pclass);

	private void loadFile(File f) {
		if (f.exists() && f.canRead()) {
			if (f.isDirectory()) {
				decodeDirectory(f, f);
			} else {
				decodeZip(f);
			}
		}
	}

	protected void load() {
		for (Entry<String, List<File>> le : FileManager.fileList.entrySet()) {
			for (File lf : le.getValue()) {
				loadFile(lf);
			}
		}
	}

	private void decodeDirectory(File baseFile, File pfile) {
		// ディレクトリ内のクラスを検索
		for (File lf : pfile.listFiles()) {
			if (lf.isFile()) {
				String lname = lf.getName();
				if (lname.indexOf(getPreFix()) >= 0 && lname.endsWith(".class")) {
					loadClass(MMM_Helper.getRelativePathSimple(baseFile, lf));
				}
			} else if (lf.isDirectory()) {
				decodeDirectory(baseFile, lf);
			}
		}
	}

	private void decodeZip(File pfile) {
		// zipファイルを解析
		try {
			FileInputStream fileinputstream = new FileInputStream(pfile);
			ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
			ZipEntry zipentry;
			
			do {
				zipentry = zipinputstream.getNextEntry();
				if(zipentry == null) {
					break;
				}
				if (!zipentry.isDirectory()) {
					String lname = zipentry.getName();
					if (lname.indexOf(getPreFix()) >= 0 && lname.endsWith(".class")) {
						loadClass(zipentry.getName());
					}
				}
			} while(true);
			
			zipinputstream.close();
			fileinputstream.close();
		}
		catch (Exception exception) {
			MMMLib.Debug("add%sZip-Exception.", getPreFix());
		}
		
	}

	private void loadClass(String pname) {
		if (pname == null) {
			return;
		}
		String lclassname = "";
		// 対象ファイルをクラスとしてロード
		try {
			ClassLoader lclassLoader = MMMLib.class.getClassLoader();
			Package lpackage = MMMLib.class.getPackage();
			lclassname = pname.replace(".class", "");
			Class lclass;
			if(lpackage != null) {
	// TODO ★	lclassname = (new StringBuilder(String.valueOf(lpackage.getName()))).append(".").append(lclassname).toString();
				lclassname = lclassname.replace("/", ".");
// LMM_EntityModeManager でしか使ってないので暫定
				lclass = lclassLoader.loadClass(lclassname);
			} else {
				lclass = Class.forName(lclassname);
			}
			if (Modifier.isAbstract(lclass.getModifiers())) {
				return;
			}
			if (append(lclass)) {
				MMMLib.Debug("get%sClass-done: %s", getPreFix(), lclassname);
			} else {
				MMMLib.Debug("get%sClass-fail: %s", getPreFix(), lclassname);
			}
			/*
            if (!(MMM_ModelStabilizerBase.class).isAssignableFrom(lclass) || Modifier.isAbstract(lclass.getModifiers())) {
            	MMMLib.Debug(String.format(String.format("get%sClass-fail: %s", pprefix, lclassname)));
                return;
            }
            
            MMM_ModelStabilizerBase lms = (MMM_ModelStabilizerBase)lclass.newInstance();
            pmap.put(lms.getName(), lms);
            MMMLib.Debug(String.format("get%sClass-done: %s[%s]", pprefix, lclassname, lms.getName()));
            */
		}
		catch (Exception exception) {
			MMMLib.Debug("get%sClass-Exception.(%s)", getPreFix(), lclassname);
			exception.printStackTrace();
		}
		catch (Error error) {
			MMMLib.Debug("get%sClass-Error: %s", getPreFix(), lclassname);
			error.printStackTrace();
		}
		
	}
	
	
}
