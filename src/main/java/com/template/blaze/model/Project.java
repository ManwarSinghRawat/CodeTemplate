package com.template.blaze.model;

import java.util.List;

import org.apache.maven.model.Model;

import lombok.Data;

@Data
public class Project {

	String projectID;
	String projectName;
	ProjectType projectType;
	String projectGroupID;
	String projectArtifactID;
	String parentArtifactID;
	String parentGroupID;
	Model pomModel;
	String projectPath;
	String basePackagePath;
	String projectArtifactPath;
	String serverFileDirectoryPath;
	String projectResourcePath;
	String adbFileDirectoryPath;
	List<ServerFile> serverFileDetails;
	List<JavaFile> javaFiles;
	List<Schema> schemaDetails;
	List<Project> subProjects;

}
