package com.bhbworkout.main;

import com.bhbworkout.account.AccountRepository;
import com.bhbworkout.account.AccountService;
import com.bhbworkout.account.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;


    @BeforeEach
    void beforEach(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("bhb");
        signUpForm.setEmail("selre1@naver.com");
        signUpForm.setPassword("123456789");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    @DisplayName("이메일 로그인")
    @Test
    void login_with_email() throws Exception {
        mockMvc.perform(post("/login")
                .param("username","selre1@naver.com")
                .param("password", "123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("bhb"));

    }

    @DisplayName("닉네임 로그인")
    @Test
    void login_with_nickname() throws Exception{
        mockMvc.perform(post("/login")
                .param("username","bhb")
                .param("password", "123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("bhb"));
    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception{
        mockMvc.perform(post("/login")
                .param("username","selre111")
                .param("password", "1234000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃")
    @Test
    void logout_success() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}