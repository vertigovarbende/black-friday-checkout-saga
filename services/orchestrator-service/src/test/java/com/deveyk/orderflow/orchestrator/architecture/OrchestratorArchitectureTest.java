package com.deveyk.orderflow.orchestrator.architecture;

import com.deveyk.orderflow.orchestrator.OrchestratorServiceApplication;
import com.deveyk.orderflow.shared.testsupport.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Main koda ArchUnit kuralları 1–15 ve 20–22 (01-paket-yapisi-kararlari §7).
 */
@AnalyzeClasses(packagesOf = OrchestratorServiceApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
class OrchestratorArchitectureTest {

    @ArchTest
    static final ArchTests MAIN_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.MainCode.class);
}
