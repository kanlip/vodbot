package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;


@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ApplicationTests {

    ApplicationModules modules = ApplicationModules.of(Application.class);
	@Test
	void contextLoads() {
	}
//
	@Test
	void writeDocumentationSnippets() {

		new Documenter(modules)
				.writeModulesAsPlantUml()
				.writeIndividualModulesAsPlantUml();
	}
	@Test
	void shouldBeCompliant() {
       
        modules.verify();
	}
}

