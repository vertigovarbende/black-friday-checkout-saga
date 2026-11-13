package com.deveyk.orderflow.shared.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.TryCatchBlock;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.GeneralCodingRules;

import java.util.Set;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.codeUnits;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noConstructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

public final class OrderflowArchitectureRules {

    // Package desenleri
    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String ADAPTER = "..adapter..";
    private static final String ADAPTER_IN = "..adapter.in..";
    private static final String ADAPTER_IN_WEB = "..adapter.in.web..";
    private static final String ADAPTER_OUT = "..adapter.out..";
    private static final String ADAPTER_OUT_PERSISTENCE = "..adapter.out.persistence..";

    private static final String SHARED = "com.deveyk.orderflow.shared..";
    private static final String SHARED_DOMAIN = "com.deveyk.orderflow.shared.domain..";
    private static final String SHARED_APPLICATION = "com.deveyk.orderflow.shared.application..";

    // Shared tipler (isimle)
    private static final String ENTITY = "com.deveyk.orderflow.shared.domain.Entity";
    private static final String USE_CASE_HANDLER = "com.deveyk.orderflow.shared.application.UseCaseHandler";
    private static final String ORDERFLOW_EXCEPTION = "com.deveyk.orderflow.shared.domain.exception.OrderflowException";
    private static final String EXCEPTION_FAMILY_PACKAGE = "com.deveyk.orderflow.shared.domain.exception";

    // Framework tipleri (isimle)
    private static final String SPRING_BOOT_TEST = "org.springframework.boot.test.context.SpringBootTest";
    private static final String JUNIT_TEST = "org.junit.jupiter.api.Test";
    private static final String JUNIT_PARAMETERIZED_TEST = "org.junit.jupiter.params.ParameterizedTest";
    private static final String JUNIT_DISPLAY_NAME = "org.junit.jupiter.api.DisplayName";

    private static final Set<String> GENERIC_THROWABLES = Set.of("java.lang.Throwable", "java.lang.Exception", "java.lang.RuntimeException");

    private OrderflowArchitectureRules() {
    }

    /**
     * Main koda uygulanan kurallar: 1–15 ve 20–22.
     */
    public static final class MainCode {

        private MainCode() {
        }

        // 1. Domain: JDK + sadece shared-domain. Spring, JPA, Jackson, SLF4J ve diğer shared modüller yok.
        //    "..domain.." deseni servisin kendi domain'ini ve shared-domain'i birlikte kapsar.
        @ArchTest
        public static final ArchRule DOMAIN_DEPENDS_ONLY_ON_JDK_AND_SHARED_DOMAIN = classes()
                .that().resideInAPackage(DOMAIN)
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", DOMAIN)
                .because("domain saf Java'dır; shared modüllerden sadece shared-domain'e bağımlı olabilir (kural 1)")
                .allowEmptyShould(true);

        // 2. Application → adapter yok. Shared modüllerden sadece shared-domain ve shared-application.
        @ArchTest
        public static final ArchRule APPLICATION_DOES_NOT_DEPEND_ON_ADAPTERS = noClasses()
                .that().resideInAPackage(APPLICATION)
                .should().dependOnClassesThat().resideInAPackage(ADAPTER)
                .because("application ile adapter arasındaki köprü port'lardır (kural 2)")
                .allowEmptyShould(true);

        @ArchTest
        public static final ArchRule APPLICATION_USES_ONLY_SHARED_DOMAIN_AND_APPLICATION = noClasses()
                .that().resideInAPackage(APPLICATION)
                .should().dependOnClassesThat(resideInAPackage(SHARED)
                        .and(not(resideInAnyPackage(SHARED_DOMAIN, SHARED_APPLICATION))))
                .because("shared-messaging kontratları ve diğer teknik modüller adapter'larda kalır (kural 2)")
                .allowEmptyShould(true);

        // 3. adapter.in ile adapter.out birbirine bağımlı olamaz.
        @ArchTest
        public static final ArchRule ADAPTER_IN_DOES_NOT_DEPEND_ON_ADAPTER_OUT = noClasses()
                .that().resideInAPackage(ADAPTER_IN)
                .should().dependOnClassesThat().resideInAPackage(ADAPTER_OUT)
                .because("gelen ve giden adapter'lar arasındaki köprü application katmanıdır (kural 3)")
                .allowEmptyShould(true);

        @ArchTest
        public static final ArchRule ADAPTER_OUT_DOES_NOT_DEPEND_ON_ADAPTER_IN = noClasses()
                .that().resideInAPackage(ADAPTER_OUT)
                .should().dependOnClassesThat().resideInAPackage(ADAPTER_IN)
                .because("gelen ve giden adapter'lar arasındaki köprü application katmanıdır (kural 3)")
                .allowEmptyShould(true);

