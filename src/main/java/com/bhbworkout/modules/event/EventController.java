package com.bhbworkout.modules.event;

import com.bhbworkout.modules.account.CurrentUser;
import com.bhbworkout.modules.account.Account;
import com.bhbworkout.modules.study.Study;
import com.bhbworkout.modules.event.form.EventForm;
import com.bhbworkout.modules.event.validator.EventValidator;
import com.bhbworkout.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    private final EventRepository eventRepository;

    private final EnrollmentRepository enrollmentRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model, @Valid EventForm eventForm, Errors errors) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            //model.addAttribute(eventForm); //생략해도 됌. 폼에 담긴 데이터랑 검증시 발생됬던 에러는 기본으로 모델(model)에 담김
            return "event/form";
        }
        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model){
        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(id).orElseThrow());
        model.addAttribute(studyService.getStudy(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String getEvents(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudy(path);
        model.addAttribute(study);
        model.addAttribute(account);

        List<Event> eventList = eventService.getEvents(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        eventList.forEach(e ->{
            if(e.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(e);
            }else{
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents",newEvents);
        model.addAttribute("oldEvents",oldEvents);

        return "study/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        Event event = eventRepository.findById(id).orElseThrow();
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, @Valid EventForm eventForm, Errors errors,Model model) throws AccessDeniedException {
        //중요!!!!
        //BindingResult나 Errors는 바인딩 받는 객체 바로 다음에 선언해야함
        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventForm.setEventType(event.getEventType()); // 클라이언트에서 모집방법 입력폼은 없지만 악의적으로 넣을 수도 있다! 그래서 기존값을 덮어씀

        // 폼에 모집인원이 확정된신청인 보다 작으면 에러 표출
        if(eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()){
            errors.rejectValue("limitOfEnrollments", "wrong.value","확인된 참가 신청보다 모집인원 수가 커야 합니다.");
        }
        if(errors.hasErrors()){
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event,eventForm);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @DeleteMapping("/events/{id}")
    public String cancelEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        eventService.deleteEvent(id);
        return "redirect:/study/"+ study.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id){
        //******중요!!!
        //기존에는 관리자(매니저) 권한인지 확인하려고 entity graph로 account 정보 다 가져옴
        //하지만 이번 요청는 관리자가 아니여도 써야함
        // (한마디로 관리자 확인 할 필요도 없고 다른 연관관계도 필요없음 --> lazy)
        // ~~toMany는 lazy로 가져옴
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(id).orElseThrow(),account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + id;
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id){
        Study study = studyService.getStudyToEnroll(path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventService.cancelEnrollment(event,account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + id;
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);


        // 리팩토링 팁!!!!!
        // @PathVariable("eventId") Event event   ====>> Event event = eventRepository.findById(eventId).orElseThrow();
        // @PathVariable("enrollmentId") Enrollment enrollment ====> Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.acceptEnrollment(event,enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long eventId, @PathVariable Long enrollmentId) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.rejectEnrollment(event,enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long eventId, @PathVariable Long enrollmentId) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.checkInEnrollment(enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkOut")
    public String checkOutEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable Long eventId, @PathVariable Long enrollmentId) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.checkOutEnrollment(enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }
}

