package com.example.domain.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.net.URL;

@Entity
@Getter @Setter
public class Question {
    @Id
    @GeneratedValue
    @Column(name = "question_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bnt_id")
    private Bnt bnt;

    @Column(name = "image_url")
    private URL imageUrl;

    private String answer;
}
