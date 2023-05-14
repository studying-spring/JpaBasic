# 프록시와 연관관계 관리

## 프록시

em.getReference() → 이 시점에는 데이터베이스에 쿼리를 안 보냄

이 값이 실제로 사용되는 시점에서 db에 쿼리를 날림

<특징>

- 실제 Entity를 상속 받아서 만들어지기 때문에 겉모양은 같음
  (Hibernate가 내부적으로 만들어내는 것)
- 사용하는 입장에서 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨
- 프록시 객체는 실제 객체의 참조를 보관
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출

프로시 객체의 초기화

```java
Member member = em.getReference(Member.class, "id1");

member.getName();
// 1. Member taget은 현재 null 상태
// 2. JPA가 영속성 컨텍스트에 요청을 함 (진짜 멤버 객체를 가져와달라)
// 3. DB 조회해서 실제 Entity를 생성해서 갖다 줌
// 4. 그 다음 target과 연결시켜줌
// 5. 그러면 target에 있는 getName을 통해서 member.getName() 반환됨
```

### **중요 특징⭐️**

- 프록시 객체는 처음에 한번만 초기화
- 프록시 객체를 초기화할 때 프록시 객체가 실제 엔티티로 바뀌는 것은 아님, 실제 엔티티에 접근이 가능한 것
  member.getClass()하면 → class hellojpa.Member$HibernateProxy$…
  교체X, 프록시는 유지가 되고 내부에 값만 채워짐
- 프록시 객체는 원본 엔티티를 상속받기 때문에 타입 체크시 주의해야 함!!!! (**instance of 사용!**, ==는 안됨)
- 영속성 컨텍스트에 실제 엔티티가 있으면 em.getReference를 호출했을 때 실제 엔티티 반환 (반대도 마찬가지, 처음에 프록시로 조회하면 em.find해도 프록시로 조회를 해버림)
- 이미 영속성 컨텍스트에 있으면 굳이 프록시로 가져와봐야 이득이 없음
- jpa입장에서는 m1==reference를 무조건 true로 반환해줘야 함(?)

```java
Member m1 = em.find(Member.class, member1.getId());
m1.getClass() -> class hellojpa.Member

Member reference = em.getReference(Member.class, member1.getId());
reference.getClass() -> class hellojpa.Member

m1 == reference : true
```

```java
Member reference = em.getReference(Member.class, member1.getId());
reference.getClass() -> class hellojpa.Member$HibernateProxy$…

Member m1 = em.find(Member.class, member1.getId());
m1.getClass() -> class hellojpa.Member$HibernateProxy$…

m1 == reference : true
```

- 준영속 상태(em.close, em.detach etc..)일 때는 프록시 초기화 할 때 오류남

### 프록시 확인

```java
Member refMember = em.getReference(Member.class, member1.getId());
```

- 프록시 인스턴스의 초기화 여부 확인

```java
// 아직 초기화 안했을 때
emf.getPersistenceUnitUtil().isLoaded(refMember) -> false

// 초기화 했을 때
refMember.getUsername();
emf.getPersistenceUnitUtil().isLoaded(refMember) -> true
```

- 프록시 클래스 확인 방법

```java
refMember.getClass() -> class hellojpa.Member$HibernateProxy$…
```

- 프록시 강제 초기화

```java
Hibernate.initialize(refMember) //강제 초기화
```

- 참고 : JPA 표준은 강제 초기화 없음

## 즉시로딩과 지연로딩

지연로딩 LAZY을 사용해서 프록시로 조회

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "TEAM_ID")
private Team team

=====================================
Member m = em.find(Member.class, member1.getId());
m.getTeam().getClass() -> class hellojpa.Team$HibernateProxy$…
m.getTeam().getName(); // 실제 team을 사용하는 시점에서 초기화
```

→ 멤버를 조회하면 멤버만 가져오고, 팀은 프록시로 조회함

### IF) Member와 Team을 자주 함께 사용한다면?

즉시로딩 EAGER를 사용해서 함께 조회

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "TEAM_ID")
private Team team

=====================================
Member m = em.find(Member.class, member1.getId());
m.getTeam().getClass() -> class hellojpa.Team
m.getTeam().getName(); // 그냥 실제 값을 가져옴
```

→ 멤버를 조회하면 팀이랑 조인해서 다 조회함 (프록시가 필요 없음)

