package com.deveyk.orderflow.product.architecture;

import com.deveyk.orderflow.product.ProductServiceApplication;
import com.deveyk.orderflow.shared.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Main koda ArchUnit kuralları 1–15 ve 20–22 (01-paket-yapisi-kararlari §7).
 */
@AnalyzeClasses(packagesOf = ProductServiceApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
class ProductArchitectureTest {

    @ArchTest
    static final ArchTests MAIN_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.MainCode.class);
}
