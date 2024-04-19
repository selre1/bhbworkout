package com.bhbworkout.domain;

import com.bhbworkout.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Event { // 스터디에서 모임(event)이 있을경우

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study; // 어디 모임에 대한 이벤트인지? 맞나?


    // properties ddl auto = update 상태에서 아래 변수명을 변경할 경우 컬럼이 추가가 됌
    // 그러므로 컬럼이 추가가 된상태에서 데이터 옮기고 기존 컬럼은 삭제'
    // **** 변수명 바뀜과 동시에 컬럼명 바뀌려면 마이그레이션 해야함 (flyway)
    @ManyToOne
    private Account createdBy; // 이 모임을 누가 만들었냐

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

    public boolean isEnrollableFor(UserAccount userAccount){
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount){
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    public boolean isAttended(UserAccount userAccount){
        for (Enrollment enrollment : this.enrollments){
            if(enrollment.getAccount().equals(userAccount) && enrollment.isAttended()){
                return true;
            }
        }
        return false;
    }

    public boolean canAccept(Enrollment enrollment){
        //관리자가 확인해야하는 모임이고
        //참석하지 않았고
        //수락하지 않았으면
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAccepted()
                && !enrollment.isAttended();
    }

    public boolean canReject(Enrollment enrollment){

        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }

    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        for(Enrollment en : this.enrollments){
            if(en.getAccount().equals(userAccount.getAccount())){
                return true;
            }
        }
        return false;
    }

    public int numberOfRemainSpots(){
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }

    public boolean isAbleToAcceptWaitingEnrollment() {

        // 선착순이고 현재 허용인원이 확정된인원보다 크면(자리가 있으면)
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    public Enrollment getTheFirstWaitingEnrollment() {
        for (Enrollment e : this.enrollments){
            if(!e.isAccepted()){
                return e;
            }
        }
        return null;
    }

    public void acceptWaitingList() {
        /*
        //늘린 인원 - 확정된 인원 == 남은자리 2   3   4
        // 기다리는 신청서 1  5 4
        // 남은자리 - 기다리는 신청서  == 양수면(남는 자리가 많다는 것이므로 모두 확정상태로 만듬)
        // 남은자리 - 기다리는 신청서  == 음수면(남는 자리가 없다는 것이므로 남는 자리만 확정상태로 만든)
        List<Enrollment> waitingEnrollmentList =  this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
        int remaining = this.limitOfEnrollments - this.getLimitOfEnrollments();
        int checkNum = remaining - waitingEnrollmentList.size();

        if(checkNum >= 0){
            waitingEnrollmentList.forEach(enrollment -> enrollment.setAccepted(true));
        }else{
            int count = 0;
            for(Enrollment enrollment : waitingEnrollmentList){
                if(count < remaining){
                    enrollment.setAccepted(true);
                }
                count++;
            }
        }
        */


        if(this.isAbleToAcceptWaitingEnrollment()){
            List<Enrollment> waitingEnrollmentList =  this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
            int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.getNumberOfAcceptedEnrollments(), waitingEnrollmentList.size());
            waitingEnrollmentList.subList(0,numberToAccept).forEach(e -> e.setAccepted(true));
        }
    }
}
