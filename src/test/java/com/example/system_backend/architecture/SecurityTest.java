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

/**
 * Test các quy tắc bảo mật
 */
@DisplayName("Security Tests")
class SecurityTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Không sử dụng System.out hoặc System.err")
    void shouldNotUseSystemOutOrErr() {
        ArchRule rule = noClasses()
                .should().callMethod(System.class, "out")
                .orShould().callMethod(System.class, "err");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Không sử dụng printStackTrace()")
    void shouldNotUsePrintStackTrace() {
        ArchRule rule = noClasses()
                .should().callMethod(Throwable.class, "printStackTrace");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repository phải là interface")
    void repositoriesShouldBeInterfaces() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().resideInAPackage("..repository..")
                .should().beInterfaces();

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Service classes phải tồn tại trong service packages")
    void servicesShouldExistInServicePackages() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().resideInAnyPackage("..service..", "..application.service..", "..domain..", "..security..")
                .and().areNotInterfaces()
                .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
                .orShould().resideInAPackage("..domain..") // Domain services may not have @Service
                .because("Service classes nên được annotate với @Service");

        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("Entity không được có public field (trừ enum constants)")
    void entitiesShouldNotHavePublicFields() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..entity..")
                .and().areNotStatic() // Exclude static fields (enum constants)
                .and().areNotFinal()  // Exclude final fields (constants)
                .should().notBePublic()
                .because("Entity fields nên là private với getter/setter");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Không sử dụng java.util.Date (nên dùng java.time)")
    void shouldNotUseJavaUtilDate() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..security..") // Allow in security package (JWT library requirement)
                .should().dependOnClassesThat()
                .belongToAnyOf(java.util.Date.class)
                .because("Nên sử dụng java.time.* thay vì java.util.Date");

        rule.allowEmptyShould(true).check(importedClasses);
    }
}
