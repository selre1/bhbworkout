package com.bhbworkout.event.validator;

import com.bhbworkout.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        /*
        * 먼저 모집을하고
        *
        * 모임을 시작해야함!!!***
        * */



        if(eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now())){ // 모임모집시간을 현재시간보다 과거의 시간을 선택했으면 에러 표시해야함
            errors.rejectValue("endEnrollmentDateTime","wrong.datetime","모임 접수 종료일시를 정확히 입력하세요.");
        }

        if(isaBoolean(eventForm)){
            errors.rejectValue("endDateTime","wrong.datetime","모임 종료 일시를 정확히 입력하세요.");
        }

        if(eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime())){ //모임 시작 시간을 모집시간보다 전에 하면 에러
            errors.rejectValue("startDateTime","wrong.datetime","모임 시작 일시를 정확히 입력하세요.");
        }
    }

    private boolean isaBoolean(EventForm eventForm) {
        // 이벤트 종료시간이 시작시간보다 이전이면 안되고
        // 이벤트 종료시간이 접수종료시간보다 이전이면 안된다
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || eventForm.getEndDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }
}
