package com.bhbworkout.domain;

import com.bhbworkout.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


//쿼리 튜닝 작업
// eager fetch 전략
// 스터디를 조회(@GetMapping("/study/{path}"))하면 5번의 쿼리가 발생되던걸 한번에 조회
// repository에 @entitygraph 붙여주면 됌
@NamedEntityGraph(
        name = "Study.withAll", attributeNodes = {
                @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")
}
)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    //lob은 기본값이 EAGER,
    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;// 본문

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;//스터디 종료한 시간

    private LocalDateTime recruitingUpdatedDateTime; // 너무 자주 열고 닫기 할 수 없게 조절하기 위함

    private boolean recruiting; //현재 인원 모집중인지

    private boolean published; //공개여부

    private boolean closed; // 종료여부

    private boolean useBanner;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount){
        Account account = userAccount.getAccount();
        // 공개, 모집중, 맴버아님, 매니저가아님
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount){
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount){
        return this.managers.contains(userAccount.getAccount());
    }
}
