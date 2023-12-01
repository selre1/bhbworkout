package com.bhbworkout.account;

import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Tag;
import com.bhbworkout.settings.NicknameForm;
import com.bhbworkout.settings.Notifications;
import com.bhbworkout.settings.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        sendSignUpMailSender(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpform) {
        signUpform.setPassword(passwordEncoder.encode(signUpform.getPassword()));

        //account를 인스턴스화해서 생성하기 때문에 초기화 필드값이 적용됌
        Account account = modelMapper.map(signUpform,Account.class);
        account.generateEmailCheckToken();// 위의 트랙젝션안에 있어야 persist 상태가 됌
        //Builder에서는 초기화 필드는 무시됨 !!!
        /*Account account = Account.builder()
                .email(signUpform.getEmail())
                .nickname(signUpform.getNickname())
                .password(passwordEncoder.encode(signUpform.getPassword()))
                .studyUpdatedResultByWeb(true)
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .build();*/
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

    @Transactional
    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("bhb workout 로그인 링크");
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        javaMailSender.send(mailMessage);
    }

    @Transactional
    public void addTag(Account account, Tag tag) {
        //account는 detached 상태이며, tomany 관계에서 모두 null임
        //그래서 lazy loading 불가

        //persist 상태일때만 lazy loading 가능

        Optional<Account> byId = accountRepository.findById(account.getId());// 무조건 읽어옮
        byId.ifPresent(a -> a.getTags().add(tag));

        //레이지 로딩임 필요한 순간에 읽어옮
        //accountRepository.getOne();
    }

    public Set<Tag> getTags(Account account) {
       Optional<Account> byId = accountRepository.findById(account.getId());
       return byId.orElseThrow().getTags(); // 없으면 에러를 던지고 있으면 태그정보 리턴
    }

    @Transactional
    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }
}
