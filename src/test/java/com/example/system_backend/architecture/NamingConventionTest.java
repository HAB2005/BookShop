package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Test quy ước đặt tên cho các class
 */
@DisplayName("Naming Convention Tests")
class NamingConventionTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Controller phải có tên kết thúc bằng 'Controller'")
    void controllersShouldHaveControllerSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().areAnnotatedWith(RestController.class)
                .or().areAnnotatedWith(Controller.class)
                .should().haveSimpleNameEndingWith("Controller");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repository phải có tên kết thúc bằng 'Repository'")
    void repositoriesShouldHaveRepositorySuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areAnnotatedWith(Repository.class)
                .or().areInterfaces()
                .should().haveSimpleNameEndingWith("Repository");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Service phải có tên kết thúc bằng 'Service'")
    void servicesShouldHaveServiceSuffix() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..service..", "..application.service..")
                .and().areAnnotatedWith(Service.class)
                .should().haveSimpleNameEndingWith("Service");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Facade phải có tên kết thúc bằng 'Facade'")
    void facadesShouldHaveFacadeSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..facade..")
                .should().haveSimpleNameEndingWith("Facade");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTO Request phải có tên kết thúc bằng 'Request'")
    void requestDTOsShouldHaveRequestSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..dto..")
                .and().haveSimpleNameContaining("Request")
                .and().areNotMemberClasses() // Exclude inner classes like Builder
                .should().haveSimpleNameEndingWith("Request");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTO Response phải có tên kết thúc bằng 'Response'")
    void responseDTOsShouldHaveResponseSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..dto..")
                .and().haveSimpleNameContaining("Response")
                .and().areNotMemberClasses() // Exclude inner classes like Builder
                .should().haveSimpleNameEndingWith("Response");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Mapper phải có tên kết thúc bằng 'Mapper'")
    void mappersShouldHaveMapperSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..mapper..")
                .should().haveSimpleNameEndingWith("Mapper");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Exception phải có tên kết thúc bằng 'Exception'")
    void exceptionsShouldHaveExceptionSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..exception..")
                .and().areAssignableTo(Exception.class)
                .should().haveSimpleNameEndingWith("Exception");

        rule.check(importedClasses);
    }
}
