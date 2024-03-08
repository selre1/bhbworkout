package com.bhbworkout.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Event { // 스터디에서 모임(event)이 있을경우

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study; // 이 모임

    @ManyToOne
    private Account createBy; // 이 모임을 누가 만들었냐

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime; // 모임을 만든 일시


    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime; //접수를 언제 종료 할지
    @Column(nullable = false)
    private LocalDateTime startDateTime; // 모임 시작 일시
    @Column(nullable = false)
    private LocalDateTime endDateTime; // 모임 종료 일시

    private int limitOfEnrollments; // 참가신청을 최대 몇개까지 받을 수 있는지

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments; // 등록 리스트 (이벤트 하나에 여러개의 등록신청이 있을 수 있음)

    @Enumerated(EnumType.STRING) //enumtype Odinary 를 하면 순서대로 입력되기 떄문에 (0,1,2), string으로 해야함
    private EventType eventType;
}