### 프록시와 즉시로딩 주의..!!

- 실무에서는 즉시로딩 쓰지 말기
  - 즉시로딩을 적용하면 예상하지 못한 SQL이 발생
  - N+1 문제를 일으킴

    ```java
    List<Member> members = em.createQuery("select m from Member m", Member.class)
    .getResultList();
    // -> 쿼리가 두번 나감
    // select * from Member
    // select * from Team where TEAM_ID == xxx
    
    Team teamA = new Team();
    team.setName("teamA");
    em.persist(teamA);
    
    Team teamB = new Team();
    teamB.setName("teamB");
    em.persiste(teamB);
    
    Member member1 = new Member(); ... //teamA 소속
    Member member2 = new Member(); ... //teamB 소속
    
    List<Member> members = em.createQuery("select m from Member m", Member.class)
    .getResultList();
    // -> 쿼리가 3번 나감
    ```

- ManyToOne, OneToOne은 디폴트로 즉시 로딩으로 설정되어 있음 → LAZY로 다시 설정해줘야 함!!
- 패치 조인 사용하기

```java
List<Member> members = em.createQuery("select m from Member m join fetch m.team", Member.class)
.getResultList();
// -> 이렇게 하면 멤버와 팀을 조인해서 같이 다 가져와줌
```

## 영속성 전이(CASCADE)와 고아 객체

### 영속성 전이 : CASCADE

- 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때

종류 : ALL, PERSIST, REMOVE …

- 소유주가 하나일 때 쓰기

### 고아객체

부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제

```java
@OneToMany(mappedBy="parent", cascade=CascadeType.ALL, orphanRemoval=true)
private List<Child> childList = new ArrayList<>();

em.find(Parent.class, parent.getId());
findParent.getChildList().remove(0);
```

<주의>

- 참조하는 곳이 하나일 때 사용해야 함!
- 특정 엔티티가 개인 소유할 때 사용
- OneToOne, OneToMany에서만 가능
- 부모가 사라지면 자식도 다 delete 됨 Like CascadeType.REMOVE

```java
@OneToMany(mappedBy="parent", orphanRemoval=true)
private List<Child> childList = new ArrayList<>();

===================================================
Parent findParent = em.find(Parent.class, parent.getId());
em.remove(findParent);
// 자식까지 다 delete 됨
```

### 영속성 전이 + 고아 객체

두 옵션을 모두 활성화하면 자식의 생명주기를 부모에서 관리함




# 값 타입


## 기본값 타입

- 엔티티 타입
  - @Entity
  - 데이터가 변해도 식별자로 지속해서 추적 가능
- 값 타입
  - 자바 기본 타입(int, string)이나 객체
  - 식별자가 없음

### 값 타입 분류

- 기본값 타입 : int, double, Long, String …
  - 생명주기를 엔티티에 의존
  - 값 타입은 공유하면 안됨

    ```java
    // 공유안되고 있음
    int a = 10;
    int b = a;
    b = 20;
    System.out.println(a); // a=10
    System.out.println(b); // b=20
    =======================================================
    // 공유는 가능한데 변경이 불가
    Integer a = new Integer(10);;
    Integer b = a;
    // a.setValue(20) // 만약에 변경이 가능하면
    // ->
    // System.out.println(a); // a=20
    // System.out.println(b); // b=20
    // 하지만 변경할 방법이 없음
    ```

- 임베디드 타입 - JPA에서 정의해서 쓰기
  - 새로운 값 타입을 직접 정의할 수 있음
  - int, String과 같은 값 타입
  - 회원 엔티티 : 이름, 근무기간(근무 시작일, 근무 종료일), 집 주소(주소 도시, 주소 번지, 주소 우편번호)
  - 장점
    - 재사용, 높은 응집도
    - 의미 있는 메소드만 뽑아낼 수 있음
    - 값 타입을 소유한 엔티티에 생명주기를 의존함
    - 객체와 테이블을 아주 세밀하게 매핑하는 것이 가능
    - 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음
  - 임베디트 타입을 사용하기 전과 후에 매핑하는 테이블은 같음
  - 한 엔티티에서 같은 값 타입을 사용하면?

    ```java
    @Embedded
    private Address homeAddress;
    
    @Embedded
    @AttributeOverrides({
    					@AttributeOverride(name="city",
    										column=@Column(name="WORK_CITY")),
    					@AttributeOverride(name="street",
    										column=@Column(name="WORK_STREET")),
    					@AttributeOverride(name="zipcode",
    										column=@Column(name="WORK_ZIPCODE"))
    })
    private Address workAddress;
    ```

