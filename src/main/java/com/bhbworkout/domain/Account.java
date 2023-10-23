package com.bhbworkout.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    private boolean studyCreatedByWeb;//스터디가 만들어졌다는걸 web으로 받을거?

    private boolean studyEnrollmentResultByEmail; //스터디 모임의 가입신청결과를
    private boolean studyEnrollmentResultByWeb; //스터디 모임의 가입신청결과를
    private boolean studyUpdatedResultByWeb; //스터디 바뀐 결과를
    private boolean studyUpdatedResultByEmail; //스터디 바뀐 결과를

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }
}
