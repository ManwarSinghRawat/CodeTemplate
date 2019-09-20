package com.template.blaze.model;

import java.util.List;

import lombok.Data;

@Data
public class JavaFile {

	String packageName;
	String className;
	String fileLocation;
	String classContents;
	List<String> importPackageList;
	boolean isMainClass;
}
