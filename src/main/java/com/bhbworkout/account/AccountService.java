package com.bhbworkout.account;

import com.bhbworkout.domain.Account;
import com.bhbworkout.settings.NicknameForm;
import com.bhbworkout.settings.Notifications;
import com.bhbworkout.settings.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    @Transactional
    public Account processNewAccount(SignUpForm signUpform) {
        Account newAccount = saveNewAccount(signUpform);
        newAccount.generateEmailCheckToken();// 위의 트랙젝션안에 있어야 persist 상태가 됌
        sendSignUpMailSender(newAccount);
        return newAccount;
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

    public void sendSignUpMailSender(Account newAccount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(newAccount.getEmail());
        message.setSubject("스터디, 회원가입");
        message.setText("/check-email-token?token="+ newAccount.getEmailCheckToken()+
                "&email=" + newAccount.getEmail());
        javaMailSender.send(message);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
          new UserAccount(account),
          account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(token);

        //Authentication authentication = authenticationManager.authenticate(token);
        //SecurityContext context = SecurityContextHolder.getContext();
        //context.setAuthentication(authentication);
    }


    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account == null){
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if(account == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    @Transactional
    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile,account);

       /* account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        account.setBio(profile.getBio());
        account.setProfileImage(profile.getProfileImage());*/
        // ***** 중요 !! account가 persistence 상태가 아닌데 싱크를 맞출 수 있는 방법!!!
        // save 구현체 안에서 아이디 값이 있는지 없는지 보고 있으면 merge를 시킴!!
        // 아이디 값은 account 객체가 detached 상태여서 가지고 있음
        // 노션 참고
        // 그래서 save가 기존 데이터에 업데이트를 시키는거라고 생각하면 됌
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {

        modelMapper.map(notifications,account);
       /* account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());*/
        accountRepository.save(account);
    }

    public void updateAccount(Account account, NicknameForm nicknameForm) {
        account.setNickname(nicknameForm.getNickname());
        accountRepository.save(account);
        login(account); // 네비바 로그인 상태 값 변경을 위해서 다시 로그인
    }
}
