package com.template.blaze.service;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;

import org.junit.Test;
import org.w3c.dom.Document;

import com.template.blaze.model.Project;
import com.template.blaze.model.ServerFile;
import com.template.blaze.model.ServiceDtls;

public class CodeTemplateGeneratorTest {

	//@Test
	public void readServerFileTest(){
		ProjectTemplateGenerator codeTemplateGenerator = new ProjectTemplateGenerator();
		System.out.println(codeTemplateGenerator.readServerFiles("C:/tmp/template").toString());
		assertTrue(true);
		
	}
	
	
	//@Test
	public void checkForSchemaFileName() throws FileNotFoundException {
		String requestObjType = "ScoreRequest";
		String schemaDirPath = "C:/tmp/template/schema";
		CodeTemplateGenerator codeTemplateGenerator = new CodeTemplateGenerator();
		codeTemplateGenerator.checkForSchemaFileName(requestObjType,  schemaDirPath);
		assertTrue(true);
		
	}
	//@Test
	public void  prepareImportPackagesList(){
		ProjectTemplateGenerator projectTemplateGenerator = new ProjectTemplateGenerator();
		CodeTemplateGenerator codeTemplateGenerator = new CodeTemplateGenerator();
		String parentGroupID = "com.fico.api";
		String schemaPath ="C:/tmp/template/schema";
		projectTemplateGenerator.readServerFiles("C:/tmp/template");
		Map<String, String> testMap = codeTemplateGenerator.prepareImportPackagesList(schemaPath, projectTemplateGenerator.readServerFiles("C:/tmp/template"),parentGroupID);
		System.out.println(testMap.entrySet());
		assertTrue(true);
	}
//@Test
public void updateServerFilesJavaClassName() {
	List<ServerFile> serverFileList = new ArrayList<ServerFile>();
	String packageName = "com.test.help";
	ServerFile sf = new ServerFile();
	sf.setServerFilePath("C:/tmp/template/DS_IFRS9_ser_test.server");
	serverFileList.add(sf);
	
	CodeTemplateGenerator codeTemplateGenerator = new CodeTemplateGenerator();
	codeTemplateGenerator.updateServerFilesJavaClassName(serverFileList, packageName);
	
	assertTrue(true);
	
}
}
