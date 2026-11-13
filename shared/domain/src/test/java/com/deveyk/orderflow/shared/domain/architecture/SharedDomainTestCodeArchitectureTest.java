package com.deveyk.orderflow.shared.domain.architecture;

import com.deveyk.orderflow.shared.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * shared-domain testleri de test kurallarına uyar (kural 16–19).
 */
@AnalyzeClasses(packages = "com.deveyk.orderflow.shared.domain", importOptions = ImportOption.OnlyIncludeTests.class)
class SharedDomainTestCodeArchitectureTest {

    @ArchTest
    static final ArchTests TEST_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.TestCode.class);
}
