package com.bhbworkout.modules.notification;

import com.bhbworkout.modules.account.Account;
import com.bhbworkout.modules.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class notificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentUser Account account, Model model){
        List<Notification> notificationList = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account,false);
        Long numberOfChecked = notificationRepository.countByAccountAndChecked(account,true);

        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingStudyNotifications = new ArrayList<>();

        for(var notification : notificationList){
            switch (notification.getNotificationType()){
                case STUDY_CREAED: newStudyNotifications.add(notification); break;
                case EVENT_ENROLLMENT:eventEnrollmentNotifications.add(notification); break;
                case STUDY_UPDATED:watchingStudyNotifications.add(notification); break;
            }
        }
        model.addAttribute("numberOfNotChecked", notificationList.size());
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
        model.addAttribute("notifications",notificationList);

        model.addAttribute("isNew", true);
        notificationService.markAsRead(notificationList);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentUser Account account, Model model){
        List<Notification> notificationList = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account,true);
        Long numberOfNotChecked = notificationRepository.countByAccountAndChecked(account,false);

        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingStudyNotifications = new ArrayList<>();

        for(var notification : notificationList){
            switch (notification.getNotificationType()){
                case STUDY_CREAED: newStudyNotifications.add(notification); break;
                case EVENT_ENROLLMENT:eventEnrollmentNotifications.add(notification); break;
                case STUDY_UPDATED:watchingStudyNotifications.add(notification); break;
            }
        }
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", notificationList.size());
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
        model.addAttribute("notifications",notificationList);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentUser Account account){
        notificationRepository.deleteByAccountAndChecked(account,true);
        return "redirect:/notifications";
    }
}
