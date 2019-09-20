package com.template.blaze.model;

import lombok.Data;

@Data
public class Schema {

	String schemaPath;
	String schemaFileName;
	String bindingFilePath;
	String bindingFileName;
	String schemaID;
	boolean isExistBindingFile;
}
