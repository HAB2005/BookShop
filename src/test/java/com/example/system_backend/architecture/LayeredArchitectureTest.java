package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;

/**
 * Test kiến trúc phân lớp của ứng dụng
 */
@DisplayName("Layered Architecture Tests")
class LayeredArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Kiến trúc phân lớp phải được tuân thủ")
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()  // Only check dependencies within our layers
                
                // Định nghĩa các layer
                .layer("Controller").definedBy("..controller..")
                .layer("Application").definedBy("..application..")
                .layer("Domain").definedBy("..domain..")
                .layer("Repository").definedBy("..repository..")
                .layer("Entity").definedBy("..entity..")
                .layer("DTO").definedBy("..dto..")
                .layer("Mapper").definedBy("..mapper..")
                .layer("Common").definedBy("..common..")
                
                // Định nghĩa quy tắc truy cập giữa các layer
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Controller").mayOnlyAccessLayers("Application", "DTO", "Mapper", "Common")
                .whereLayer("Application").mayOnlyAccessLayers("Domain", "Repository", "Entity", "DTO", "Mapper", "Common")
                .whereLayer("Domain").mayOnlyAccessLayers("Entity", "Repository", "Common")
                .whereLayer("Repository").mayOnlyAccessLayers("Entity", "Common")
                .whereLayer("Mapper").mayOnlyAccessLayers("Entity", "DTO", "Application", "Common")
                
                // Ignore Spring Data types in Repository layer (Pageable, Page, etc.)
                .ignoreDependency(
                        resideInAPackage("..repository.."),
                        resideInAPackage("org.springframework.data.domain..")
                );

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller chỉ được phụ thuộc vào Application layer và DTO")
    void controllersShouldOnlyDependOnApplicationLayerAndDTOs() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..application..",
                        "..dto..",
                        "..common..",
                        "..mapper..",  // Allow mapper for response transformation
                        "java..",
                        "org.springframework..",
                        "jakarta..",
                        "lombok..",
                        "org.slf4j.."
                );

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repository không được phụ thuộc vào Controller hoặc Application")
    void repositoriesShouldNotDependOnControllerOrApplication() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackages("..controller..", "..application..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain service không được phụ thuộc vào Controller hoặc Application")
    void domainServicesShouldNotDependOnControllersOrApplication() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackages("..controller..", "..application..", "..dto..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Entity không được phụ thuộc vào bất kỳ layer nào khác")
    void entitiesShouldNotDependOnOtherLayers() {
        ArchRule rule = classes()
                .that().resideInAPackage("..entity..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..entity..",
                        "..common..",
                        "java..",
                        "jakarta..",
                        "org.hibernate..",
                        "lombok.."
                );

        rule.check(importedClasses);
    }
}
