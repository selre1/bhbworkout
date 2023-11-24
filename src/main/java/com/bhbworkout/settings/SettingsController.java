package com.bhbworkout.settings;

import com.bhbworkout.account.AccountService;
import com.bhbworkout.account.CurrentUser;
import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Tag;
import com.bhbworkout.tag.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final NicknameValidator nicknameValidator;

    private final TagRepository tagRepository;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void initBinderNickname(WebDataBinder webDataBinder){
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account,Profile.class));
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

    @GetMapping("/settings/notifications")
    public String updateNotificationsForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account,Notifications.class));
        return "settings/notifications";
    }

    @PostMapping("/settings/notifications")
    public String updateNotifications(@CurrentUser Account account, @Valid @ModelAttribute Notifications notifications, Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/notifications";
        }
        accountService.updateNotifications(account,notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + "/settings/notifications";
    }

    @GetMapping("/settings/account")
    public String updateAccountForm(@CurrentUser Account account, Model model){
        model.addAttribute(model);
        model.addAttribute(modelMapper.map(account,NicknameForm.class));
        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/account";
        }
        accountService.updateAccount(account,nicknameForm);
        attributes.addFlashAttribute("message", "닉네임을 변경했습니다.");
        return "redirect:" + "/settings/account";
    }

    @GetMapping("/settings/tags")
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);


        Set<Tag> tags = accountService.getTags(account);
        //태그 스트림의 맵으로 태그들을 문자열(title이 스트링이니까)로 바뀌고 이문자열을 수집해서 리스트로 변환
        //Tag::getTitle 문자열로 바뀜
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        //화이트리스트: 자동완성 목록
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        //json 문자열로 변환
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "settings/tags";
    }

    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTags(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(title).build());
        }

        accountService.addTag(account,tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTags(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){ //없는걸 삭제하라고 하면 안되니까
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account,tag);
        return ResponseEntity.ok().build();
    }
}
