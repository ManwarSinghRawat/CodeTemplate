package com.template.blaze.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.template.blaze.cli.TemplateGenCommand;
import com.template.blaze.model.JavaFile;
import com.template.blaze.model.Project;
import com.template.blaze.model.ProjectType;
import com.template.blaze.model.Schema;
import com.template.blaze.model.ServerFile;
import com.template.blaze.model.ServiceDtls;
import com.template.blaze.model.UserInput;
import com.template.blaze.util.FileUtility;
import com.template.blaze.util.TemplateConstant;

//Create Skeleton
@Service
public class ProjectTemplateGenerator {
	@Autowired
	POMGenerator pomGenerator;

	@Autowired
	CodeTemplateGenerator codeTemplateGenerator;

	static final Logger logger = LoggerFactory.getLogger(ProjectTemplateGenerator.class);

	public Project generateProjectSkeleton(UserInput userInput) {
		Project project;
		String path = userInput.getTargetDirectoryPath() + "/" + userInput.getProjectName();
		FileUtility.createDirectories(path);
		project = generateParentProject(userInput);
		project.setProjectID(userInput.getProjectName());
		return project;

	}

	public Project generateParentProject(UserInput userInput) {
		pomGenerator = new POMGenerator();
		Project parentProject = new Project();
		String path = userInput.getTargetDirectoryPath() + "/" + userInput.getProjectName() + "/";
		parentProject.setProjectPath(path);
		parentProject.setProjectArtifactPath(userInput.getArtifactsDirectoryPath());
		parentProject.setServerFileDirectoryPath(userInput.getServerFileDirectoryPath());
		parentProject.setAdbFileDirectoryPath(userInput.getAdbFileDirectoryPath());
		FileUtility.createDirectories(path);
		parentProject.setProjectID(userInput.getProjectName());
		parentProject.setSchemaDetails(userInput.getSchemaDetails());
		parentProject
				.setProjectGroupID(TemplateConstant.PACKAGE_PREFIX + "." + userInput.getProjectName().toLowerCase());
		parentProject.setProjectArtifactID(userInput.getProjectName() + "-" + "parent");
		parentProject.setProjectType(ProjectType.PARENT);
		try {

			List<Project> subProjects = new ArrayList<>();
			subProjects.add(generateSubProject(parentProject, "Schema"));
			subProjects.add(generateSubProject(parentProject, "API"));
			parentProject.setSubProjects(subProjects);
			parentProject.setPomModel(pomGenerator.generatePOM(parentProject));
		} catch (IOException | XmlPullParserException e) {
			logger.error("Error", e);
		}

		return parentProject;
	}

	public Project generateSubProject(Project parentProject, String projectName) {
		Project subProject = new Project();
		subProject.setProjectName(projectName);
		subProject.setProjectID(projectName);
		subProject.setProjectArtifactPath(parentProject.getProjectArtifactPath());
		subProject.setProjectGroupID(parentProject.getProjectGroupID());
		subProject.setProjectArtifactID(parentProject.getProjectGroupID() + "." + projectName.toLowerCase());
		subProject.setParentGroupID(parentProject.getProjectGroupID());
		subProject.setServerFileDirectoryPath(parentProject.getServerFileDirectoryPath());
		subProject.setAdbFileDirectoryPath(parentProject.getAdbFileDirectoryPath());
		String subProjectPath = parentProject.getProjectPath() + subProject.getProjectArtifactID();
		subProject.setProjectPath(subProjectPath);
		if (parentProject.getSchemaDetails() != null)
			subProject.setSchemaDetails(parentProject.getSchemaDetails());
		subProject.setBasePackagePath(
				subProjectPath + TemplateConstant.BASE_JAVA_PATH + subProject.getProjectArtifactID().replace(".", "/"));
		subProject.setProjectType(ProjectType.CHILD);
		subProject.setParentArtifactID(parentProject.getProjectArtifactID());
		subProject.setServerFileDetails(readServerFiles(subProject.getServerFileDirectoryPath()));
		subProject.setProjectResourcePath(subProjectPath + TemplateConstant.RESOURCE_PATH);
		FileUtility.createDirectories(subProjectPath);
		FileUtility.createDirectories(subProject.getProjectResourcePath());
		FileUtility.createDirectories(subProjectPath + TemplateConstant.RESOURCE_PATH);
		FileUtility.createDirectories(subProject.getBasePackagePath());
		List<JavaFile> javaFiles = null;
		try {
			if (!"Schema".equalsIgnoreCase(projectName)) {
				if ("API".equalsIgnoreCase(projectName)) {
					codeTemplateGenerator = new CodeTemplateGenerator();
					javaFiles = codeTemplateGenerator.generateRestControllerClasses(subProject);
					subProject.setJavaFiles(javaFiles);
					saveJavaFiles(javaFiles);
				}

			}
			subProject.setPomModel(pomGenerator.generatePOM(subProject));

		} catch (XmlPullParserException | IOException e) {
			logger.error("Error", e);
		}

		return subProject;
	}

