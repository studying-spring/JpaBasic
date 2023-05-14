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