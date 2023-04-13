package com.example.domain.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Result {
    @Id @GeneratedValue
    @Column(name = "result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bnt_info_id")
    private UserBntInfo userBntInfo;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "recognized_word")
    private String recognizedWord;

}
