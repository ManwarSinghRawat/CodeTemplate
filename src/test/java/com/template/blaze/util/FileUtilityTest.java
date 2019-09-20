package com.template.blaze.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class FileUtilityTest {

	@Test
	public void createDirectoriesTest() {
		String dirPath = "C:/tmp/testDir";
		File file = new File(dirPath);
		if (file.exists()) {
			assertFalse(FileUtility.createDirectories(dirPath));
		} else {
			assertTrue(FileUtility.createDirectories(dirPath));
		}

	}

	@Test
	public void createJavaFile() {
		String dirPath = "C:/tmp/testDir";
		String fileSource = "hello Java";
		String className = "Test";
		File file = new File(dirPath);
		if (file.exists()) {
			file.delete();
		}
		FileUtility.createJavaFile(dirPath, fileSource, className);

	}

}
