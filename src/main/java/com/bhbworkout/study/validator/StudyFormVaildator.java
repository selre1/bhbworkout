package com.bhbworkout.study.validator;

import com.bhbworkout.study.StudyRepository;
import com.bhbworkout.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormVaildator implements Validator {
    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return StudyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;

        //existsByPath 있으면 true
        if(studyRepository.existsByPath(studyForm.getPath())){
            errors.rejectValue("path","wrong.path", "해당 스터디 경로값을 사용할 수 없습니다.");
        }
    }
}