- 컬렉션 값 타입 - JPA에서 정의해서 쓰기

## 값 타입과 불변 객체

임베디드 타입 같은 값 타입을 여러 엔티티에서 굥유하면 위험함

- 값 타입의 실제 인스턴스인 값을 공유하는 것은 위험하므로 값을 복사해서 사용

### 한계

- 값을 항상 복사해서 사용하면 부작용은 피할 수 있음
- but, 직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입
- 객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없음
- so, 객체의 공유 참조는 피할 수 없음

### 불변 객체

- 객체 타입을 수정할 수 없게 만들기 → 불변 객체
- 불변 객체 : 생성 시점 이후 절대 값을 변경할 수 없는 객체
- 생성자로만 값을 설정하고 setter를 만들지 않기
- Integer, String

IF) 값을 바꾸고 싶다면?

→ 새로 만들기

```java
Address address = new Address("city", "street", "10000");
Member member = new Member();
....

Address newAddress = new Address("NewCity", address.getStreet(), address.getZipcode());
member.setHomeAddress(newAddress);
```

## 값 타입의 비교

값 타입 : 인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봄

동일성 비교 vs 동등성 비교

- 동일성 비교 : == 사용
- 동등성 비교 : equals() 사용
- 값 타입은 a.equals(b)를 사용해서 동등성 비교를 해야 함

## 값 타입 컬렉션

```java
@ElementCollection
@CollectionTable(name="FAVORITE_FOOD", joinColumns = @JoinColumn(name="MEMBER_ID"))
@Column(name="FOOD_NAME")
private Set<String> favoriteFoods = new HashSet<>();
```

- 값 타입을 하나 이상 저장할 때 사용
- 데베는 컬렉션을 같은 테이블에 저장X → 별도의 테이블이 필요

1. 값 타입 저장 예제

```java
Member m = new Member();
m.setUserName("member1");
m.setHomeAddress(new Address("city1", "street", "zipcode"));

m.getFavoriteFoods().add("치킨");
m.getFavoriteFoods().add("족발");
m.getFavoriteFoods().add("피자");

m.getAddressHistory().add(new Address("old1", "street", "zipcode"));
m.getAddressHistory().add(new Address("old2", "street", "zipcode"));

em.persist(m);
```

1. 값 타입 조회 예제

```java
Member findMember = em.find(Member.class, m.getId());
//컬렉션은 지연로딩

List<Address> addressHistory = findMember.getAddressHistory();
for ...
```

1. 갑 타입 수정 예제

```java
// 통으로 바꾸기!
// homeCity -> newCity
Address a = findMember.getHomeAddress();
findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcod()));

//치킨 -> 한식
findMember.getFavoriteFoods().remove("치킨");
findMember.getFavoriteFoods().add("한식");

//old1 -> new1
findMember.getAddressHistory().remove(new Address("old1", "street", "zipcode"));
findMember.getAddressHistory().add(new Address("new1", "street", "zipcode"));
// -> old1, old2를 지우고 다시 new1, old2를 insert
```

→ 값 타입 컬렉션에 변경 사항이 발생하면 주인 엔티티와 연관된 모든 데이터를 삭제하고 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장

+) 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 구성해야 함 : nullX, 중복X

### 대안

- 실무에서는 값 타입 컬렉션 대신 일대다 관계를 고려
- 영속성전이+고아객체 제거를 사용해서 값 타입 컬렉션처럼 사용

값 타입 컬렉션은 언제 쓰는가?

- 추적할 필요도 없고 업데이트 할 필요도 없는 굉장히 간단한 로직일 때

|  엔티티 타입   |          값 타입           |                               |
|:---------:|:-----------------------:|:-----------------------------:|
|   식별자 O   |          식별자X           |                               |
| 생명 주기 관리  |      생명주기를 엔티티에 의존      |                               |
|    공유     | 공유하지 않는 것이 안전(복사해서 사용)  | 공유를 해야 할 때는 불변 객체로 만드는 것이 안전  |