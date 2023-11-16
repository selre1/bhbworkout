package com.bhbworkout.settings;

import com.bhbworkout.account.AccountService;
import com.bhbworkout.account.CurrentUser;
import com.bhbworkout.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    private static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";

    private final AccountService accountService;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors, Model model, RedirectAttributes attributes){
        //@CurrentUser Account account 이 객체는 http 세션안에 넣어놨던 인증 정보안의 principal 정보임 // 상태는 detached
        //@Valid @ModelAttribute Profile profile 폼에서 받을 객체 설정시  @ModelAttribute 생략 가능
        if(errors.hasErrors()){
            //에러 발생시
            //폼에 채웠던 데이터는 자동으로 들어가고 profile
            // 에러에 대한 정보도 모델에 자동으로 들어간다 error
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }

        accountService.updateProfile(account,profile);

        // 리다이렉트로 한번만 보낼 수 있음
        // @GetMapping("/settings/profile") 에서 받을때 model에서 받음
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/" + "settings/profile"; //  @GetMapping("/settings/profile") 으로 리다이렉트
    }

    @GetMapping("/settings/password")
    public String passwordUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String passwordUpdate(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/password";
        }
        accountService.updatePassword(account,passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");

        return "redirect:" + "/settings/password";
    }
}
