package com.deveyk.orderflow.shared.inbox.architecture;

import com.deveyk.orderflow.shared.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

@AnalyzeClasses(packages = "com.deveyk.orderflow.shared.inbox", importOptions = ImportOption.OnlyIncludeTests.class)
class SharedInboxTestCodeArchitectureTest {

    @ArchTest
    static final ArchTests TEST_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.TestCode.class);
}
