package com.bhbworkout;

import com.bhbworkout.account.AccountService;
import com.bhbworkout.account.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class WithSecurityContextFactory implements org.springframework.security.test.context.support.WithSecurityContextFactory<WithAccount> {

    private final AccountService accountService;

    @Override
    public SecurityContext createSecurityContext(WithAccount annotation) {
        String nickname = annotation.value();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(nickname+"@email.com");
        signUpForm.setPassword("123456789");
        accountService.processNewAccount(signUpForm);

        UserDetails principl = accountService.loadUserByUsername(nickname);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principl,principl.getPassword(),principl.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        return securityContext;
    }
}
