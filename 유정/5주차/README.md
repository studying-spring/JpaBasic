# 다양한 연관관계 매핑

## 다대일 [N:1]

- 외래 키가 있는 쪽이 연관관계의 주인
- 다쪽에 외래키

## 일대다 [1:N] (사용하지 말자)

일대다 단방향(실무에서는 거의 사용하지 않는 모델)

- 일이 연관관계 주인
- 그래서 멤버와 팀에서 팀에 있는 멤버리스트를 수정하면 멤버 테이블에 있는 팀아이디가 수정
- 객체와 테이블의 차이(객체 측에서는 일이 주인인데 테이블에서는 다 쪽에 외래키가 있으므로 객체 측에서 보았을 때 반대편 테이블의 외래 키를 관리하는 특이한 구조를 가지게 됨)
- Update 쿼리가 추가로 실행이 됨

→ 일대다 단방향 매핑보다는 **다대일 양방향 매핑**

일대다 양방향(좀 억지스러움)

```
// 읽기 전용
@ManyToOne
@JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
private Team team;
```

## 일대일 [1:1]

- 외래 키는 어디에나 넣어도 됨 (비즈니스 로직에 따라서 정하기)
- 일대일이기 때문에 외래 키에 유니크 조건을 걸기

일대일 : 주 테이블에 외래 키 단방향

일대일 : 주 테이블에 외래 키 양방향

- 외래 키가 있는 곳이 주인
- 반대편은 mappedBy 적용
1. JPA 매핑 편리
2. 값이 없으면 외래 키에 null 허용

일대일 : 대상 테이블에 외래 키 단방향 (JPA 지원X)

일대일 : 대상 테이블에 외래 키 양방향 == 일대일 : 주 테이블에 외래키 양방향

1. 주:대상 - 일대일에서 일대다로 관계로 변경할 때 테이블 구조 유지
2. 양방향으로 만들어야 함
3. 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨

## 다대다 [N:M] (실무에서 쓰지 말기~)

- 관계형 데이터베이스는 다대다 관계를 표현할 수 없음
- 연결 테이블을 추가해서 대대일, 일대다로 풀어내야 함
- 객체는 다대다 관계가 가능

한계점

- 편리해 보이지만 단순히 연결만 하고 끝나지 않음 (분명히 필요한 추가적인 내용이 있음)

극복

- 중간 테이블을 엔티티로 승격시키기!


## 속성

@JoinColumn : 외래키 매핑할 때 사용

| 속성                                                                            | 설명 | 기본 값 |
|-------------------------------------------------------------------------------| --- | --- |
| name                                                                          | 외래 키 이름 | 필드명_참조하는 테이블의 기본키 컬럼명 |
| referencedColumnName                                                          | 외래 키가 참조하는 대상 테이블의 컬럼명 | 참조하는 테이블의 기본 컬럼명 |
| foreignKey(DDL)                                                               | 외래 키 제약조건 직접 지정 가능 (테이블 생성 시 사용) |  |
| unique <br> nullable instance <br> updatable <br> columnDefinition <br> table | same with @Column attribute |  |

@ManyToOne: 다대일 관계 매핑

| 속성 | 설명                            | 기본값                                       |
| --- |-------------------------------|-------------------------------------------|
| optional | false로 설정하면 연관된 엔티티가 항상 있어야 함 | true                                      |
| fetch | 글로벌 패치 전략을 설정                 | @ManyToOne : eager <br> @OneToMany : lazy |
| casacade | 연속성 전이 기능을 사용                 |                                           |
| targetEntity | 연관된 엔티티의 타입 정보 설정 <br> 거의 사용X |                                           |



# 고급매핑

## 상속관계 매핑

- 관계형 데이터베이스에는 상속 관계가 없고 객체에는 있음
- 슈퍼타입 서브타입 관계라는 모델링 기법이 객체 상속과 유사함

### How? → 3가지 방법 (어떤 방법으로 매핑을 하던 JPA 지원O)

- 기본은 단일 테이블 전략
- 주요 어노테이션
    - @Inheritance(strategy = InheritanceType.XXX) ← JOINED / SINGLE_TABLE / TABLE_PER_CLASS
    - @DiscriminatorColumn(name=”DTYPE”)
    - @DiscriminatorValue(”M”)

1. 조인 전략 : 각각 테이블로 변환 (가장 정교화)

```java
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
public class Item {}
```

<img width="1167" alt="image" src="https://user-images.githubusercontent.com/95369406/236673099-a9cc7ac1-f94b-4469-a63a-dfef22d32139.png">

장점

- 테이블 정규화
- 외래키 참조 무결성 제약조건 활용가능
- 저장공간 효율화

단점

- 조회시 조인을 많이 사용 → 성능 저하
- 조회 쿼리가 복잡
- Insert 쿼리가 두번 나감

→ 근데 딱히 치명적인 단점들은 아님

1. 단일 테이블 전략 : 통합 테이블로 변환 (inset문도 한번, 조인도 필요X → 성능 GOOD)

```java
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn //꼭 만들어주기
public class Item {}
```

<img width="1048" alt="image" src="https://user-images.githubusercontent.com/95369406/236673115-e0b16c0d-1eb8-4179-a80e-7774e0a2e0fe.png">

장점

- 조인이 필요 없으므로 조회 성능이 빠름
- 조회 쿼리 단순함

단점

- 자식 엔티티가 매핑한 컬럼은 모두 null 허용
- 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있음 → 상황에 따라 테이블이 커짐

→ 좀 치명적인 단점

1. 구현 클래스마다 테이블 전략 : 서브타입 테이블로 변환 (조인전략과 비슷, item 클래스는 추상클래스로, 조회할 때 비효율적(A, M, B을 통합해서 조회하기 때문)) - **쓰지마**

```java
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn //여기서는 의미가 없음
public abstract class Item {}
```

<img width="1174" alt="image" src="https://user-images.githubusercontent.com/95369406/236673003-099a7907-594f-40f0-ac0a-181c96fc8c55.png">

장점

- 서브 타입을 명확하게 구분해서 처리할 때 효과적
- not null 제약조건 사용 가능

단점

- 조회할 때 성능이 느림(UNION SQL)
- 자식 테이블을 통합해서 변경 등등 쿼리하기 어려움

→ 많이 치명적인 단점 ⇒ **쓰지마!**

<aside>
💡 보통 조인 전략을 쓰지만 테이블이 너무 단순한거 같으면 단일 테이블 전략 사용

</aside>

## MappedSuperclass

- 공통 매핑 정보가 필요할 때 사용

ex) 보통 거의 모든 테이블은 created_date, updated_date가 필요
→ 모든 테이블마다 생성일, 수정일 쓰는건 귀찮음
→ BaseEntity를 만들어서 생성일, 수정일을 넣은 다음 다른 테이블에서 BaseEntity를 상속받으면 편-리-V

```java
@MappedSuperclass
public abstract class BaseEntity{}

...

public class Member extends BaseEntity{}
```

- 상속 관계 매핑 X, 엔티티 X, 테이블과 매핑 X
- 조회, 검색 불가
- 추상 클래스 권장
- 테이블과 관계 없음