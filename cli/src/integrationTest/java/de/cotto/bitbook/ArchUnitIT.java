package de.cotto.bitbook;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SliceRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;

import java.util.regex.Pattern;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

class ArchUnitIT {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter().withImportOption(new DoNotIncludeTests()).importPackages("de.cotto");
    }

    @Test
    void services_must_not_access_dto_classes_directly() {
        ArchRule rule = ArchRuleDefinition.classes().that()
                .haveSimpleNameEndingWith("Dto").or().haveSimpleNameEndingWith("DTO")
                .should()
                .onlyHaveDependentClassesThat().haveSimpleNameNotEndingWith("Service");
        assertThat(importedClasses).isNotEmpty();
        rule.check(importedClasses);
    }

    @Test
    void services_must_not_access_spring_data_repositories_directly() {
        ArchRule rule = ArchRuleDefinition.classes().that()
                .areAssignableTo(Repository.class)
                .should()
                .onlyHaveDependentClassesThat().haveSimpleNameNotEndingWith("Service");
        assertThat(importedClasses).isNotEmpty();
        rule.check(importedClasses);
    }

    @Test
    void no_package_cycle() {
        SliceRule rule = slices().matching("de.cotto.bitbook.(**)").should().beFreeOfCycles();
        assertThat(importedClasses).isNotEmpty();
        rule.check(importedClasses);
    }

    @Test
    void daos_are_transactional() {
        ArchRule rule = ArchRuleDefinition.classes().that()
                .haveSimpleNameEndingWith("DaoImpl")
                .should()
                .beAnnotatedWith("javax.transaction.Transactional")
                .orShould()
                .beAnnotatedWith("org.springframework.transaction.annotation.Transactional");
        // https://stackoverflow.com/q/26387399/947526
        assertThat(importedClasses).isNotEmpty();
        rule.check(importedClasses);
    }

    private static class DoNotIncludeTests implements ImportOption {
        private static final Pattern GRADLE_PATTERN = Pattern.compile(".*/build/classes/([^/]+/)?[a-zA-Z-]*[tT]est/.*");

        @Override
        public boolean includes(Location location) {
            return !location.matches(GRADLE_PATTERN);
        }
    }
}
