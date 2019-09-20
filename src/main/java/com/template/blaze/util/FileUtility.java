package com.template.blaze.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtility {

	public static boolean createDirectories(String directoryPath) {
		File file = new File(directoryPath);
		return file.mkdirs();

	}

	public static boolean createJavaFile(String directoryPath, String fileSource, String className) {
		File codeFile = new File(directoryPath + "/" + className + ".java");
		codeFile.getParentFile().mkdirs();
		try (FileWriter fileWriter = new FileWriter(codeFile)) {
			fileWriter.write(fileSource);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return codeFile.exists();
	}

	public static boolean isDirectoryExist(String directoryPath) {
		File file = new File(directoryPath);
		if (file.isDirectory()) {
		   return true;
		}
		
		return false;
	}
	
	public static boolean isFileExist(String filePath) {
		
		return true;
	}
	
	private FileUtility() {

	}
}
