package com.template.blaze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.template.blaze.cli.TemplateGenCommand;
import com.template.blaze.service.ProjectTemplateGenerator;

import picocli.CommandLine;

@SpringBootApplication
public class BlazeCodeTemplateGeneratorApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BlazeCodeTemplateGeneratorApplication.class, args);
		final TemplateGenCommand templateGenCommand = CommandLine.populateCommand(new TemplateGenCommand(), args);

		if (templateGenCommand.isHelp()) {
			CommandLine.usage(templateGenCommand, System.out, CommandLine.Help.Ansi.AUTO);
			System.exit(0);
		} else {

			//ApplicationContext context = new AnnotationConfigApplicationContext(ProjectTemplateGenerator.class);
			//ProjectTemplateGenerator projectTemplateGenerator = context.getBean(ProjectTemplateGenerator.class);
			ProjectTemplateGenerator projectTemplateGenerator = new ProjectTemplateGenerator();
			projectTemplateGenerator.generateProjectSkeleton(
					projectTemplateGenerator.prepareUserInputForProcessing(templateGenCommand));
		}
	}

}
