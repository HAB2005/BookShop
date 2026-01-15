package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Test annotation cho các class
 */
@DisplayName("Annotation Tests")
class AnnotationTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Controller phải được annotate với @RestController")
    void controllersShouldBeAnnotatedWithRestController() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(RestController.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Service phải được annotate với @Service")
    void servicesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..service..", "..application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .and().areNotInterfaces()
                .should().beAnnotatedWith(Service.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repository interface phải extend JpaRepository")
    void repositoriesShouldExtendJpaRepository() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areInterfaces()
                .and().haveSimpleNameEndingWith("Repository")
                .should().beAssignableTo(JpaRepository.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Entity phải được annotate với @Entity")
    void entitiesShouldBeAnnotatedWithEntity() {
        ArchRule rule = classes()
                .that().resideInAPackage("..entity..")
                .and().areNotMemberClasses()
                .and().areNotEnums()
                .and().haveSimpleNameNotEndingWith("Id") // Exclude composite key classes
                .should().beAnnotatedWith(Entity.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Facade phải được annotate với @Service")
    void facadesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAPackage("..facade..")
                .and().haveSimpleNameEndingWith("Facade")
                .should().beAnnotatedWith(Service.class);

        rule.check(importedClasses);
    }
}
