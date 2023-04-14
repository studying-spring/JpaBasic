## 객체와 테이블 매핑

### @Entity

- @Entity가 붙은 클래스는 JPA가 관리, 엔티티라 한다.
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity가 필수!!
- 주의
    - 기본 생성자 필수 (파라미터가 없는 public 또는 protected 생성자)
    - final 클래스, enum, interface, inner 클래스 사용X
    - 저장할 필드에 final 사용 X
- 속성 : name → 같은 클래스 이름이 없으면 default 값 사용하기!

### @Table

|            속성            |           기능            |     기본값     |
|:------------------------:|:-----------------------:|:-----------:|
|           name           |       매핑할 테이블 이름        | 엔티티 이름을 사용  |
|         catalog          |    데이터베이스 catalog 매핑    |             |
|          schema          |    데이터베이스 schema 매핑     |             |
| uniqueConstraints (DDL)  | DDL 생성 시에 유니크 제약 조건 생성  |             |

## 데이터베이스 스키마 자동 생성

- DDL을 애플리케이션 실행 시점에서 자동 생성
- 테이블 중심 →  객체 주심
- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성

```java
<property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
```


- 이렇게 생성된 DDL은 개발 장비에서만 사용!!(운영에서는 쓰면 안됨!)
- 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용

```java
<property name="hibernate.hbm2ddl.auto" value="create" />
```

→ 기존테이블이 있으면 drop 후 새로 create

```java
<property name="hibernate.hbm2ddl.auto" value="create-drop" />
```

→ create와 같으나 종료하는 시점에서 테이블을 drop

```java
<property name="hibernate.hbm2ddl.auto" value="update" />
```

→ drop을 안하고 변경사항만 반영을 해줌(대신 추가만 되고, 삭제는 안됨)
ex. id랑 name 컬럼만 있었는데, age를 중간에 추가하는건 반영이 되지만 다시 age를 삭제하면 그건 반영이 안됨

```java
<property name="hibernate.hbm2ddl.auto" value="validate" />
```

→ 테이블이 정상 매핑되었는지만 확인할 때

```java
<property name="hibernate.hbm2ddl.auto" value="none" />
```

== 주석처리 (value 값에 아무거나 써도 똑같음)

### 데이터베이스 스키마 자동 생성 - 주의

- **운영 장비에는 절대 create, create-drop, update 사용하면 안됨!**

| 개발 초기 | create, update |
| --- | --- |
| 테스트 서버 | update, validate |
| 스테이징 / 운영서버 | validate, none |

‼️ 로컬서버는 상관없지만, 여러명이서 쓰는 개발서버나 스테이징, 운영서버에는 가급적 쓰지 말기
+ 자동 생성이 된 스키마 관련 sql문도 가급적 다듬어서 운영서버에 반영

### DDL 생성 기능

- 제약조건은 실행자체에 영향을 주지 않고 ddl생성에만 영향을 줌!

## 필드와 컬럼 매핑

```java
@Entity
 public class Member {
	 @Id
	 private Long id;

	 @Column(name = "name")
	 private String username;private Integer age;

	 @Enumerated(EnumType.STRING)
	 private RoleType roleType;

	 @Temporal(TemporalType.TIMESTAMP) // Date, Time, DateTime
	 private Date createdDate;

	 @Temporal(TemporalType.TIMESTAMP)
	 private Date lastModifiedDate;

		// Lob + Sring : clob
	 @Lob // varchar를 넘어서는 큰 contents를 넣고 싶을 때
	 private String description;

}
```

### 매핑 어노테이션 정리

| 어노테이션 | 설명 |
| --- | --- |
| @Column | 컬럼 매핑 |
| @Temporal | 날짜 타입 매핑 (최신버전에서는 사용X) |
| @Enumerated | enum 타입 매핑 |
| @Lob | BLOB, CLOB 매핑 |
| @Transient | 특정 필드를 컬럼에 매핑하지 않음(매핑 무시) - DB에 저장이 되지 않음 오직 메모리에서만 씀 |

### @Column

| 속성 | 설명 | 기본값 |
| --- | --- | --- |
| name | 필드와 매핑할 테이블의 컬럼 이름 | 객체의 필드 이름 |
| insertable, updatable | 등록, 변경 가능 여부 (컬럼을 수정했을 때 반영을 할지 말지) | True |
| nullable(DDL) | null 값의 허용 여부를 설정, false로 설정하면 DDL 생성 시에 not null 제약 조건이 붙음 |  |
| unique(DDL) | @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용 |  |
| columnDefinition(DDL) | 데이터베이스 컬럼 정보를 직접 줄 수 있음 | 필드의 자바 타입과 방언 정보를 사용 |
| length(DDL) | 문자 길이 제약 조건, String 타입에만 사용 | 255 |
| precision, scale(DDL) | BigDecimal 타입에서 사용함(BigInteger도 사용), percision은 소수점을 포함한 전체 자릿수를 scale은 소수의 자릿수, 참고로 double, float 타입에는 적용X, 아주 큰 숫자나 정밀한 소수를 다루어ㅑ 할 때만 사용 | precision = 19, 
scale = 2 |

```java
@Column(updatable = false)
private String username;
```

→ 유저네임 컬럼은 절대 update되지 않음 (물론, 강제 update하면 되긴 함)

```java
@Column(nullable = false)
private String username;
```

→ not null 제약조건이 붙어서 null 값이 못 들어오게 함

