package com.bhbworkout.event;

import com.bhbworkout.account.CurrentUser;
import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Event;
import com.bhbworkout.domain.Study;
import com.bhbworkout.event.form.EventForm;
import com.bhbworkout.event.validator.EventValidator;
import com.bhbworkout.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        model.addAttribute(study);
        model.addAttribute(study);
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
}

