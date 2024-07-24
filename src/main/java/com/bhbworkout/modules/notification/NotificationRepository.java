package com.bhbworkout.modules.notification;

import com.bhbworkout.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    long countByAccountAndChecked(Account account, boolean checked);


    /*
    * 여기만 트랜젝선 readOnly가 아닌 이유
    * 알림 checked를 변경해줘야하기 떄문
    *
    * */
    @Transactional
    List<Notification> findByAccountAndCheckedOrderByCreatedDateTimeDesc(Account account, boolean b);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean b);
}