        // 4. adapter.out içindeki *Adapter, *Entity, *Repository public olamaz.
        @ArchTest
        public static final ArchRule ADAPTER_OUT_INTERNALS_ARE_NOT_PUBLIC = noClasses()
                .that().resideInAPackage(ADAPTER_OUT)
                .and(haveSimpleNameEndingWithAny("Adapter", "Entity", "Repository"))
                .should().bePublic()
                .because("adapter.out dışına sadece port'lar görünür (kural 4)")
                .allowEmptyShould(true);

        // 5. İsimlendirme: handler'lar ve port'lar. Arayüzler hariç: UseCaseHandler arayüzünün kendisi
        //    shared-application'ın kök package'ındadır.
        @ArchTest
        public static final ArchRule USE_CASE_HANDLERS_IMPLEMENT_USE_CASE_HANDLER = classes()
                .that().haveSimpleNameEndingWith("UseCaseHandler").and().areNotInterfaces()
                .should().beAssignableTo(USE_CASE_HANDLER)
                .andShould().resideInAPackage("..application..handler..")
                .because("handler'lar UseCaseHandler'ı uygular ve application.<aggregate>.handler altında durur (kural 5)")
                .allowEmptyShould(true);

        @ArchTest
        public static final ArchRule PORTS_RESIDE_IN_APPLICATION_PORT_PACKAGE = classes()
                .that().haveSimpleNameEndingWith("Port").and().areInterfaces()
                .should().resideInAPackage("..application..port..")
                .because("port arayüzleri application.<aggregate>.port altında durur (kural 5)")
                .allowEmptyShould(true);

        // 6. Döngü yok. Dilim = katman + bir alt package: domain.<aggregate>, application.<aggregate>,
        //    adapter.in, adapter.out. Package seviyesinde değil, çünkü aynı aggregate'in model ve
        //    exception package'ları bilerek birbirini kullanır (Order → InvalidOrderStateException → OrderStatus).
        @ArchTest
        public static final ArchRule NO_CYCLES_BETWEEN_AGGREGATES_AND_ADAPTER_SIDES = slices()
                .matching("com.deveyk.orderflow.*.(*).(*)..")
                .namingSlices("$1.$2")
                .should().beFreeOfCycles()
                .because("aggregate'ler ve adapter yönleri arasında döngüsel bağımlılık olamaz (kural 6)")
                .allowEmptyShould(true);

        // 7. Application, Spring'den sadece stereotype ve transaction annotation'larını kullanabilir.
        @ArchTest
        public static final ArchRule APPLICATION_USES_ONLY_ALLOWED_SPRING_PACKAGES = noClasses()
                .that().resideInAPackage(APPLICATION)
                .should().dependOnClassesThat(resideInAPackage("org.springframework..")
                        .and(not(resideInAnyPackage(
                                "org.springframework.stereotype..",
                                "org.springframework.transaction.annotation.."))))
                .because("application Spring Web, Data ve Kafka'yı bilmez (kural 7, 03-solid-kararlari §D)")
                .allowEmptyShould(true);

        // 8. Field injection yok (@Autowired, @Inject, @Resource, @Value alanları).
        @ArchTest
        public static final ArchRule NO_FIELD_INJECTION = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION
                .because("sadece constructor injection kullanılır (kural 8)")
                .allowEmptyShould(true);

        // 9. Domain sınıflarının rehydrate()'i sadece adapter.out.persistence'tan çağrılabilir.
        @ArchTest
        public static final ArchRule REHYDRATE_IS_CALLED_ONLY_FROM_PERSISTENCE_ADAPTERS = noClasses()
                .that().resideOutsideOfPackage(ADAPTER_OUT_PERSISTENCE)
                .should().callMethodWhere(rehydrateOfDomainClass())
                .because("rehydrate() kural kontrolü yapmaz; sadece veritabanından okunan veri için kullanılır (kural 9)")
                .allowEmptyShould(true);

        // 10. Facade'ları sadece adapter.in.web kullanabilir.
        @ArchTest
        public static final ArchRule FACADES_ARE_USED_ONLY_BY_WEB_ADAPTERS = classes()
                .that().haveSimpleNameEndingWith("Facade").or().haveSimpleNameEndingWith("FacadeImpl")
                .should().onlyHaveDependentClassesThat().resideInAPackage(ADAPTER_IN_WEB)
                .because("facade web'e aittir; Kafka listener'ları handler'ı doğrudan çağırır (kural 10)")
                .allowEmptyShould(true);

        // 11. Domain'de public set* metodu olamaz.
        @ArchTest
        public static final ArchRule DOMAIN_HAS_NO_PUBLIC_SETTERS = noMethods()
                .that().areDeclaredInClassesThat().resideInAPackage(DOMAIN)
                .and().haveNameMatching("set[A-Z].*")
                .should().bePublic()
                .because("domain durumu sadece davranış metotlarıyla değişir (kural 11)")
                .allowEmptyShould(true);

