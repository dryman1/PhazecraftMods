package net.phazecraft.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.phazecraft.log.Logger;

public class MinecraftVersionDetector {
	public MinecraftVersionDetector() {
	}

	/**
	 * Finds out using some clever tricks the current minecraft version version
	 * 
	 * @param jarFilePath
	 *            The .minecraft directory
	 * @return The version of the jar file
	 */
	public String getMinecraftVersion(String jarFilePath) {
		try {
			ZipInputStream file = new ZipInputStream(new FileInputStream(new File(jarFilePath, "bin/" + "minecraft.jar")));
			ZipEntry ent;
			ent = file.getNextEntry();
			while (ent != null) {
				if (ent.getName().contains("Minecraft.class")) {
					StringBuilder sb = new StringBuilder();
					for (int c = file.read(); c != -1; c = file.read()) {
						sb.append((char) c);
					}
					String data = sb.toString();
					String search = "Minecraft 1";
					file.closeEntry();
					file.close();
					return data.substring(data.indexOf(search) + 10, data.indexOf(search) + search.length() + 4);
				}
				file.closeEntry();
				ent = file.getNextEntry();
			}
			file.close();
		} catch (IOException e1) {
			Logger.logError(e1.getMessage(), e1);
			return "error";
		}
		return "unknown";
	}

	public boolean shouldUpdate(String jarFilePath) {

		return true;
	}
}
