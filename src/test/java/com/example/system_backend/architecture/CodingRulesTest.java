package com.example.system_backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

/**
 * Test các quy tắc coding chung
 */
@DisplayName("Coding Rules Tests")
class CodingRulesTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.system_backend");
    }

    @Test
    @DisplayName("Không sử dụng generic exception")
    void shouldNotThrowGenericExceptions() {
        ArchRule rule = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
                .because("Nên sử dụng custom exception thay vì generic Exception");
        
        // Allow empty should vì có thể có class throw generic exception
        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("Không truy cập standard streams")
    void shouldNotAccessStandardStreams() {
        ArchRule rule = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Không sử dụng Java Util Logging")
    void shouldNotUseJavaUtilLogging() {
        ArchRule rule = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Không sử dụng Jodatime")
    void shouldNotUseJodatime() {
        ArchRule rule = NO_CLASSES_SHOULD_USE_JODATIME;
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controller method phải có return type")
    void controllerMethodsShouldHaveReturnType() {
        ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
                .and().arePublic()
                .should().notHaveRawReturnType(void.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Service class phải có final fields cho dependency injection")
    void servicesShouldHaveFinalFields() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..service..", "..application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .and().areNotInterfaces()
                .should().haveOnlyFinalFields()
                .because("Service nên dùng constructor injection với final fields");

        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("DTO phải nằm trong package dto")
    void dtosShouldBeInDtoPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("DTO")
                .or().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .and().resideOutsideOfPackage("..common.response..") // Exclude common response wrappers
                .should().resideInAPackage("..dto..")
                .because("DTO classes nên nằm trong package dto");

        rule.allowEmptyShould(true).check(importedClasses);
    }

    @Test
    @DisplayName("Custom exception phải extend RuntimeException")
    void customExceptionsShouldExtendRuntimeException() {
        ArchRule rule = classes()
                .that().resideInAPackage("..exception..")
                .and().haveSimpleNameEndingWith("Exception")
                .and().areNotInterfaces()
                .should().beAssignableTo(RuntimeException.class)
                .because("Custom exception nên extend RuntimeException");

        rule.allowEmptyShould(true).check(importedClasses);
    }
}
