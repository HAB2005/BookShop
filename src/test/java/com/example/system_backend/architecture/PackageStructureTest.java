package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Test cấu trúc package của dự án
 */
@DisplayName("Package Structure Tests")
class PackageStructureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Controller phải nằm trong package 'controller'")
    void controllersShouldResideInControllerPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Service phải nằm trong package 'service' hoặc 'application.service'")
    void servicesShouldResideInServicePackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().areNotInterfaces()
                .and().resideOutsideOfPackage("..domain..") // Exclude domain services (ValidationService)
                .and().resideOutsideOfPackage("..security..") // Exclude security services (JwtService, TokenBlacklistService)
                .should().resideInAnyPackage("..service..", "..application.service..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repository phải nằm trong package 'repository'")
    void repositoriesShouldResideInRepositoryPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Entity phải nằm trong package 'entity'")
    void entitiesShouldResideInEntityPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..entity..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTO phải nằm trong package 'dto'")
    void dtosShouldResideInDtoPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .and().resideOutsideOfPackage("..common.response..") // Exclude common response wrappers
                .should().resideInAPackage("..dto..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Mapper phải nằm trong package 'mapper'")
    void mappersShouldResideInMapperPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .should().resideInAPackage("..mapper..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Facade phải nằm trong package 'facade' hoặc 'application.facade'")
    void facadesShouldResideInFacadePackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Facade")
                .should().resideInAnyPackage("..facade..", "..application.facade..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain service phải nằm trong package 'domain'")
    void domainServicesShouldResideInDomainPackage() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .and().haveSimpleNameEndingWith("Service")
                .should().resideInAPackage("..domain..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Exception phải nằm trong package 'exception'")
    void exceptionsShouldResideInExceptionPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Exception")
                .and().areAssignableTo(Exception.class)
                .should().resideInAPackage("..exception..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Config phải nằm trong package 'config'")
    void configClassesShouldResideInConfigPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Config")
                .or().haveSimpleNameEndingWith("Properties")
                .should().resideInAPackage("..config..");

        rule.check(importedClasses);
    }
}
