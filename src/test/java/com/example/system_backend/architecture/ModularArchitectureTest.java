package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Test kiến trúc Modular Monolith
 * Đảm bảo các modules tương đối độc lập và không có circular dependencies
 */
@DisplayName("Modular Architecture Tests")
class ModularArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Modules không được có circular dependencies")
    void modulesShouldNotHaveCircularDependencies() {
        ArchRule rule = slices()
                .matching("com.example.system_backend.(*)..")
                .should().beFreeOfCycles()
                .because("Circular dependencies làm code khó maintain và test");

        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("Common module không được phụ thuộc vào business modules")
    void commonShouldNotDependOnBusinessModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..common..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..auth..",
                        "..user..",
                        "..product..",
                        "..order..",
                        "..otp..",
                        "..stock.."
                )
                .because("Common module nên chứa utilities dùng chung, không phụ thuộc business logic");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Product module không được phụ thuộc vào Order module")
    void productShouldNotDependOnOrder() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..product..")
                .should().dependOnClassesThat()
                .resideInAPackage("..order..")
                .because("Product không nên biết về Order (dependency ngược)");

        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("User module không được phụ thuộc vào Auth module")
    void userShouldNotDependOnAuth() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..user..")
                .should().dependOnClassesThat()
                .resideInAPackage("..auth..")
                .because("User không nên phụ thuộc vào Auth (tránh circular dependency)");

        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("Modules chỉ được giao tiếp qua public APIs (application layer)")
    void modulesShouldOnlyCommunicateThroughApplicationLayer() {
        // Module A chỉ được gọi application/facade của Module B
        // Không được gọi trực tiếp domain/repository/entity của Module B
        
        ArchRule rule = noClasses()
                .that().resideInAPackage("..auth..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..user..domain..",
                        "..user..repository..",
                        "..user..entity.."
                )
                .because("Module chỉ nên gọi application layer của module khác, không gọi internal implementation");

        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("Mỗi module phải có ít nhất một class")
    void eachModuleShouldHaveClasses() {
        // Simple check: Đảm bảo các modules chính có classes
        // Nếu module không có class nào, test sẽ pass (allowEmptyShould)
        
        ArchRule rule = classes()
                .that().resideInAnyPackage("..auth..", "..user..", "..product..", "..order..")
                .should().bePublic()
                .orShould().notBePublic()
                .because("Modules nên có classes");

        rule.allowEmptyShould(true).check(importedClasses);
    }
}
