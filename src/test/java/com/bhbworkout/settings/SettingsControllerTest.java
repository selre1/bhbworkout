package com.bhbworkout.settings;

import com.bhbworkout.WithAccount;
import com.bhbworkout.account.AccountRepository;
import com.bhbworkout.account.AccountService;
import com.bhbworkout.domain.Account;
import com.bhbworkout.domain.Tag;
import com.bhbworkout.tag.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class SettingsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }


    @WithAccount(value = "bhb")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount(value = "bhb")
    @DisplayName("계정 태그 추가")
    @Transactional
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("bhbb");

        mockMvc.perform(post("/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag tag = tagRepository.findByTitle("bhbb");

        assertNotNull(tag);
        //accountRepository.findByNickname("bhb") 여기까지는 persist 상태
        // accountRepository.findByNickname("bhb").getTags() detached 상태
        // 따라서 transactional 추가 해줘야함
        // 그러면 getTags()가 lazy 로딩함
        assertTrue(accountRepository.findByNickname("bhb").getTags().contains(tag));
    }

    @WithAccount(value = "bhb")
    @DisplayName("계정 태그 삭제")
    @Transactional
    @Test
    void removeTag() throws Exception {
        Account account = accountRepository.findByNickname("bhb");
        Tag tag = tagRepository.save(Tag.builder().title("bhbb").build());
        accountService.addTag(account,tag);

        assertTrue(account.getTags().contains(tag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("bhbb");

        mockMvc.perform(post("/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(accountRepository.findByNickname("bhb").getTags().contains(tag));
    }


    @WithAccount(value = "bhb")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception{
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(view().name("settings/profile"));
    }

    @WithAccount(value = "bhb")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception{
        mockMvc.perform(post("/settings/profile")
                .param("bio","소개소개")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));
        Account bhb = accountRepository.findByNickname("bhb");
        assertEquals("소개소개", bhb.getBio());
    }

    @WithAccount(value = "bhb")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        mockMvc.perform(post("/settings/profile")
                // 길이 넘기면 에러
                .param("bio", "소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개소개")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
        Account account = accountRepository.findByNickname("bhb");
        assertNull(account.getBio());
    }

    @WithAccount(value = "bhb")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception{
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount(value = "bhb")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception{
        mockMvc.perform(post("/settings/password")
                .param("newPassword","12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("bhb");
        assertTrue(passwordEncoder.matches("12345678", account.getPassword()));
    }
}