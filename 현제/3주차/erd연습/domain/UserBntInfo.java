package com.example.domain.domain;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class UserBntInfo {
    @Id
    @Generated
    @Column(name = "user_bnt_info_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bnt_id")
    private Bnt bnt;

    private LocalDateTime createAt;

    public void setUser(Member member) {
        this.member = member;
        member.getUserBntInfos().add(this);
    }

    public static UserBntInfo createUserBntInfo(Member member, Bnt bnt) {
        UserBntInfo userBntInfo = new UserBntInfo();
        userBntInfo.setUser(member);
        userBntInfo.setBnt(bnt);
        userBntInfo.setCreateAt(LocalDateTime.now());
        return userBntInfo;
    }


}