        // 12. Entity / AggregateRoot alt sınıflarının public constructor'ı olamaz.
        @ArchTest
        public static final ArchRule ENTITIES_HAVE_NO_PUBLIC_CONSTRUCTORS = noConstructors()
                .that().areDeclaredInClassesThat().areAssignableTo(ENTITY)
                .should().bePublic()
                .because("oluşturma sadece factory method ve rehydrate() ile yapılır (kural 12)")
                .allowEmptyShould(true);

        // 13. Domain ve application'da Exception, RuntimeException, Throwable yakalanamaz.
        @ArchTest
        public static final ArchRule DOMAIN_AND_APPLICATION_DO_NOT_CATCH_GENERIC_EXCEPTIONS = codeUnits()
                .that().areDeclaredInClassesThat().resideInAnyPackage(DOMAIN, APPLICATION)
                .should(notCatchGenericThrowables())
                .because("exception'lar global handler'a ve Kafka error handler'ına kadar çıkar (kural 13)")
                .allowEmptyShould(true);

        // 14. OrderflowException'dan doğrudan sadece shared-domain'deki abstract aileler türeyebilir.
        @ArchTest
        public static final ArchRule ONLY_SHARED_FAMILIES_EXTEND_ORDERFLOW_EXCEPTION_DIRECTLY = classes()
                .that(directlyExtend(ORDERFLOW_EXCEPTION))
                .should().resideInAPackage(EXCEPTION_FAMILY_PACKAGE)
                .andShould().haveModifier(JavaModifier.ABSTRACT)
                .because("her exception altı aileden birine aittir; HTTP ve retry davranışı aileden gelir (kural 14)")
                .allowEmptyShould(true);

        // 15. Somut exception'lar final'dır ve domain..exception ya da adapter altında durur.
        @ArchTest
        public static final ArchRule CONCRETE_EXCEPTIONS_ARE_FINAL_AND_PLACED = classes()
                .that().areAssignableTo(ORDERFLOW_EXCEPTION)
                .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                .should().haveModifier(JavaModifier.FINAL)
                .andShould().resideInAnyPackage("..domain..exception..", ADAPTER)
                .because("somut exception'lar genişletilmez ve sahibinin yanında durur (kural 15)")
                .allowEmptyShould(true);

        // 20. System.out, System.err, java.lang.IO ve printStackTrace() yok.
        @ArchTest
        public static final ArchRule NO_STANDARD_STREAMS = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                .because("loglar SLF4J ile yazılır ve structured olarak stdout'a gider (kural 20)")
                .allowEmptyShould(true);

        // 21. Domain log yazmaz. (Kural 1 bunu da kapsar; ayrı kural daha açık bir hata mesajı verir.)
        @ArchTest
        public static final ArchRule DOMAIN_DOES_NOT_LOG = noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat().resideInAPackage("org.slf4j..")
                .because("domain log yazmaz; logu handler ve adapter'lar yazar (kural 21)")
                .allowEmptyShould(true);

        // 22. Domain ve application MDC'ye dokunmaz.
        @ArchTest
        public static final ArchRule DOMAIN_AND_APPLICATION_DO_NOT_USE_MDC = noClasses()
                .that().resideInAnyPackage(DOMAIN, APPLICATION)
                .should().dependOnClassesThat().haveNameMatching("org\\.slf4j\\.MDC(\\$.*)?")
                .because("MDC'yi sadece sınırlar yazar: filtre, inbox, zamanlanmış görevler (kural 22)")
                .allowEmptyShould(true);
    }

    public static final class TestCode {

        private TestCode() {
        }

        // 16. *IT sınıfları bir *TestContainerConfiguration ya da OrderflowEndToEndTest'ten türer.
        @ArchTest
        public static final ArchRule INTEGRATION_TESTS_EXTEND_CONTAINER_BASE = classes()
                .that().haveSimpleNameEndingWith("IT").and().areTopLevelClasses()
                .should(extendAnIntegrationTestBase())
                .because("container'lar ve ortak ayarlar taban sınıftan gelir (kural 16)")
                .allowEmptyShould(true);

        // 17. *Test sınıfları Spring context'i ya da container açmaz (Surefire / Failsafe ayrımı).
        @ArchTest
        public static final ArchRule UNIT_TESTS_DO_NOT_START_SPRING_BOOT_OR_CONTAINERS = noClasses()
                .that().haveSimpleNameEndingWith("Test")
                .should(beOrInheritFromClassMetaAnnotatedWith(SPRING_BOOT_TEST))
                .orShould().dependOnClassesThat().resideInAPackage("org.testcontainers..")
                .because("*Test Docker'sız ve hızlı koşar; context + container isteyen test *IT olur (kural 17)")
                .allowEmptyShould(true);

