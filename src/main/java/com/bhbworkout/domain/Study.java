package com.bhbworkout.domain;

import com.bhbworkout.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
})
@NamedEntityGraph(
        name = "Study.withTagsAndManagers", attributeNodes = {
                @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers")
})
@NamedEntityGraph(
        name = "Study.withZonesAndManagers", attributeNodes = {
            @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")
})
@NamedEntityGraph(
        name = "Study.withManagers", attributeNodes = {
                @NamedAttributeNode("managers")
})
@NamedEntityGraph(name = "Study.withMembers", attributeNodes = {
        @NamedAttributeNode("members")
})
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

    //타임리프에서 사용 study.isJoinable spring expression으로 객체 메서드로 호출 가능!!-
    public boolean isJoinable(UserAccount userAccount){
        Account account = userAccount.getAccount();
        // 공개, 모집중, 맴버아님, 매니저가아님,
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
    }

    // 타임리프에서 시큐리티로 메서드 활용가능
    public boolean isMember(UserAccount userAccount){

        return this.members.contains(userAccount.getAccount());
    }

    // 타임리프에서 시큐리티로 메서드 활용가능
    public boolean isManager(UserAccount userAccount){
        return this.managers.contains(userAccount.getAccount());
    }

    public void publish() {
        if(!this.closed && !this.isPublished()){
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if(this.isPublished() && !this.closed){
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("스터디를 종료할 수 없는 상태입니다. 스터디를 이미 종료했거나 공개하지 않았습니다.");
        }
    }

    public void recruit() {
        if(canRecruitStudy()){
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("스터디 모집을 할 수 없는 상태입니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요");
        }
    }

    // 스터디 공개 중이고 모집시간 값이 없으면
    // 아니면 한 시간 이내로 버튼을 눌렀는지
    public boolean canRecruitStudy() {
        return this.isPublished() && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void stopRecruit() {
        if(canRecruitStudy()){
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public boolean isRemovable() {
        return !this.published;
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }
}
