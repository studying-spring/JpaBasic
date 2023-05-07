## 연관관계가 필요한 이유

객체를 테이블에 맞추어 모델링을 할 경우 → 객체지향스럽지 않은 로직 (본인 기준 지저분함)

Member Table

```java
@Entity @Getter @Setter
public class Member {

    @Id @GeneratedValue
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "TEAM_ID")
    private Long teamId;
}
```

Team Table

```java
@Entity @Getter @Setter
public class Team {

    @Id @GeneratedValue
    private Long id;

    private String name;

}
```

<저장>

```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeamId(team.getId());
em.persist(member);
```

<조회>

```java
Member findMember = em.find(Member.class, member.getId());

Long findTeamId = findMember.getTeamId();
Team findTeam = em.find(Team.class, findTeamId);
```

## 단방향 연관관계

Member Table

```java
@Entity
public class Member {

    @Id @GeneratedValue
    private Long id;

    @Column(name = "USERNAME")
    private String username;
    
    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;  // 원래 코드 : private Long teamId;
}
```

<저장>

```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeam(team); // 원래 코드 : member.setTeamId(team.getId());
em.persist(member);
```

<조회>

```java
Member findMember = em.find(Member.class, member.getId());

Long findTeam = findMember.getTeam;
/*
원래 코드
Long findTeamId = findMember.getTeamId();
Team findTeam = em.find(Team.class, findTeamId);
*/
```

<변경>

```java
Team newTeam = em.find(Team.class, 100L);
findMember.setTeam(newTeam);
```

## 양방향 연관관계와 연관관계의 주인

Team Table

```java
@Entity @Getter @Setter
public class Team {

    @Id @GeneratedValue
    private Long id;

    private String name;

		@OneToMany(mappedBy = "team") //User entity에 있는 team
		private List<Member> members = new ArrayList<>();

}
```

<팀에서 멤버 조회>

```java
Member findMember = em.find(Member.class, member.getId());
List<Member> memberList = findMember.getTeam().getMembers();

for(Member m : members) {
		System.out.println("m = " + m.getUsername());
}
```

### 연관관계의 주인과 mappedBy

- 객체와 테이블간에 연관관계를 맺는 차이를 이해하기
    - 객체 연관관계 2개 (회원 → 팀), (팀 → 회원) : 단방향 * 2
        - 즉, 객체의 양방향 관계는 사실 양방향 관계가 아니라 단방향 관계가 2개가 있는 것..!
    - 테이블 연관관계 1개 (회원 ↔ 팀) : 양방향 * 1
        - 외래 키 하나로 두 테이블의 연관관계를 관리

❗️둘 중 하나로 외래키를 관리해야 함 (which one?)

### → 연관관계의 주인을 정해야 함!!!

- 객체의 두 관계 중 하나를 연관관계의 주인으로 지정
- 연관관계의 주인만이 등록, 수정 가능
- 주인이 아닌 쪽은 read_only + mappedBy 속성으로 주인 지정

### Which one?

→ **외래 키가 있는 곳을 주인으로 정해라! (비즈니스 로직을 기준으로 선택하면 어려움)**

### Why?

→ 많은 이유가 있지만 성능 이슈가 있음

ex) Team에 있는 member list를 수정했는데, 다른 테이블에서 update 쿼리가 나감

## 양방향 매핑시 가장 많이 하는 실수

<이렇게 하지 마시오>

```java
Member member = new Member();
member.setUsername("member1");
em.persist(member);

Team team = new Team();
team.setName("TeamA");
team.getMembers().add(member);
em.persist(team);
```

→ Member Table에 member1의 team이 자동 update되지 않음!

|  ID   | USERNAME  | TEAM_ID  |
|:-----:|:---------:|:--------:|
|   1   |  member1  |   null   |

why? mappedBy는 가짜 매핑, 읽기 전용이기 때문

<Correct Answer>

```java
Team team = new Team();
team.setName("TeamA");
//team.getMembers().add(member);
em.persist(team);

Member member = new Member();
member.setUsername("member1");

team.getMembers().add(member);

member.setTeam(team);
em.persist(user);
```

→ 정상적으로 값이 들어감

### 이때!! `team.getMembers().add(member);`를 안넣어도 되지 않나??

→ 위 코드를 넣지 않아도

```java
Team findTeam = em.find(Team.class, team.getId());
List<Member> members = findTEam.getMembers();

for (Member m : members) {
		System.out.println("m = " + m.getUsername());
}
```

멤버리스트를 출력해줌 → jpa에서 members를 가져올 때 쿼리를 날려서 연관된 멤버를 가져오기 때문

### 그런데 왜 `team.getMembers().add(member);`를 넣어야 하는가..!

만약에 team에서 멤버리스트를 가져오기 전에 `em.flush();`와 `em.clear();`를 하지 않으면

1차 캐시에 있는 값을 가져옴

→ 1차 캐시에는 컬렉션 값이 들어가 있지 않음

<aside>
💡 그래서 양방향 매핑을 할 때는 양쪽에 값을 다 세팅해주는 것이 맞음.

</aside>

❗️여기서 실수를 범할 수 있음 (so, 연관관계 편의 메소드를 만드는 것이 좋음)

Member Table

```java
public void setTeam(Team team) {
    this.team = team;
    team.getUserList().add(this);   //추가
}
```

```java
Team team = new Team();
team.setName("TeamA");
//team.getMembers().add(member);
em.persist(team);

Member member = new Member();
member.setUsername("member1");

// team.getMembers().add(member); -> 안써도 됨

member.setTeam(team);
em.persist(user);
```

<aside>
❗ 연관관계 편의 메소드는 한쪽에만 설정하기

</aside>

## 주의

- 컨트롤러에서 엔티티 반환하지 말기 (Dto로 변환을 해서 반환하기..!!!!!!)
- toString 롬복 어노테이션 조심하기