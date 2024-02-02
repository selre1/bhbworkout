package com.bhbworkout.study;

import com.bhbworkout.account.CurrentUser;
import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Study;
import com.bhbworkout.study.form.StudyForm;
import com.bhbworkout.study.validator.StudyFormVaildator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController {
    private final  StudyService studyService;
    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    private final StudyFormVaildator studyFormVaildator;

    @InitBinder("StudyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormVaildator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentUser Account account, @Valid StudyForm studyForm, Errors errors){
        if(errors.hasErrors()){
            return "study/form";
        }
        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);
        //newStudy.getPath() 한글로 쓰일 수 있어서 url로 인코딩
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, @PathVariable String path, Model model){
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));

        return "study/view";
    }
}
