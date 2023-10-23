package com.bhbworkout.account;

import com.bhbworkout.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;

    public void processNewAccount(SignUpForm signUpform) {
        Account newAccount = saveNewAccount(signUpform);
        newAccount.generateEmailCheckToken();
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
        Account newAccount = accountRepository.save(account);
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
