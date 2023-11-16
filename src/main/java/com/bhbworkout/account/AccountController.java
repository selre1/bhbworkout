package com.bhbworkout.account;

import com.bhbworkout.domain.Account;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;

    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        model.addAttribute("signUpForm",new SignUpForm()); //"signUpForm" 지워도 카멜케이스로 작동가능
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
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
        if(!account.isValidToken(token)){ // 이메일 토큰이 같은지 확인
            model.addAttribute("error", "wrong email");
            return view;
        }

        accountService.completeSignUp(account);
        //몇번째 회원가입이 됐냐
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model){
        model.addAttribute("email", account.getEmail());
        return "account/check-eamil";
    }

    @GetMapping("/resend-eamil-token")
    public String resendEmail(@CurrentUser Account account, Model model){
        if(!account.canSendConfirmEmail()){ // 이메일이 보낸지 1시간 전이 아니면!! 즉, 1시간이 지났으면!!
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-eamil";
        }

        accountService.sendSignUpMailSender(account);
        //account.completeSignUp();
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account){
        Account byNickname = accountRepository.findByNickname(nickname);
        if(nickname == null){
            throw new IllegalIdentifierException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));

        return "account/profile";
    }
}
