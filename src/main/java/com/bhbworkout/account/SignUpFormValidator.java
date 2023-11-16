package com.bhbworkout.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email","invalid", new Object[]{signUpForm.getEmail()}, "이메일이 이미 사용 중에 있습니다.");
        }

        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname", "invalid", new Object[]{signUpForm.getNickname()},"사용중인 닉네임이 있습니다");
        }
    }
}
