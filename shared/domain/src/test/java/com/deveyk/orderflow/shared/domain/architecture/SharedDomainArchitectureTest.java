package com.deveyk.orderflow.shared.domain.architecture;

import com.deveyk.orderflow.shared.testsupport.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * shared-domain de domain kurallarına uyar: saf Java, setter yok, public constructor yok,
 * aileler abstract, somut exception'lar final (kural 1–15, 20–22).
 */
@AnalyzeClasses(packages = "com.deveyk.orderflow.shared.domain", importOptions = ImportOption.DoNotIncludeTests.class)
class SharedDomainArchitectureTest {

    @ArchTest
    static final ArchTests MAIN_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.MainCode.class);
}
