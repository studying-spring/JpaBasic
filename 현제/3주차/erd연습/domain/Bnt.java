package com.example.domain.domain;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Bnt {
    @Id @GeneratedValue
    @Column(name = "bnt_id")
    private Long id;

}
