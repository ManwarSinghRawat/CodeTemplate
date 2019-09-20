package com.template.blaze.model;

import lombok.Data;

@Data
public class ServiceDtls {

	String entryPointID;
	boolean alwaysMapObjectsForDefaultModeRuleSets;
	boolean alwaysMapPostObjects;
	String srlArgumentType;
	String srlReturnType;
	String srlName;
	
	
}