	public boolean saveJavaFiles(List<JavaFile> javaFiles) {
		String filePath;
		File codeFile;
		for (JavaFile javaCodeFile : javaFiles) {
			filePath = javaCodeFile.getFileLocation() + "/" + javaCodeFile.getClassName() + ".java";
			codeFile = new File(filePath);
			codeFile.getParentFile().mkdirs();
			try (FileWriter fileWriter = new FileWriter(codeFile)) {
				fileWriter.write(javaCodeFile.getClassContents());
			} catch (IOException e) {

				logger.error("Error", e);
			}
		}

		return true;
	}

	public List<ServerFile> readServerFiles(String serverFilePath) {
		File serverFilsDir = new File(serverFilePath);
		File[] serverFileList = serverFilsDir.listFiles((d, name) -> name.endsWith(".server"));
		List<ServerFile> serverFiles = null;
		if (serverFileList != null) {
			serverFiles = new ArrayList<>();
			for (File serverFile : serverFileList) {
				serverFiles.add(prepareServerFileObject(serverFile.getPath()));
			}
		}

		return serverFiles;
	}

	// Create ServerFile Object reading from .server File
	public ServerFile prepareServerFileObject(String serverFilePath) {
		File serverFile = new File(serverFilePath);
		ServerFile serverFileDetails = new ServerFile();
		serverFileDetails.setServerFilePath(serverFile.getPath().replace("\\" , "/"));
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(serverFile);
			doc.getDocumentElement().normalize();
			List<ServiceDtls> services = new ArrayList<>();
			ServiceDtls serviceDtls;
			Element eElement;
			NodeList nList = doc.getElementsByTagName("DeployRulesServerConfig");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node node = nList.item(temp);
				serviceDtls = new ServiceDtls();
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					eElement = (Element) node;
					serviceDtls.setEntryPointID(
							eElement.getElementsByTagName("EntryPointId").item(0).getTextContent().trim());
					serviceDtls.setSrlArgumentType(
							eElement.getElementsByTagName("SrlArgumentType").item(0).getTextContent().trim());
					serviceDtls.setSrlReturnType(
							eElement.getElementsByTagName("SrlReturnType").item(0).getTextContent().trim());
					serviceDtls.setSrlName(eElement.getElementsByTagName("SrlName").item(0).getTextContent().trim());
					serverFileDetails.setName(eElement.getElementsByTagName("Name").item(0).getTextContent().trim());
				}

				services.add(serviceDtls);
			}
			serverFileDetails.setServiceDetails(services);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error("Error", e);
		}

		return serverFileDetails;
	}

	public UserInput prepareUserInputForProcessing(TemplateGenCommand commandInput) {

		UserInput userInput = new UserInput();
		userInput.setAdbFileDirectoryPath(commandInput.getArtifactsDirectoryPath());
		userInput.setProjectName(commandInput.getProjectName());
		userInput.setTargetDirectoryPath(commandInput.getTargetDirectoryPath());
		userInput.setArtifactsDirectoryPath(commandInput.getArtifactsDirectoryPath());
		userInput.setServerFileDirectoryPath(commandInput.getServerFileDirectoryPath());
		userInput.setAdbFileDirectoryPath(commandInput.getAdfFileDirectoryPath());
		userInput.setSchemaDetails(getSchemas(commandInput.getXsdFileDirectoryPath()));
		return userInput;

	}

	public List<Schema> getSchemas(String schemaDirPath) {
		File schemaDir = new File(schemaDirPath);
		Schema schema = null;
		List<Schema> schemas = null;
		String schemaFileName;
		String bindingFileName;
		File[] schemaFileList = schemaDir.listFiles((dir, name) -> name.endsWith(".xsd"));
		File[] bindingFileList = schemaDir.listFiles((dir, name) -> name.endsWith(".xjb"));
		boolean bindingFileExist = false;
		schemas = new ArrayList<>();
		File bindingFile = null;
		for (File schemaFile : schemaFileList) {
			bindingFile = null;
			bindingFileExist= false;
			schemaFileName = schemaFile.getName().substring(0, schemaFile.getName().indexOf('.'));
			schema = new Schema();
			for (File bindingFileTemp : bindingFileList) {
				bindingFileName = bindingFileTemp.getName().substring(0, bindingFileTemp.getName().indexOf('.'));
				bindingFile = bindingFileTemp;
				if (schemaFileName.equalsIgnoreCase(bindingFileName)) {
					bindingFileExist = true;
					break;
				}
			}
			
			schema.setSchemaFileName(schemaFile.getName());
			if(bindingFileExist) {
				schema.setBindingFileName(bindingFile.getName());
				schema.setBindingFilePath(schemaDirPath);
			}
			schema.setSchemaPath(schemaDirPath);
			schema.setSchemaID(schemaFileName.replaceAll("\\W", "").toLowerCase());
			schema.setExistBindingFile(bindingFileExist);
			schemas.add(schema);
		}
		return schemas;
	}

}
