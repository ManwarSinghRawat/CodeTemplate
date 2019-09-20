package com.template.blaze.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.template.blaze.model.JavaFile;
import com.template.blaze.model.Project;
import com.template.blaze.model.ProjectType;
import com.template.blaze.model.Schema;

@Service
public class POMGenerator {

	static final Logger logger = LoggerFactory.getLogger(POMGenerator.class);

	public Model generatePOM(Project project) throws IOException, XmlPullParserException {
		Resource resource;
		if (project.getProjectType() == ProjectType.PARENT) {
			resource = new ClassPathResource("templates/pom/parent_pom.xml");
		} else {
			if ("Schema".equalsIgnoreCase(project.getProjectName()))
				resource = new ClassPathResource("templates/pom/child_schema_pom.xml");
			else
				resource = new ClassPathResource("templates/pom/child_rest_pom.xml");
		}
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model = reader.read(resource.getInputStream());
		model.setGroupId(project.getProjectGroupID());
		model.setArtifactId(project.getProjectArtifactID());
		model.setName(project.getProjectArtifactID());
		if (project.getProjectType() == ProjectType.PARENT) {
			model.setModules(null);
			if (project.getSubProjects() != null) {
				List<String> modules = new ArrayList<>();
				for (Project subProject : project.getSubProjects()) {
					modules.add(subProject.getProjectArtifactID());
				}
				model.setModules(modules);
			}
		} else {

			if ("Schema".equalsIgnoreCase(project.getProjectName())) {
				Build build = model.getBuild();
				build.setPlugins(Arrays.asList(generateSchemaPlugin(project.getSchemaDetails(),
						build.getPlugins().get(0), project.getParentGroupID())));
				model.setBuild(build);
			} else {
				// Main class set
				if (project.getJavaFiles() != null) {
					model.getBuild().getPlugins().get(0)
							.setConfiguration(createMainClassConfigurationDOMObject(project.getJavaFiles()));
				}

				if (project.getParentArtifactID() != null) {
					Parent parent = model.getParent();
					parent.setArtifactId(project.getParentArtifactID());
					parent.setGroupId(project.getParentGroupID());
					model.setParent(parent);
				}
				List<Dependency> dependencyList = model.getDependencies();
				//local Dependencies
				dependencyList.addAll(prepareLocalDependencies(project.getProjectArtifactPath()));
				dependencyList.add(prepareSchemaDependency(project.getParentGroupID()));
				model.setDependencies(dependencyList);
			}
		}
		Writer writer = new FileWriter(project.getProjectPath() + "/pom.xml");

		new MavenXpp3Writer().write(writer, model);

		return model;
	}

	

	/**
	 * Generate Schema Plugin
	 * 
	 * @param schemaDetails
	 * @param templatePlugin
	 * @return
	 */
	private Plugin generateSchemaPlugin(List<Schema> schemaDetails, Plugin templatePlugin, String parentProjectID) {
		Plugin schemaPlugin = new Plugin();
		schemaPlugin.setGroupId(templatePlugin.getGroupId());
		schemaPlugin.setArtifactId(templatePlugin.getArtifactId());
		schemaPlugin.setVersion(templatePlugin.getVersion());
		if (schemaDetails != null) {
			List<PluginExecution> executions = new ArrayList<>();
			PluginExecution exectuion;
			for (Schema schema : schemaDetails) {
				exectuion = new PluginExecution();
				exectuion.setId(schema.getSchemaID());
				exectuion.setGoals(Arrays.asList("generate"));
				exectuion.setConfiguration(createConfigurationDOMObject(schema, parentProjectID));
				executions.add(exectuion);
			}

			schemaPlugin.setExecutions(executions);
		}
		return schemaPlugin;
	}

	/**
	 * Create the Plugin configuration of schemas
	 * 
	 * @param schema
	 * @return
	 */
	public Xpp3Dom createConfigurationDOMObject(Schema schema, String parentProjectID) {

		Xpp3Dom coniguration = new Xpp3Dom("configuration");
		Xpp3Dom schemaDirectory = new Xpp3Dom("schemaDirectory");
		schemaDirectory.setValue(schema.getSchemaPath());
		coniguration.addChild(schemaDirectory);
		Xpp3Dom schemaIncludes = new Xpp3Dom("schemaIncludes");
		Xpp3Dom schemaInclude = new Xpp3Dom("include");
		schemaInclude.setValue(schema.getSchemaFileName());
		schemaIncludes.addChild(schemaInclude);
		coniguration.addChild(schemaIncludes);
		if (schema.isExistBindingFile()) {
			Xpp3Dom bindingDirectory = new Xpp3Dom("bindingDirectory");
			bindingDirectory.setValue(schema.getBindingFilePath());
			coniguration.addChild(bindingDirectory);
			Xpp3Dom bindingIncludes = new Xpp3Dom("bindingIncludes");
			Xpp3Dom bindingInclude = new Xpp3Dom("include");
			bindingInclude.setValue(schema.getBindingFileName());
			bindingIncludes.addChild(bindingInclude);
			coniguration.addChild(bindingIncludes);
		}
		Xpp3Dom generatePackage = new Xpp3Dom("generatePackage");
		generatePackage.setValue(parentProjectID + ".schema." + schema.getSchemaID());
		coniguration.addChild(generatePackage);

		Xpp3Dom generateDirectory = new Xpp3Dom("generateDirectory");
		generateDirectory.setValue("${project.build.directory}/generated-sources/" + schema.getSchemaID());
		coniguration.addChild(generateDirectory);

		return coniguration;
	}

	/**
	 * Create Main Class configuration DOM object
	 * 
	 * @param javaFiles
	 * @return
	 */
	public Xpp3Dom createMainClassConfigurationDOMObject(List<JavaFile> javaFiles) {
		Xpp3Dom coniguration = null;
		Xpp3Dom mainClass;
		for (JavaFile javaFile : javaFiles) {
			if (javaFile.isMainClass()) {
				coniguration = new Xpp3Dom("configuration");
				mainClass = new Xpp3Dom("mainClass");
				mainClass.setValue(javaFile.getPackageName() + "." + javaFile.getClassName());
				coniguration.addChild(mainClass);
			}
		}
		return coniguration;
	}
	
	
	public List<Dependency> prepareLocalDependencies(String localLibArtifactPath){
		File libDir = new File(localLibArtifactPath);
		File[] directoryList = libDir.listFiles();
		Dependency localDependency;
		List<Dependency> dependencyList = null;
		if (directoryList != null) {
			dependencyList = new ArrayList<>();
			for (File dependecyFile : directoryList) {
				localDependency = new Dependency();
				localDependency.setSystemPath(dependecyFile.getPath());
				localDependency.setScope("system");
				localDependency.setArtifactId(dependecyFile.getName());
				localDependency.setGroupId("local");
				localDependency.setVersion("1.0.0");
				dependencyList.add(localDependency);
			}
		}
		return dependencyList;
	}
	
	
	public Dependency prepareSchemaDependency(String parentGroupID) {
		Dependency schemaDependency = new Dependency();
		schemaDependency.setGroupId(parentGroupID);
		schemaDependency.setArtifactId(parentGroupID+".schema");
		schemaDependency.setVersion("1.0.0");
		return schemaDependency;
	}
	
	
}
