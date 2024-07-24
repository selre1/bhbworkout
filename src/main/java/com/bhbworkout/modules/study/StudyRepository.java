package com.bhbworkout.modules.study;

import com.bhbworkout.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study,Long>, StudyRepositoryExtension{
    boolean existsByPath(String path);

/*
* 기본패치타입
* ~~toOne으로 끝나는건 다 eager
* ~~toMany로 끝나는건 다 lazy
*/

    // load 는 명시하면 eager 그 외 기본패치 타입 적용
    // fetch 는 명시하면 eager 그 외 lazy 타입 적용
    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagerByPath(String path);

    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

    @EntityGraph(value = "Study.withTagsAndZones", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"members","managers"}) // fetch가 기본모드
    Study findStudyWithManagersAndMemversById(Long id);

    @EntityGraph(attributePaths = {"tags","zones"})
    List<Study> findTop9ByPublishedTrueAndClosedFalseOrderByPublishedDateTimeDesc();
    List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account accountLogin, boolean b);

    List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account accountLogin, boolean b);
}
