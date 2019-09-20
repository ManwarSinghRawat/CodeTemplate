package com.template.blaze.model;

import java.util.List;

import lombok.Data;

@Data
public class UserInput {
	String projectName;
	String serverFileDirectoryPath;
	String basePackageName;
	String artifactsDirectoryPath;
	String xsdFileDirectoryPath;
	String adbFileDirectoryPath;
	String targetDirectoryPath;
	List<Schema> schemaDetails;
}
