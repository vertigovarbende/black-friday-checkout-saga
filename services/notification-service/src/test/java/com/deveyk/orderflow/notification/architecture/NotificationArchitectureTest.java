package com.deveyk.orderflow.notification.architecture;

import com.deveyk.orderflow.notification.NotificationServiceApplication;
import com.deveyk.orderflow.shared.architecture.OrderflowArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Main koda ArchUnit kuralları 1–15 ve 20–22 (01-paket-yapisi-kararlari §7).
 */
@AnalyzeClasses(packagesOf = NotificationServiceApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
class NotificationArchitectureTest {

    @ArchTest
    static final ArchTests MAIN_CODE_RULES = ArchTests.in(OrderflowArchitectureRules.MainCode.class);
}
