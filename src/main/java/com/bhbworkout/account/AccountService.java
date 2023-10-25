package com.bhbworkout.account;

import com.bhbworkout.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void processNewAccount(SignUpForm signUpform) {
        Account newAccount = saveNewAccount(signUpform);
        newAccount.generateEmailCheckToken();// 위의 트랙젝션안에 있어야 persist 상태가 됌
        sendSignUpMailSender(newAccount);
    }
    private Account saveNewAccount(SignUpForm signUpform) {
        Account account = Account.builder()
                .email(signUpform.getEmail())
                .nickname(signUpform.getNickname())
                .password(passwordEncoder.encode(signUpform.getPassword()))
                .studyUpdatedResultByWeb(true)
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .build();
        Account newAccount = accountRepository.save(account); // 여기서는 트랜잭션 (엔티티가 persist 영속성임)
        return newAccount;
    }

    private void sendSignUpMailSender(Account newAccount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(newAccount.getEmail());
        message.setSubject("스터디, 회원가입");
        message.setText("/check-email-token?token="+ newAccount.getEmailCheckToken()+
                "&email=" + newAccount.getEmail());
        javaMailSender.send(message);
    }
}
