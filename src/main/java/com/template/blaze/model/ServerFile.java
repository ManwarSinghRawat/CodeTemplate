package com.template.blaze.model;

import java.util.List;

import lombok.Data;

@Data
public class ServerFile {
	String name;
	List<ServiceDtls> serviceDetails;
	String serverFilePath;
}
