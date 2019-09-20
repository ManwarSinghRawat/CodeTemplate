package com.template.blaze.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "gencode", headerHeading = "@|bold,underline Usage|@:%n", synopsisHeading = "%n", descriptionHeading = "%n@|bold,underline Description|@:%n%n", description = "Blaze Template Generator genrate code based on the input .server and adb file%n%n", usageHelpAutoWidth = true, optionListHeading = "@|bold,underline Options|@:%n%n")
public class TemplateGenCommand {

	@Option(names = { "-p", "--project" }, description = "Project Name", defaultValue = "demo")
	private String projectName;

	@Option(names = { "-sf",
			"--server-file-directory" }, description = "Directory of your server file", required = true)
	private String serverFileDirectoryPath;

	@Option(names = { "-af",
			"--artifact-directory-path" }, description = "Directory of your project-dependecy jar files", required = true)
	private String artifactsDirectoryPath;

	@Option(names = { "-x", "--schema-directory-path" }, description = "Directory of your schema files",required = true)
	private String xsdFileDirectoryPath;

	@Option(names = { "-t",
			"--target-directory-path" }, description = "Directory of your target generated source code", required = true)
	private String targetDirectoryPath;

	@Option(names = { "-adb", "--adb-directory-path" }, description = "Directory of your adb file", required = true)
	private String adbFileDirectoryPath;

	@Option(names = { "-h", "--help" }, description = "Display help/usage.", help = true)
	boolean help;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getServerFileDirectoryPath() {
		return serverFileDirectoryPath;
	}

	public void setServerFileDirectoryPath(String serverFileDirectoryPath) {
		this.serverFileDirectoryPath = serverFileDirectoryPath;
	}

	public String getArtifactsDirectoryPath() {
		return artifactsDirectoryPath;
	}

	public void setArtifactsDirectoryPath(String artifactsDirectoryPath) {
		this.artifactsDirectoryPath = artifactsDirectoryPath;
	}

	public String getXsdFileDirectoryPath() {
		return xsdFileDirectoryPath;
	}

	public void setXsdFileDirectoryPath(String xsdFileDirectoryPath) {
		this.xsdFileDirectoryPath = xsdFileDirectoryPath;
	}

	public String getTargetDirectoryPath() {
		return targetDirectoryPath;
	}

	public void setTargetDirectoryPath(String targetDirectoryPath) {
		this.targetDirectoryPath = targetDirectoryPath;
	}

	public boolean isHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

	public String getAdfFileDirectoryPath() {
		return adbFileDirectoryPath;
	}

	public void setAdbFileDirectoryPath(String adbFileDirectoryPath) {
		this.adbFileDirectoryPath = adbFileDirectoryPath;
	}

	@Override
	public String toString() {
		return "TemplateGenCommand [projectName=" + projectName + ", serverFileDirectoryPath=" + serverFileDirectoryPath
				+ ", artifactsDirectoryPath=" + artifactsDirectoryPath + ", xsdFileDirectoryPath="
				+ xsdFileDirectoryPath + ", targetDirectoryPath=" + targetDirectoryPath + ", adfFileDirectoryPath="
				+ adbFileDirectoryPath + ", help=" + help + "]";
	}

	
}
