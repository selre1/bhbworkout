package com.bhbworkout.modules.main;

import com.bhbworkout.modules.account.Account;
import com.bhbworkout.modules.account.AccountRepository;
import com.bhbworkout.modules.account.CurrentUser;
import com.bhbworkout.modules.event.EnrollmentRepository;
import com.bhbworkout.modules.study.Study;
import com.bhbworkout.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController{

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {

        /*
        * 로그인한 사용자
        *
        * */
        if(account != null){
            // @currentUser는 detached 상태임
            // tag와 zone을 가져오기위해서 새로 조회함
            Account accountLogin = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLogin);
            /*
            * 모임목록 가져오기 (자기가 참가신청 했고 수락된 것)
            * */
            model.addAttribute("enrollmentList",enrollmentRepository.findByAccountAndAcceptedTrueOrderByEnrolledAtDesc(accountLogin));
            model.addAttribute("studyList",studyRepository.findByAccount(accountLogin.getTags(),accountLogin.getZones()));
            model.addAttribute("studyManagerOf",studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(accountLogin,false));
            model.addAttribute("studyMemberOf",studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(accountLogin,false));
            return "index-after-login";
        }
        /*
        * 로그인 하지 않은 사용자를 위함
        * */
        List<Study> studyList = studyRepository.findTop9ByPublishedTrueAndClosedFalseOrderByPublishedDateTimeDesc();
        model.addAttribute("studyList",studyList);
        return "index";
    }

    @GetMapping("/login")
    public String login(){

        return "login"; // templates/login.html
    }

    @GetMapping("/search/study") //size, page, sort
    public String searchStudy(@PageableDefault(size = 9, page = 0, sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable, String keyword, Model model){
        Page<Study> studyPage = studyRepository.findByKeyword(keyword,pageable);
        model.addAttribute("studyPage",studyPage);
        model.addAttribute("keyword",keyword);
        /*
        * 페이저블 정렬에 publishedDateTime 있으면, publishedDateTime
        * publishedDateTime 없으면, memberCount로 정렬
        * */
        model.addAttribute("sortProperty",pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }
}
