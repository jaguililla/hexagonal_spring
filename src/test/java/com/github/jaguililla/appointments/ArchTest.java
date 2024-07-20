package com.github.jaguililla.appointments;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import org.junit.jupiter.api.Test;

public class ArchTest {
    private static final String APPLICATION_PACKAGE = ArchTest.class.getPackageName();
    private static final String DOMAIN_PACKAGE = APPLICATION_PACKAGE + ".domain";
    private static final String DOMAIN_MODEL_PACKAGE = DOMAIN_PACKAGE + ".model";
    private static final String STORES_PACKAGE = APPLICATION_PACKAGE + ".output.stores..";
    private static final String NOTIFIERS_PACKAGE = APPLICATION_PACKAGE + ".output.notifiers..";
    private static final String CONTROLLERS_PACKAGE = APPLICATION_PACKAGE + ".input.controllers..";

    private static final String GENERATED_PACKAGES = APPLICATION_PACKAGE + ".http.controllers..";

    private static final String JAVA_PACKAGES = "java..";
    private static final String JAVAX_PACKAGES = "javax..";

    private static final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new DoNotIncludeTests())
        .importPackages(APPLICATION_PACKAGE);

    @Test
    void domain_can_only_access_domain() {
        classes()
            .that()
            .resideInAnyPackage(DOMAIN_PACKAGE, DOMAIN_MODEL_PACKAGE)
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(DOMAIN_PACKAGE, DOMAIN_MODEL_PACKAGE, JAVA_PACKAGES)
            .check(classes);
    }

    @Test
    void adapters_can_only_access_domain() {
        classes()
            .that()
            .resideInAnyPackage(NOTIFIERS_PACKAGE, STORES_PACKAGE)
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(
                NOTIFIERS_PACKAGE,
                STORES_PACKAGE,
                DOMAIN_PACKAGE,
                DOMAIN_MODEL_PACKAGE,
                JAVA_PACKAGES,
                JAVAX_PACKAGES,
                "org.slf4j..",
                "org.springframework..",
                "org.apache.."
            )
            .check(classes);
    }

    @Test
    void controllers_can_only_access_domain() {
        classes()
            .that()
            .resideInAPackage(CONTROLLERS_PACKAGE)
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(
                CONTROLLERS_PACKAGE,
                DOMAIN_PACKAGE,
                DOMAIN_MODEL_PACKAGE,
                GENERATED_PACKAGES,
                JAVA_PACKAGES,
                JAVAX_PACKAGES,
                "jakarta..",
                "org.slf4j..",
                "org.springframework.."
            )
            .check(classes);
    }

    @Test
    void application_can_only_access_domain_and_adapters() {
        classes()
            .that()
            .resideInAPackage(APPLICATION_PACKAGE)
            .should()
            .onlyAccessClassesThat()
            .resideInAnyPackage(
                APPLICATION_PACKAGE,
                DOMAIN_PACKAGE,
                DOMAIN_MODEL_PACKAGE,
                NOTIFIERS_PACKAGE,
                STORES_PACKAGE,
                JAVA_PACKAGES,
                JAVAX_PACKAGES,
                "org.slf4j..",
                "org.springframework..",
                "org.apache.."
            )
            .check(classes);
    }
}
