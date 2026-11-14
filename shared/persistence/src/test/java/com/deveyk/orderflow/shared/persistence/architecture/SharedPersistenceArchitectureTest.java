package com.deveyk.orderflow.shared.persistence.architecture;

import com.deveyk.orderflow.shared.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

@AnalyzeClasses(packages = "com.deveyk.orderflow.shared.persistence", importOptions = ImportOption.DoNotIncludeTests.class)
class SharedPersistenceArchitectureTest {

    @ArchTest
    static final ArchTests MAIN_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.MainCode.class);
}
