package com.example.domain.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter @Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"account"})})
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String account;
    private String password;
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "edu_status")
    private EducateStatus eduStatus;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<UserBntInfo> userBntInfos = new ArrayList<>();
}

