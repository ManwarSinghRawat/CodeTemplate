package com.template.blaze.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.template.blaze.model.Project;
import com.template.blaze.model.UserInput;

public class ProjectTemplateGeneratorTest {

	
	//@Test
	public void generateParentProjectTest() {
		ProjectTemplateGenerator projectTemplateGenerator = new ProjectTemplateGenerator();
		UserInput userInput = new UserInput();
		userInput.setProjectName("TestProject");
		userInput.setTargetDirectoryPath("C:/tmp/test/");
		Project testProject = projectTemplateGenerator.generateParentProject(userInput);
		
	
		assertEquals("C:/tmp/test/",testProject.getProjectID());
		System.out.println(testProject.toString());	
	}

	
	@Test
	public void getSchemasTest() {
		ProjectTemplateGenerator projectTemplateGenerator = new ProjectTemplateGenerator();
		System.out.println(projectTemplateGenerator.getSchemas("C:/tmp/template/schema-test/").toString());
		
		assertTrue(true);
	}
	
	
	
	
}
	
	
	
	