        // 18. Test kodunda Thread.sleep / TimeUnit.sleep yok.
        @ArchTest
        public static final ArchRule NO_SLEEP_IN_TESTS = noClasses()
                .should().callMethodWhere(sleepCall())
                .because("asenkron sonuçlar Awaitility ile beklenir (kural 18)")
                .allowEmptyShould(true);

        // 19. @Test ve @ParameterizedTest metotlarında @DisplayName zorunlu.
        @ArchTest
        public static final ArchRule TEST_METHODS_HAVE_DISPLAY_NAME = methods()
                .that().areAnnotatedWith(JUNIT_TEST).or().areAnnotatedWith(JUNIT_PARAMETERIZED_TEST)
                .should().beAnnotatedWith(JUNIT_DISPLAY_NAME)
                .because("test raporları Türkçe, tam cümle okunur (kural 19)")
                .allowEmptyShould(true);
    }

    // ---------- yardımcı predicate / condition'lar

    private static DescribedPredicate<JavaClass> haveSimpleNameEndingWithAny(final String... suffixes) {
        return describe("have simple name ending with any of " + String.join(", ", suffixes), javaClass -> {
            for (final String suffix : suffixes) {
                if (javaClass.getSimpleName().endsWith(suffix)) {
                    return true;
                }
            }
            return false;
        });
    }

    private static DescribedPredicate<JavaClass> directlyExtend(final String superclassName) {
        return describe("directly extend " + superclassName, javaClass -> javaClass.getRawSuperclass()
                .map(superclass -> superclass.getName().equals(superclassName))
                .orElse(false));
    }

    private static DescribedPredicate<JavaMethodCall> rehydrateOfDomainClass() {
        final DescribedPredicate<JavaClass> inDomain = resideInAPackage(DOMAIN);
        return describe("rehydrate() of a domain class", call ->
                call.getName().equals("rehydrate") && inDomain.test(call.getTargetOwner()));
    }

    private static DescribedPredicate<JavaMethodCall> sleepCall() {
        return describe("Thread.sleep(..) or TimeUnit.sleep(..)", call ->
                call.getName().equals("sleep") && (call.getTargetOwner().isEquivalentTo(Thread.class)
                        || call.getTargetOwner().isEquivalentTo(java.util.concurrent.TimeUnit.class)));
    }

    private static ArchCondition<JavaCodeUnit> notCatchGenericThrowables() {
        return new ArchCondition<>("not catch Exception, RuntimeException or Throwable") {
            @Override
            public void check(final JavaCodeUnit codeUnit, final ConditionEvents events) {
                for (final TryCatchBlock block : codeUnit.getTryCatchBlocks()) {
                    for (final JavaClass caught : block.getCaughtThrowables()) {
                        if (GENERIC_THROWABLES.contains(caught.getName())) {
                            events.add(SimpleConditionEvent.violated(codeUnit, String.format(
                                    "%s catches %s in %s",
                                    codeUnit.getFullName(), caught.getSimpleName(), block.getSourceCodeLocation())));
                        }
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> extendAnIntegrationTestBase() {
        return new ArchCondition<>("extend a *TestContainerConfiguration or OrderflowEndToEndTest") {
            @Override
            public void check(final JavaClass javaClass, final ConditionEvents events) {
                final boolean extendsBase = javaClass.getAllRawSuperclasses().stream()
                        .map(JavaClass::getSimpleName)
                        .anyMatch(name -> name.endsWith("TestContainerConfiguration")
                                || name.equals("OrderflowEndToEndTest"));
                if (!extendsBase) {
                    events.add(SimpleConditionEvent.violated(javaClass, String.format(
                            "%s does not extend a *TestContainerConfiguration or OrderflowEndToEndTest",
                            javaClass.getName())));
                }
            }
        };
    }

    // @SpringBootTest sınıfın kendisinde, onu taşıyan bir annotation'da ya da bir üst sınıfta olabilir.
    private static ArchCondition<JavaClass> beOrInheritFromClassMetaAnnotatedWith(final String annotationName) {
        return new ArchCondition<>("be (or inherit from a class) meta-annotated with @" + simpleName(annotationName)) {
            @Override
            public void check(final JavaClass javaClass, final ConditionEvents events) {
                final boolean annotated = javaClass.getClassHierarchy().stream()
                        .anyMatch(type -> type.isMetaAnnotatedWith(annotationName));
                events.add(new SimpleConditionEvent(javaClass, annotated, String.format(
                        "%s is %smeta-annotated with @%s",
                        javaClass.getName(), annotated ? "" : "not ", simpleName(annotationName))));
            }
        };
    }

    private static String simpleName(final String fullyQualifiedName) {
        return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
    }
}
