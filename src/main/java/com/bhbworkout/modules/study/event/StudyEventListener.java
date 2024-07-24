package com.bhbworkout.modules.study.event;

import com.bhbworkout.infra.config.AppProperties;
import com.bhbworkout.infra.mail.EmailMessage;
import com.bhbworkout.infra.mail.EmailService;
import com.bhbworkout.modules.account.Account;
import com.bhbworkout.modules.account.AccountPredicates;
import com.bhbworkout.modules.account.AccountRepository;
import com.bhbworkout.modules.notification.Notification;
import com.bhbworkout.modules.notification.NotificationRepository;
import com.bhbworkout.modules.notification.NotificationType;
import com.bhbworkout.modules.study.Study;
import com.bhbworkout.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;

    private final EmailService emailService;

    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    private final NotificationRepository notificationRepository;

    /*
    * 스터디가 공개된 후 알림을 보낸다
    * */
    @EventListener
    public void handlerStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent){

        //zone과 tag를 같이 가져와야하기 떄문에 다시 조회
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());

        // 스터디를 만든 사람이 지역과 태그를 등록했으면
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));
        accounts.forEach(account -> {
            if(account.isStudyCreatedByEmail()){
                Context context = new Context();
                context.setVariable("nickname", account.getNickname());
                context.setVariable("link","/study/"+ study.getEncodedPath());
                context.setVariable("linkName", study.getTitle());
                context.setVariable("message", "새로운 스터디가 생겼습니다.");
                context.setVariable("host", appProperties.getHost());
                String message = templateEngine.process("mail/simple-link",context);

                EmailMessage emailMessage = EmailMessage.builder()
                        .subject("bhbworkout," + study.getTitle() + " 스터디에 새소식이 있습니다.")
                        .to(account.getEmail())
                        .message(message)
                        .build();

                emailService.sendEmail(emailMessage);
            }
            if(account.isStudyCreatedByWeb()){
                Notification notification = new Notification();
                notification.setTitle(study.getTitle());
                notification.setLink("/study/"+study.getEncodedPath());
                notification.setChecked(false);
                notification.setCreatedDateTime(LocalDateTime.now());
                notification.setMessage(study.getShortDescription());
                notification.setAccount(account);
                notification.setNotificationType(NotificationType.STUDY_CREAED);
                notificationRepository.save(notification);
            }
        });
    }

    @EventListener
    public void handelStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent){
        Study study = studyRepository.findStudyWithManagersAndMemversById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getMembers());
        accounts.addAll(study.getManagers());

        accounts.forEach(account -> {
            if(account.isStudyUpdatedByEmail()){
                Context context = new Context();
                context.setVariable("nickname", account.getNickname());
                context.setVariable("link","/study/"+ study.getEncodedPath());
                context.setVariable("linkName", study.getTitle());
                context.setVariable("message", studyUpdateEvent.getMessage());
                context.setVariable("host", appProperties.getHost());
                String message = templateEngine.process("mail/simple-link",context);

                EmailMessage emailMessage = EmailMessage.builder()
                        .subject("bhbworkout," + study.getTitle() + " 스터디에 새소식이 있습니다.")
                        .to(account.getEmail())
                        .message(message)
                        .build();

                emailService.sendEmail(emailMessage);
            }

            if(account.isStudyUpdatedByWeb()){
                Notification notification = new Notification();
                notification.setTitle(study.getTitle());
                notification.setLink("/study/"+study.getEncodedPath());
                notification.setChecked(false);
                notification.setCreatedDateTime(LocalDateTime.now());
                notification.setMessage(studyUpdateEvent.getMessage());
                notification.setAccount(account);
                notification.setNotificationType(NotificationType.STUDY_UPDATED);
                notificationRepository.save(notification);
            }
        });
    }
}
