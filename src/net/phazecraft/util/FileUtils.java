
package net.phazecraft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.phazecraft.log.Logger;

public class FileUtils {
	/**
	 * @param sourceFolder - the folder to be moved
	 * @param destinationFolder - where to move to
	 * @throws IOException
	 */
	public static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
		if (sourceFolder.isDirectory()) {
			if (!destinationFolder.exists()) {
				destinationFolder.mkdirs();
			}
			String files[] = sourceFolder.list();
			for (String file : files) {
				File srcFile = new File(sourceFolder, file);
				File destFile = new File(destinationFolder, file);
				copyFolder(srcFile, destFile);
			}
		} else {
			copyFile(sourceFolder, destinationFolder);
		}
	}

	/**
	 * @param sourceFile - the file to be moved
	 * @param destinationFile - where to move to
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destinationFile) throws IOException {
		if (sourceFile.exists()) {
			if(!destinationFile.exists()) {
				destinationFile.createNewFile();
			}
			FileChannel sourceStream = null, destinationStream = null;
			try {
				sourceStream = new FileInputStream(sourceFile).getChannel();
				destinationStream = new FileOutputStream(destinationFile).getChannel();
				destinationStream.transferFrom(sourceStream, 0, sourceStream.size());
			} finally {
				if(sourceStream != null) {
					sourceStream.close();
				}
				if(destinationStream != null) {
					destinationStream.close();
				}
			}
		}
	}

	/**
	 * @param resource - the resource to delete
	 * @return whether deletion was successful
	 * @throws IOException
	 */
	public static boolean delete(File resource) throws IOException {
		if (resource.isDirectory()) {
			File[] childFiles = resource.listFiles();
			for (File child : childFiles) {
				delete(child);
			}
		}
		return resource.delete();
	}

	/**
	 * Extracts given zip to given location
	 * @param zipLocation - the location of the zip to be extracted
	 * @param outputLocation - location to extract to
	 */
	public static void extractZipTo(String zipLocation, String outputLocation) {
		ZipInputStream zipinputstream = null;
		try {
			byte[] buf = new byte[1024];
			zipinputstream = new ZipInputStream(new FileInputStream(zipLocation));
			ZipEntry zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) { 
				String entryName = zipentry.getName();
				int n;
				if(!zipentry.isDirectory() && !entryName.equalsIgnoreCase("minecraft") && !entryName.equalsIgnoreCase(".minecraft") && !entryName.equalsIgnoreCase("instMods")) {
					new File(outputLocation + File.separator + entryName).getParentFile().mkdirs();
					FileOutputStream fileoutputstream = new FileOutputStream(outputLocation + File.separator + entryName);             
					while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
						fileoutputstream.write(buf, 0, n);
					}
					fileoutputstream.close();
				}
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();
			}
		} catch (Exception e) {
			Logger.logError(e.getMessage(), e);
			backupExtract(zipLocation, outputLocation);
		} finally {
			try {
				zipinputstream.close();
			} catch (IOException e) { }
		}
	}

	public static void backupExtract(String zipLocation, String outputLocation){
		Logger.logInfo("Extracting (Backup way)");
		byte[] buffer = new byte[1024];
		ZipInputStream zis = null;
		ZipEntry ze = null;
		try{
			File folder = new File(outputLocation);
			if(!folder.exists()){
				folder.mkdir();
			}
			zis = new ZipInputStream(new FileInputStream(zipLocation));
			ze = zis.getNextEntry();
			while(ze != null){
				File newFile = new File(outputLocation, ze.getName());
				newFile.getParentFile().mkdirs();
				if(!ze.isDirectory()) {
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.flush();
					fos.close();
				}
				ze = zis.getNextEntry();
			}
		} catch(IOException ex) {
			Logger.logError(ex.getMessage(), ex);
		} finally {
			try {
				zis.closeEntry();
				zis.close();
			} catch (IOException e) { }	
		}
	}    

	/**
	 * deletes the META-INF
	 */
	public static void killMetaInf() {
	}
}