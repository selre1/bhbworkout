package com.bhbworkout.modules.event;

import com.bhbworkout.modules.account.Account;
import com.bhbworkout.modules.event.event.EnrollmentAcceptedEvent;
import com.bhbworkout.modules.event.event.EnrollmentRejectedEvent;
import com.bhbworkout.modules.event.form.EventForm;
import com.bhbworkout.modules.study.Study;
import com.bhbworkout.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    private final EnrollmentRepository enrollmentRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account); // 누가 만드는거냐?
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), event.getTitle() + " 모임을 만들었습니다."));
        return eventRepository.save(event);
    }

    public List<Event> getEvents(Study study) {
        return eventRepository.findByStudyOrderByStartDateTime(study);
    }

    @Transactional
    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm,event);

        // 이벤트 폼 수정시
        // 모집인원을 늘린 선착순 모임의 경우, 자동으로 추가 인원의 참가 신청을 확정 상태로 변경해야한다.(대기 인원이 있다면 참가 확정 상태로 변경해야함)
        event.acceptWaitingList();
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), event.getTitle() + " 모임정보를 수정했으니 확인하세요"));
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id).orElseThrow();
        eventRepository.delete(event);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), event.getTitle() + " 모임으 취소했습니다."));
    }

    @Transactional
    public void newEnrollment(Event event, Account account) {
        // 이 모임(이벤트)에 대한 해당 유저에 신청서가 없으면
        if(!enrollmentRepository.existsByEventAndAccount(event,account)){
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            //모임에 신청하자마자 확정상태 만들어주는 부분
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    @Transactional
    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event,account);
        if(!enrollment.isAttended()){
            //양방향 관계 매핑된 상태에서 삭제할때는
            //해당 신청서를 가져와서 모임(이벤트)에서 빼주고 관계해제 후 삭제
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);

            //중요 !!!
            //기존에 확정됐던 사용자가 나가면서 다음 대기사용자를 확정시켜야함

            // 1. 우선 더 추가로 받을 수 있는지 확인
            if(event.isAbleToAcceptWaitingEnrollment()){
                // 2. 첫번째 대기 신청서 가져온다
                Enrollment waitingEnrollment = event.getTheFirstWaitingEnrollment();
                // 3. 대기 신청서가 있는지 확인하고 있으면 확정
                if(waitingEnrollment != null){
                    waitingEnrollment.setAccepted(true);
                }
            }
        }
    }

    @Transactional
    public void acceptEnrollment(Event event, Enrollment enrollment) {
        if(event.getEventType() == EventType.CONFIRMATIVE && event.getLimitOfEnrollments() > event.getNumberOfAcceptedEnrollments()){
            enrollment.setAccepted(true);
        }
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    @Transactional
    public void rejectEnrollment(Event event, Enrollment enrollment) {
        if(event.getEventType() == EventType.CONFIRMATIVE && event.getLimitOfEnrollments() > event.getNumberOfAcceptedEnrollments()){
            enrollment.setAccepted(false);
        }
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    @Transactional
    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    @Transactional
    public void checkOutEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
