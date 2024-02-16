package com.bhbworkout.study;

import com.bhbworkout.account.CurrentUser;
import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Study;
import com.bhbworkout.domain.Tag;
import com.bhbworkout.domain.Zone;
import com.bhbworkout.study.form.StudyDescriptionForm;
import com.bhbworkout.study.tag.TagForm;
import com.bhbworkout.study.zone.ZoneForm;
import com.bhbworkout.tag.TagRepository;
import com.bhbworkout.tag.TagService;
import com.bhbworkout.zone.ZoneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/{path}/settings")
public class StudySettingsController {
    private final StudyService studyService;
    private final ModelMapper modelMapper;

    private final TagRepository tagRepository;

    private final ObjectMapper objectMapper;

    private final TagService tagService;
    private final ZoneRepository zoneRepository;

    @GetMapping("/description")
    public String viewStudySetting(@CurrentUser Account account, @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm studyDescriptionForm, Errors errors, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        //서비스의 트랜잭셔널 안에서 가져온거라 persis상태임
        Study study = studyService.getStudyToUpdate(account,path);
        if(errors.hasErrors()){
            //studyDescriptionForm, errors 는 모델에 기본으로 담아준다
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디를 수정했습니다.");
        return "redirect:/study/"+ getPath(path) + "/settings/description";
    }

    @GetMapping("/banner")
    public String viewStudyBanner(@CurrentUser Account account, @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(study);
        model.addAttribute(account);
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String updateStudyBanner(@CurrentUser Account account, @PathVariable String path,
                                    String image, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        studyService.updateStudyImage(study,image);
        attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");
        return "redirect:/study/"+ getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/"+ getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/"+ getPath(path) + "/settings/banner";
    }

    @GetMapping("/tags")
    public String viewStudyTags(@CurrentUser Account account, @PathVariable String path, Model model) throws AccessDeniedException, JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);

        // 스터디가 가지고 있는 태그 정보
        model.addAttribute("tags",study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        // 전체 태그 리스트(자동완성에 쓰려고)
        List<String> allTagTitle = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTagTitle));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm) throws AccessDeniedException {
        //url에 해당하는 스터디를 찾고 매니저 권한 추가, 전부다 가져올 필요 없으므로 필요한것만 가져오기(entitygraph)
        Study study = studyService.getStudyToUpdateTag(account,path);
        //태그를 찾고, 만약 없으면 만들어서 리턴하고
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        //변경(수정)을 할 때, 트랜잭션 안에서 해야함!  현재 study,tag는 persis 상태임 (osiv)
        studyService.addTag(study,tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateTag(account,path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if(tag == null){
            ResponseEntity.badRequest().build();
        }
        studyService.removeTag(study,tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String viewStudyZones(@CurrentUser Account account, @PathVariable String path, Model model) throws AccessDeniedException, JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);

        List<String> zones = study.getZones().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("zones",zones);
        List<String> allZones =  zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allZones));
        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZones(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) throws AccessDeniedException {
        Study study = studyService.getStudyToupdateZone(account,path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName());
        if(zone == null){
            ResponseEntity.badRequest().build();
        }
        studyService.addZone(study,zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZones(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) throws AccessDeniedException {
        Study study = studyService.getStudyToupdateZone(account,path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(),zoneForm.getProvinceName());
        if(zone == null){
            ResponseEntity.badRequest().build();
        }
        studyService.removeZone(study,zone);
        return ResponseEntity.ok().build();
    }
    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/study")
    public String viewStudy(@CurrentUser Account account, @PathVariable String path, Model model) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdate(account,path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.publishStudy(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다");
        return "redirect:/study/"+getPath(path)+"/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.closeStudy(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다");
        return "redirect:/study/"+getPath(path)+"/settings/study";
    }

    @PostMapping("/recruit/start")
    public String recruitStudy(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!study.canRecruitStudy()){
            attributes.addFlashAttribute("message", "스터디 모집을 1시간 이후 다시 시도하세요");
            return "redirect:/study/"+getPath(path)+"/settings/study";
        }

        studyService.recruitStudy(study);
        attributes.addFlashAttribute("message", "스터디 모집을 시작했습니다.");
        return "redirect:/study/"+getPath(path)+"/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruitStudy(@CurrentUser Account account, @PathVariable String path, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!study.canRecruitStudy()){
            attributes.addFlashAttribute("message", "스터디 중지를 1시간 이후 다시 시도하세요");
            return "redirect:/study/"+getPath(path)+"/settings/study";
        }
        studyService.stopRecruitStudy(study);
        attributes.addFlashAttribute("message", "스터디 모집을 종료했습니다.");
        return "redirect:/study/"+getPath(path)+"/settings/study";
    }

    @PostMapping("/study/path")
    public String updateStudyPath(@CurrentUser Account account, @PathVariable String path, String newPath, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        // @RequestParam은 생략이 가능하다!!
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!studyService.isValidPath(newPath)){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "해당 스터디 경로는 사용할 수 없습니다.");
            return "study/settings/study";
        }
        studyService.updateStudyPath(study,newPath);
        attributes.addFlashAttribute("message", "경로 업데이트 완료했습니다.");
        return "redirect:/study/"+getPath(newPath)+"/settings/study";
    }

    @PostMapping("/study/title")
    public String updateStudyTitle(@CurrentUser Account account, @PathVariable String path, String newTitle, Model model, RedirectAttributes attributes) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(!studyService.isValidTitle(newTitle)){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "해당 스터디 경로는 사용할 수 없습니다.");
            return "study/settings/study";
        }
        studyService.updateStudyTitle(study,newTitle);
        attributes.addFlashAttribute("message", "이름 업데이트 완료했습니다.");
        return "redirect:/study/"+getPath(path)+"/settings/study";
    }

    @PostMapping("/study/remove")
    public String removeStudy(@CurrentUser Account account, @PathVariable String path) throws AccessDeniedException {
        Study study = studyService.getStudyToUpdateStatus(account,path);
        studyService.remove(study);
        return "redirect:/";
    }
}
