package com.bhbworkout.study;

import com.bhbworkout.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study,Long> {
    boolean existsByPath(String path);


    //~~toOne으로 끝나는건 다 eager
    //~~toMany로 끝나는건 다 lazy
    // load 는 명시하면 eager 그 외 기본패치 타입 적용
    // fetch 는 명시하면 eager 그 외 lazy 타입 적용
    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);
}
