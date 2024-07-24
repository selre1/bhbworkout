package com.bhbworkout.modules.study;

import com.bhbworkout.modules.account.Account;
import com.bhbworkout.modules.study.event.StudyCreatedEvent;
import com.bhbworkout.modules.study.event.StudyUpdateEvent;
import com.bhbworkout.modules.study.form.StudyDescriptionForm;
import com.bhbworkout.modules.tag.Tag;
import com.bhbworkout.modules.tag.TagRepository;
import com.bhbworkout.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.HashSet;

import static com.bhbworkout.modules.study.form.StudyForm.VALID_PATH_PATTERN;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;


    /*
    * 주요 로직 응답 시간에 영향을 주기 않기 위함
    * 코드를 최대한 주요 로직에 집중하고 알림 처리 로직은 분리
    * 비동기적인 이벤트 기반 데이터 처리
    * */
    private final ApplicationEventPublisher eventPublisher;

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
        if(!study.isManagedBy(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    @Transactional
    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"스터디 소개를 수정했습니다."));
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
        Study study = studyRepository.findStudyWithTagsByPath(path);
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        // 스터디를 수정할 수 있는 권한은 매니저들이다.
        if(!study.isManagedBy(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    @Transactional
    public Study getStudyToupdateZone(Account account, String path) throws AccessDeniedException {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        if(!study.isManagedBy(account)){
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

    @Transactional
    public void publishStudy(Study study) {
        study.publish();
        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    @Transactional
    public void closeStudy(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"스터디를 종료했습니다."));
    }

    @Transactional
    public Study getStudyToUpdateStatus(Account account, String path) throws AccessDeniedException {
        Study study = studyRepository.findStudyWithManagerByPath(path);
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        if(!study.isManagedBy(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    @Transactional
    public void recruitStudy(Study study) {
        study.recruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"팀원 모집을 시작합니다."));
    }

    @Transactional
    public void stopRecruitStudy(Study study) {
        study.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"팀원 모집을 중단했습니다."));
    }

    @Transactional
    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidPath(String newPath) {
        if(!newPath.matches(VALID_PATH_PATTERN)){
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    @Transactional
    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    @Transactional
    public void remove(Study study) {
        if(study.isRemovable()){
            studyRepository.delete(study);
        }else {
            throw new IllegalArgumentException("스터디를 삭제할 수 업습니다.");
        }
    }

    @Transactional
    public void addMember(Account account, Study study) {
        study.addMember(account);
    }

    @Transactional
    public void removeMember(Account account, Study study) {
        study.removeMember(account);
    }

    @Transactional
    public Study getStudy(String path) {
        return studyRepository.findByPath(path);
    }

    @Transactional
    public Study getStudyToEnroll(String path) {
        Study study = studyRepository.findStudyOnlyByPath(path);
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return study;
    }

    public void generate(Account account) {
        for (int i=0; i<30;i++){
            String random = RandomString.make(5);
            Study study = Study.builder().title("방항배"+random)
                    .path("test-"+ random)
                    .shortDescription("ddddd")
                    .fullDescription("ddfdf")
                    .tags(new HashSet<>())
                    .managers(new HashSet<>())
                    .build();
            study.publish();
            Study newstudy = this.createNewStudy(study,account);
            Tag jpa = tagRepository.findByTitle("자바");
            newstudy.getTags().add(jpa);
        }
    }
}
