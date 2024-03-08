package com.bhbworkout.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Enrollment { // 등록(참가신청) 했는지 승락이 됐는지 등등 결과값
    @Id
    @GeneratedValue
    private Long id;

    /*
    *
    *  어떤 이벤트에 대한 참가 신청인지
    *
    * 이벤트에는 여러개의 참가신청을 할 수 있음
    *
    * */
    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account; //누가 신청한건지 알아야하므로.

    private LocalDateTime enrolledAt; //언제 등록한건지...선착순이면 중요함

    private boolean accepted; // 확정이냐 아니냐

    private boolean attended; // 참석했냐 안했냐
}
