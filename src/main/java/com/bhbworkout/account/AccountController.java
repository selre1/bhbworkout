package com.bhbworkout.account;

import com.bhbworkout.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;

    private final AccountRepository accountRepository;

    @InitBinder("SignUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        model.addAttribute("signUpForm",new SignUpForm()); //"signUpForm" 지워도 카멜케이스로 작동가능
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpform, Errors error){
        if(error.hasErrors()){
            return "account/sign-up";
        }
        accountService.processNewAccount(signUpform);
        return "redirect:/";
    }

    @GetMapping("/check-email-token") // 회원가입 인증 메일 확인
    public String checkEmailToken(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if(account == null){ // 이메일이 가입됐는지 확인
            model.addAttribute("error", "wrong email");
            return view;
        }
        if(!account.getEmailCheckToken().equals(token)){ // 이메일 토큰이 같은지 확인
            model.addAttribute("error", "wrong email");
            return view;
        }
        account.completeSignUp();

        //몇번째 회원가입이 됐냐
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }
}
