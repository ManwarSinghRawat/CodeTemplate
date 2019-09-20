package com.template.blaze.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.template.blaze.model.Project;
import com.template.blaze.model.ProjectType;
import com.template.blaze.model.Schema;

public class PomGeneratorTest {

	//@Test
	public void generatePOM() throws ParserConfigurationException, TransformerException {
		Project project = new Project();
		project.setProjectArtifactID("projectArtifactID");
		project.setProjectGroupID("projectGroupID");
		project.setProjectPath("C:/tmp/");
		project.setProjectType(ProjectType.PARENT);
		List<Project> subprojects = new ArrayList<>();
		subprojects.add(project);
		project.setSubProjects(subprojects);
		POMGenerator pomGenerator = new POMGenerator();
		
		try {
			Model model = pomGenerator.generatePOM(project);
			assertEquals(project.getProjectArtifactID(), model.getArtifactId());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void createConfigurationDOMObjectTest() {
		
		POMGenerator pomGenerator = new POMGenerator();
		Schema schema = new Schema();
		schema.setBindingFileName("testdinding.xjb");
	    schema.setSchemaFileName("schema.xsd");
	    schema.setSchemaPath("C:/tmp/xsd/");
	    schema.setBindingFilePath("C:/tmp/schema/binding/");
	
		assertTrue(pomGenerator.createConfigurationDOMObject(schema,"project-name")!=null);
		
		
	}
	
	
	
}
