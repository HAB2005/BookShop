package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Test các quy tắc liên quan đến Spring Framework
 */
@DisplayName("Spring Architecture Tests")
class SpringArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Controller phải có @RestController hoặc @Controller")
    void controllersShouldHaveControllerAnnotation() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(Controller.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("REST Controller method phải có mapping annotation")
    void restControllerMethodsShouldHaveMappingAnnotation() {
        ArchRule rule = methods()
                .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                .and().arePublic()
                .should().beAnnotatedWith(RequestMapping.class)
                .orShould().beAnnotatedWith(GetMapping.class)
                .orShould().beAnnotatedWith(PostMapping.class)
                .orShould().beAnnotatedWith(PutMapping.class)
                .orShould().beAnnotatedWith(DeleteMapping.class)
                .orShould().beAnnotatedWith(PatchMapping.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Service phải có @Service annotation")
    void servicesShouldHaveServiceAnnotation() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..service..", "..application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .and().areNotInterfaces()
                .should().beAnnotatedWith(Service.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Không sử dụng @Autowired field injection")
    void shouldNotUseFieldInjection() {
        ArchRule rule = noClasses()
                .should().dependOnClassesThat()
                .areAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller không được có @Transactional")
    void controllersShouldNotBeTransactional() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repository phải extend JpaRepository")
    void repositoriesShouldExtendJpaRepository() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areInterfaces()
                .should().beAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Configuration class phải có @Configuration")
    void configurationClassesShouldHaveConfigurationAnnotation() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Config")
                .and().resideInAPackage("..config..")
                .should().beAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                .orShould().beAnnotatedWith(org.springframework.boot.context.properties.ConfigurationProperties.class);

        rule.check(importedClasses);
    }
}
