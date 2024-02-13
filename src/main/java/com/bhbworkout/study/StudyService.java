package com.bhbworkout.study;

import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Study;
import com.bhbworkout.domain.Tag;
import com.bhbworkout.domain.Zone;
import com.bhbworkout.study.form.StudyDescriptionForm;
import com.bhbworkout.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;

    @Transactional
    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    /*
    * 스터디 수정하기
    * */
    @Transactional
    public Study getStudyToUpdate(Account account, String path) throws AccessDeniedException {
        Study study = studyRepository.findByPath(path);
        //스터디가 있는지 확인
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }

        // 스터디를 수정할 수 있는 권한은 매니저들이다.
        if(!account.isManagerOf(study)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    @Transactional
    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
    }

    @Transactional
    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    @Transactional
    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    @Transactional
    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    @Transactional
    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    @Transactional
    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    @Transactional
    public Study getStudyToUpdateTag(Account account, String path) throws AccessDeniedException {
        Study study = studyRepository.findAccountWithTagsByPath(path);
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        // 스터디를 수정할 수 있는 권한은 매니저들이다.
        if(!account.isManagerOf(study)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    @Transactional
    public Study getStudyToupdateZone(Account account, String path) throws AccessDeniedException {
        Study study = studyRepository.findAccountWithZonesByPath(path);
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        if(!account.isManagerOf(study)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    @Transactional
    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    @Transactional
    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }
}
