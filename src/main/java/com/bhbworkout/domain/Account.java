package com.bhbworkout.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)//중복 방지
    private String email;

    @Column(unique = true)//중복 방지
    private String nickname;

    private String password;

    private boolean emailVerified; // 아메일 인증이된 계정인지 확인

    private String emailCheckToken; // 이메일 검증 토큰값

    private LocalDateTime joinedAt;

    private String bio;

    private String url;
    private String occupation;

    private String location; //varchar(255)

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail; //스터디가 만들어졌다는걸 이메일로 받을거?

    private boolean studyCreatedByWeb = true;//스터디가 만들어졌다는걸 web으로 받을거?

    private boolean studyEnrollmentResultByEmail; //스터디 모임의 가입신청결과를
    private boolean studyEnrollmentResultByWeb = true; //스터디 모임의 가입신청결과를
    private boolean studyUpdatedResultByEmail; //스터디 바뀐 결과를
    private boolean studyUpdatedResultByWeb = true; //스터디 바뀐 결과를

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags;
    private LocalDateTime emailCheckTokenGeneratedAt;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true; // 이메일이 인증되었다 true
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.getEmailCheckToken().equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.getEmailCheckTokenGeneratedAt().isBefore(LocalDateTime.now().minusHours(1));
    }

}
