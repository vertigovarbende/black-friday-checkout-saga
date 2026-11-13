package com.deveyk.orderflow.shared.application.architecture;

import com.deveyk.orderflow.shared.testsupport.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * shared-application de application kurallarına uyar: Spring Web / Data / Kafka yok, adapter yok,
 * shared modüllerden sadece shared-domain (kural 1–15, 20–22).
 */
@AnalyzeClasses(packages = "com.deveyk.orderflow.shared.application", importOptions = ImportOption.DoNotIncludeTests.class)
class SharedApplicationArchitectureTest {

    @ArchTest
    static final ArchTests MAIN_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.MainCode.class);
}