```java
@Column(unique = true)
private String username;
```

→ 이 친구는 잘 안씀(유니크 조건 걸고 실행하면 유저네임이 랜덤값으로 변경이 됨)
그래서 @Table에서 유니크 조건을 거는 걸 선호함

```java
@Column(length = 10)
private String username;
```

→ varchar(255)에서 varchar(10)으로 수정

```java
@Column(columnDefinition = "varchar(100) defualt 'EMPTY'")
private String username;
```

→ 이 문구가 그대로 DDL에 들어가게 됨

### @Enumerated

‼️ 주의할 점

enumerated의 기본 값은 ORDINAL

```java
@Enumerated
private RoleType roletype;
```

→ 이렇게 되면 db에 숫자로 저장이 됨

### 이게 왜 문제?

```java
public enum RoleType {
    USER, ADMIN
}
```

이 상태에서 멤버 A는 User, 멤버 B는 Admin으로 저장하면 각각 0과 1로 저장이 됨

```java
public enum RoleType {
    GUEST, USER, ADMIN
}
```

그런데 중간에 게스트를 저 앞에 추가한 다음 멤버 C를 Guest로 추가하게 되면 DB에는

| ROLETYPE | NAME |
| --- | --- |
| 0 | A |
| 1 | B |
| 0 | C |

이런식으로 저장이 되어버림!!!!!

### 따라서 반드시 enum타입을 매핑할 때는 속성을 STRING으로 설정하기!!!

### ORDINAL 사용 X

```java
@Enumerated(value = EnumType.STRING)
private RoleType roletype;
```

### @Temporal

지금은 잘 사용안함….(LocalDateTime 쓰면 됨!)

```java
@Temporal(TemporalType.TIMESTAMP)
private Date createDate;
...
```

⬇️

```java
private LocalDate testLocalDate;
private LocalDateTime testLocalDateTime;
```

### @Lob

지정하는 속성이 없음!

- 매핑하는 필드 타입이 string이면 clob, 나머지는 blob으로 매핑

### @Transient

매핑을 하기 싫으면 이 어노테이션 사용하기~

## 기본 키 매핑

기본 키 매핑 어노테이션 : @Id, @GeneratedValue

### 기본 키 매핑 방법 - 직접 할당하는 경우 : @Id만 사용

```java
@Id
private String id;
```

### 기본 키 매핑 방법 - 값을 자동으로 할당하는 경우 (+@GeneratedValue 같이 씀)

> IDENTITY
>

```java
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

- 기본 키 생성을 데이터베이스에 위임 (My SQL에서 AUTO_INCREMENT)
- id값 세팅안하고 persist하면 db가 알아서 id값을 넣어줌
- JPA는 보통 트랜잭션 커밋 시점에서 insert문을 실행하지만, identity 전략은 persist할 때 insert문을 실행 (트랜잭션 내에서 여러 insert문을 실행한다고 해서 성능에 문제가 있는 것은 아님)

이 전략만 다른 이유는 Identity전략을 사용할 경우, 1차 캐시에 id가 null로 들어가 알 수 없기 때문에 db에 직접들어가야 id 값을 알 수 있음. 그래서 persist를 할 때 insert문을 실행하고 db에서 pk값을 조회해서 1차 캐시에 저장

> SEQUENCE
>

```java
@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
private Long id;
```

- Oracle 데베에서 많이 사용
- em.persist를 하면 MEM_SEQ에서 id값을 얻어와서 보여줌 그리고 트랜잭션 커밋하면 insert문이 실행됨

그러면 persist할 때마다 네트워크를 타게 되면 성능에 문제가 있지 않나?

```java
@SequenceGenerator(
		name = "MEMBER_SEQ_GENERATOR",
		sequenceName = "MEMBER_SEQ",
		initialBalue = 1, allocationSize = 50)
public class Member {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE,
				generator = "MEMBER_SEQ_GENERATOR")

		private Long id;

}
```

이렇게 allocationSize 속성을 50으로 설정해주면 미리 50개 size를 db에 올려놓음 → 성능최적화

```java
em.persist(member1); //1, 51
em.persist(member2); //MEM
em.persist(member3); //MEM

//51번을 만나면 51~100까지 또 가져옴
```

❗️allocationSize를 충분히 늘리면 좋지만,,, 웹서버를 내리게 되면 날라가기 때문에 중간에 숫자구멍이 생길 수 있음!!(이것은 낭비) → 그래서 50~100이 적당

<aside>
💡 id 타입 : int는 0이 있기 때문에 integer을 써야 하는데 값이 너무 커지면 한바퀴 돌기 때문에 결국 Long을 써야함!!!

</aside>

> TABLE
>

```java
@Id @GeneratedValue(strategy = GenerationType.TABLE)
```

- 키 생성 전용 테이블을 만드는 것
- 모든 db에 다 적용 가능!
- but, 성능이 좀 떨어짐

여기에도 allocationSize 쓸 수 있음

### 권장하는 식별자 전략

- 기본 키 제약 조건 : null 아님, 유일, **변하면 X**
- but, 미래까지 이 조건을 만족하는 자연키(like 주민등록번호)는 찾기 어려움
  ( + 주민번호는 기본 키로 적절하지 않음)
- so, 대리키를 사용하자!!! - 비즈니스와 전혀 상관 없는 키를 쓰는걸 권장
- **권장 : Long형 + 대체키 + 키 생성전략 사용**