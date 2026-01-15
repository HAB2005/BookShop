package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Test các quy tắc phụ thuộc giữa các package
 */
@DisplayName("Dependency Rule Tests")
class DependencyRuleTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Không có phụ thuộc vòng giữa các package")
    void noCircularDependenciesBetweenPackages() {
        ArchRule rule = com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
                .slices()
                .matching("com.example.system_backend.(*)..")
                .should().beFreeOfCycles();

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTO không được phụ thuộc vào Entity")
    void dtosShouldNotDependOnEntities() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..dto..")
                .should().dependOnClassesThat()
                .resideInAPackage("..entity..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller không được phụ thuộc trực tiếp vào Repository")
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller không được phụ thuộc trực tiếp vào Entity")
    void controllersShouldNotDependOnEntities() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..entity..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Các module không được phụ thuộc lẫn nhau (auth, product, user, order, stock)")
    void modulesShouldNotDependOnEachOther() {
        String[] modules = {"auth", "product", "user", "order", "stock"};
        
        for (String module : modules) {
            for (String otherModule : modules) {
                if (!module.equals(otherModule)) {
                    ArchRule rule = noClasses()
                            .that().resideInAPackage("..system_backend." + module + "..")
                            .should().dependOnClassesThat()
                            .resideInAPackage("..system_backend." + otherModule + "..")
                            .allowEmptyShould(true); // Allow empty modules
                    
                    rule.check(importedClasses);
                }
            }
        }
    }

    @Test
    @DisplayName("Không có class nào được phụ thuộc vào implementation cụ thể của logging")
    void noClassesShouldDependOnConcreteLoggingImplementations() {
        ArchRule rule = noClasses()
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.apache.log4j..",
                        "ch.qos.logback.."
                );

        rule.check(importedClasses);
    }
}
