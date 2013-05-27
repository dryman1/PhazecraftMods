
package net.phazecraft.mclauncher;

import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.phazecraft.data.Settings;
import net.phazecraft.log.LogLevel;
import net.phazecraft.log.Logger;
import net.phazecraft.util.OSUtils;

public class MinecraftLauncher {
	public static Process launchMinecraft(String workingDir, String username, String password, String forgename, String rmax) throws IOException {
		String[] jarFiles = new String[] {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
		StringBuilder cpb = new StringBuilder("");
		File instModsDir = new File(new File(workingDir).getParentFile(), "instMods/");
		if(instModsDir.isDirectory()) {
			String[] files = instModsDir.list();
			Arrays.sort(files);
			for(String name : files) {
				if(!name.equals(forgename)) {
					if(name.toLowerCase().contains("forge") && name.toLowerCase().contains("minecraft") && name.toLowerCase().endsWith(".zip")) {
						if(new File(instModsDir, forgename).exists()) {
							if (!new File(instModsDir, forgename).equals(new File(instModsDir, name))) {
								new File(instModsDir, name).delete();
							}
						} else {
							new File(instModsDir, name).renameTo(new File(instModsDir, forgename));
						}
					} else if(!name.equalsIgnoreCase(forgename) && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar"))) {
						cpb.append(OSUtils.getJavaDelimiter());
						cpb.append(new File(instModsDir, name).getAbsolutePath());
					}
				}
			}
		} else {
			Logger.logInfo("Not loading any instMods (minecraft jar mods), as the directory does not exist.");
		}

		cpb.append(OSUtils.getJavaDelimiter());
		cpb.append(new File(instModsDir, forgename).getAbsolutePath());

		for(String jarFile : jarFiles) {
			cpb.append(OSUtils.getJavaDelimiter());
			cpb.append(new File(new File(workingDir, "bin"), jarFile).getAbsolutePath());
		}

		List<String> arguments = new ArrayList<String>();

		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java" + (OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS ? "w" : "");
		arguments.add(path);

		setMemory(arguments, rmax);

		arguments.add("-XX:+UseConcMarkSweepGC");
		arguments.add("-XX:+CMSIncrementalMode");
		arguments.add("-XX:+AggressiveOpts");
		arguments.add("-XX:+CMSClassUnloadingEnabled");
		arguments.add("-XX:MaxPermSize=128m");

		arguments.add("-cp");
		arguments.add(System.getProperty("java.class.path") + cpb.toString());
		
		String additionalOptions = Settings.getSettings().getAdditionalJavaOptions();
		if (!additionalOptions.isEmpty()) {
 			Collections.addAll(arguments, additionalOptions.split("\\s+"));
 		}

		arguments.add(MinecraftLauncher.class.getCanonicalName());
		arguments.add(workingDir);
		arguments.add(forgename);
		arguments.add(username);
		arguments.add(password);


		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		processBuilder.redirectErrorStream(true);
		return processBuilder.start();
	}

	private static void setMemory(List<String> arguments, String rmax) {
		boolean memorySet = false;
		try {
			int min = 256;
			if (rmax != null && Integer.parseInt(rmax) > 0) {
				arguments.add("-Xms" + min + "M");
				Logger.logInfo("Setting MinMemory to " + min);
				arguments.add("-Xmx" + rmax + "M");
				Logger.logInfo("Setting MaxMemory to " + rmax);
				memorySet = true;
			}
		} catch (Exception e) {
			Logger.logError("Error parsing memory settings", e);
		}
		if (!memorySet) {
			arguments.add("-Xms" + 256 + "M");
			Logger.logInfo("Defaulting MinMemory to " + 256);
			arguments.add("-Xmx" + 1024 + "M");
			Logger.logInfo("Defaulting MaxMemory to " + 1024);
		}
	}

	public static void main(String[] args) {
		String basepath = args[0], animationname = args[1], forgename = args[2], username = args[3], password = args[4], modPackName = args[5], modPackImageName = args[6];
		Settings.getSettings().save();  //Call so that the settings file is loaded from the correct location.  Would be wrong on OS X and *nix if called after user.home is reset

		try {
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			ArrayList<File> classPathFiles = new ArrayList<File>();
			File tempDir = new File(new File(basepath).getParentFile(), "instMods/");
			if(tempDir.isDirectory()) {
				for(String name : tempDir.list()) {
					if(!name.equalsIgnoreCase(forgename)) {
						if(name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")) {
							classPathFiles.add(new File(tempDir, name));
						}
					}
				}
			}

			classPathFiles.add(new File(tempDir, forgename));
			for(String jarFile : jarFiles) {
				classPathFiles.add(new File(new File(basepath, "bin"), jarFile));
			}	

			URL[] urls = new URL[classPathFiles.size()];
			for(int i = 0; i < classPathFiles.size(); i++) {
				try {
					urls[i] = classPathFiles.get(i).toURI().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				System.out.println("Added URL to classpath: " + urls[i].toString());
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(basepath, "bin"), "natives").toString();
			System.out.println("Natives loaded...");

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(basepath).getParent());

			URLClassLoader cl = new URLClassLoader(urls, MinecraftLauncher.class.getClassLoader());

			System.out.println("Loading minecraft class");
			Class<?> mc = cl.loadClass("net.minecraft.client.Minecraft");
			System.out.println("mc = " + mc);
			Field[] fields = mc.getDeclaredFields();
			System.out.println("field amount: " + fields.length);

			for (Field f : fields) {
				if (f.getType() != File.class) {
					continue;
				}
				if (0 == (f.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC))) {
					continue;
				}
				f.setAccessible(true);
				f.set(null, new File(basepath));
				System.out.println("Fixed Minecraft Path: Field was " + f.toString());
				break;
			}

			String mcDir = mc.getMethod("a", String.class).invoke(null, (Object) "minecraft").toString();

			System.out.println("MCDIR: " + mcDir);

			System.out.println("Launching with applet wrapper...");

			try {
				Class<?> MCAppletClass = cl.loadClass("net.minecraft.client.MinecraftApplet");
				Applet mcappl = (Applet) MCAppletClass.newInstance();
				MinecraftFrame mcWindow = new MinecraftFrame(modPackName, modPackImageName, animationname);
				mcWindow.start(mcappl, username, password);
			} catch (InstantiationException e) {
				Logger.log("Applet wrapper failed! Falling back to compatibility mode.", LogLevel.WARN, e);
				mc.getMethod("main", String[].class).invoke(null, (Object) new String[] {username, password});
			}
		} catch (Throwable t) {
			Logger.logError("Unhandled error launching minecraft", t);
		}
	}
}
