package com.bhbworkout;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT ="..modules.event..";
    private static final String ACCOUNT ="..modules.account..";
    private static final String TAG ="..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    private static final String MAIN = "..modules.main..";


    @ArchTest
    ArchRule modulesPacageRule = classes().that().resideInAnyPackage("com.bhbworkout.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAPackage("com.bhbworkout.modules..");


    /*
    * 스터디는 자기자신이랑 이벤트에서만 접근가능해야한다.
    * */
    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage("..modules.study..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY,EVENT,MAIN);

    /*
     * 이벤트에 들어있는 것들은 자기자신, 스터디, account를 참조한다.
     * */
    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat()
            .resideInAnyPackage(STUDY,EVENT,ACCOUNT);


    /*
     * accouht에 들어있는 것들은 자기자신, 태그,존을 참조한다.
     * */
    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat()
            .resideInAnyPackage(TAG,ZONE,ACCOUNT);


    /*
    * 패키지(모듈스 전부) 조각을 낸후 슬라이스들 간의 순환참조가 있으면 안된다.
     */
    @ArchTest
    ArchRule cycleCheck = slices().matching("com.bhbworkout.modules.(*)..")
            .should().beFreeOfCycles();
}
