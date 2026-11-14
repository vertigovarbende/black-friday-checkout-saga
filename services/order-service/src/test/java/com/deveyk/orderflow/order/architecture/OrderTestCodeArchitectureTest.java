package com.deveyk.orderflow.order.architecture;

import com.deveyk.orderflow.order.OrderServiceApplication;
import com.deveyk.orderflow.shared.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Test koduna ArchUnit kuralları 16–19 (07-test-altyapisi-kararlari §17).
 */
@AnalyzeClasses(packagesOf = OrderServiceApplication.class, importOptions = ImportOption.OnlyIncludeTests.class)
class OrderTestCodeArchitectureTest {

    @ArchTest
    static final ArchTests TEST_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.TestCode.class);
}
