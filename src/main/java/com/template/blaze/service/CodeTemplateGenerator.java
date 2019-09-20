package com.template.blaze.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.template.blaze.model.JavaFile;
import com.template.blaze.model.Project;
import com.template.blaze.model.ServerFile;
import com.template.blaze.model.ServiceDtls;
import com.template.blaze.util.TemplateConstant;

@Service
public class CodeTemplateGenerator {

	VelocityEngine velocityEngine;

	static final Logger logger = LoggerFactory.getLogger(CodeTemplateGenerator.class);

	public JavaFile generateMainClass(Project project) {

		JavaFile javaFile = new JavaFile();
		Template t = velocityEngine.getTemplate("templates/code/MainClassTemplate.vm");
		VelocityContext context = new VelocityContext();
		context.put(TemplateConstant.PACKAGE, project.getProjectArtifactID());
		context.put(TemplateConstant.CLASS, project.getProjectName());
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		javaFile.setClassName(project.getProjectName());
		javaFile.setFileLocation(project.getBasePackagePath());
		javaFile.setPackageName(project.getProjectArtifactID());
		javaFile.setClassContents(writer.toString());
		javaFile.setMainClass(true);
		return javaFile;
	}

	// Create rest and related classes
	public List<JavaFile> generateRestControllerClasses(Project project) {

		List<JavaFile> restContollerClasses = new ArrayList<>();
		restContollerClasses.add(generateMainClass(project));
		generateApplicationPropertyFile(project.getServerFileDetails(), project.getProjectResourcePath(),
				project.getAdbFileDirectoryPath());

		List<ServerFile> serverFiles = project.getServerFileDetails();
		// ServerFile
		ServerFile serverFile = serverFiles.get(0);
		JavaFile javaFile;
		Template t;
		StringWriter writer;
		VelocityContext context;
		Map<String, String> importPackages = prepareImportPackagesList(
				project.getSchemaDetails().get(0).getSchemaPath(), project.getServerFileDetails(),
				project.getParentGroupID());
		StringBuilder importPackageStament = new StringBuilder();
		for (Map.Entry<String, String> packageMapEntry : importPackages.entrySet()) {
			importPackageStament.append("import " + packageMapEntry.getValue() + ";\r\n");
		}

		t = velocityEngine.getTemplate("templates/code/DecisionService.vm");
		context = new VelocityContext();
		context.put(TemplateConstant.PACKAGE, project.getProjectArtifactID());
		context.put(TemplateConstant.CLASS, "DecisionService");
		context.put("importStatement", importPackageStament);
		context.put("serviceRequestObj", serverFile.getServiceDetails().get(0).getSrlArgumentType());
		context.put("serviceResponseObj", serverFile.getServiceDetails().get(0).getSrlReturnType());
		writer = new StringWriter();
		t.merge(context, writer);
		javaFile = new JavaFile();
		javaFile.setClassName("DecisionService");
		javaFile.setFileLocation(project.getBasePackagePath() + "/model");
		javaFile.setPackageName(project.getProjectArtifactID());
		javaFile.setClassContents(writer.toString());
		restContollerClasses.add(javaFile);

		t = velocityEngine.getTemplate("templates/code/BlazeServer.vm");
		context = new VelocityContext();
		String packageName = project.getProjectArtifactID() + ".model";
		context.put(TemplateConstant.PACKAGE, packageName);
		context.put(TemplateConstant.CLASS, "BlazeServer");
		writer = new StringWriter();
		t.merge(context, writer);
		javaFile = new JavaFile();
		javaFile.setClassName("BlazeServer");
		javaFile.setFileLocation(project.getBasePackagePath() + "/model");
		javaFile.setPackageName(project.getProjectArtifactID());
		javaFile.setClassContents(writer.toString());
		restContollerClasses.add(javaFile);
		updateServerFilesJavaClassName(project.getServerFileDetails(), packageName + "." + "BlazeServer");

		t = velocityEngine.getTemplate("templates/code/DecisionServiceFactory.vm");
		context = new VelocityContext();
		javaFile = new JavaFile();
		context.put(TemplateConstant.PACKAGE, project.getProjectArtifactID());
		context.put(TemplateConstant.CLASS, "DecisionServiceFactory");
		writer = new StringWriter();
		t.merge(context, writer);
		javaFile.setClassName("DecisionServiceFactory");
		javaFile.setFileLocation(project.getBasePackagePath() + "/model");
		javaFile.setPackageName(project.getProjectArtifactID());
		javaFile.setClassContents(writer.toString());
		restContollerClasses.add(javaFile);

		t = velocityEngine.getTemplate("templates/code/RestClassTemplate.vm");
		context = new VelocityContext();
		context.put(TemplateConstant.PACKAGE, project.getProjectArtifactID());
		context.put(TemplateConstant.CLASS, "APIController");
		context.put("serviceEntryPoint", serverFile.getName());
		context.put("serviceDetails", serverFile.getServiceDetails());
		context.put("importStatement", importPackageStament);

		javaFile = new JavaFile();
		writer = new StringWriter();
		t.merge(context, writer);
		javaFile.setClassName("APIController");
		javaFile.setFileLocation(project.getBasePackagePath());
		javaFile.setPackageName(project.getProjectArtifactID());
		javaFile.setClassContents(writer.toString());
		restContollerClasses.add(javaFile);

		t = velocityEngine.getTemplate("templates/code/SwaggerConfig.vm");
		context = new VelocityContext();
		context.put("packagename", project.getProjectArtifactID());
		javaFile = new JavaFile();
		writer = new StringWriter();
		t.merge(context, writer);
		javaFile.setClassName("SwaggerConfig");
		javaFile.setFileLocation(project.getBasePackagePath());
		javaFile.setPackageName(project.getProjectArtifactID());
		javaFile.setClassContents(writer.toString());
		restContollerClasses.add(javaFile);

		return restContollerClasses;
	}

	// Read Server File and update ServerFactory Java Full classpath of BlazeServer
	public void updateServerFilesJavaClassName(List<ServerFile> serverFileList, String classPath) {
		String serverFilePath;
		if (serverFileList != null) {
			serverFilePath = serverFileList.get(0).getServerFilePath();
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = docFactory.newDocumentBuilder();
				Document doc = documentBuilder.parse(serverFilePath);
				NodeList nodelist = doc.getElementsByTagName("ServerFactory");
				NodeList childNodelist = nodelist.item(0).getChildNodes();
				if ("JavaName".equalsIgnoreCase(childNodelist.item(0).getNodeName())) {
					childNodelist.item(0).setTextContent(classPath);
				}
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(serverFilePath));
				transformer.transform(source, result);

			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}

		}

	}

	private void generateApplicationPropertyFile(List<ServerFile> serverFileDetails, String projectResourcePath,
			String adbFilePath) {
		Template t;
		StringWriter writer;
		VelocityContext context;
		t = velocityEngine.getTemplate("templates/code/application_property_template.vm");
		context = new VelocityContext();
		context.put("serverFileDetails", serverFileDetails);
		context.put("clientIDs", prepareClientIDs(serverFileDetails));
		context.put("adbFilePath", adbFilePath);
		writer = new StringWriter();
		t.merge(context, writer);
		String filePath = projectResourcePath + "/application.properties";
		File codeFile = new File(filePath);
		try (FileWriter fileWriter = new FileWriter(codeFile)) {
			fileWriter.write(writer.toString());
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	private String prepareClientIDs(List<ServerFile> serverFileDetails) {
		StringBuilder clintIDs = new StringBuilder();
		for (ServerFile serverFile : serverFileDetails) {
			clintIDs.append(serverFile.getName() + ",");
		}
		return clintIDs.toString();
	}

	public CodeTemplateGenerator() {
		super();
		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		velocityEngine.setProperty("classpath.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		velocityEngine.init();
	}

	public Map<String, String> prepareImportPackagesList(String schemaPath, List<ServerFile> severfiles,
			String parentGroupID) {
		Map<String, String> packageMap = new HashMap<>();
		String packageName;
		for (ServerFile serverFile : severfiles) {
			for (ServiceDtls serviceDetails : serverFile.getServiceDetails()) {
				packageName = parentGroupID + ".schema."
						+ checkForSchemaFileName(serviceDetails.getSrlArgumentType(), schemaPath).replaceAll("\\W", "")
								.toLowerCase()
						+ ".";
				packageMap.put(serviceDetails.getSrlArgumentType(), packageName + serviceDetails.getSrlArgumentType());
				packageMap.put(serviceDetails.getSrlReturnType(), packageName + serviceDetails.getSrlReturnType());
			}

		}
		return packageMap;
	}

	public String checkForSchemaFileName(String requestObjType, String schemaDirPath) {
		File schemaDir = new File(schemaDirPath);
		File[] schemaFileList = schemaDir.listFiles((dir, name) -> name.endsWith(".xsd"));
		String fileContents;
		for (File schemaFile : schemaFileList) {
			try {
				fileContents = Files.readAllLines(Paths.get(schemaFile.getPath())).toString();
				if (fileContents.contains(requestObjType)) {
					return schemaFile.getName().substring(0, schemaFile.getName().indexOf('.'));
				}
			} catch (IOException e) {
				logger.error("Error", e);
			}
		}
		return null;
	}

}
