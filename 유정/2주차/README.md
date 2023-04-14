## 영속성 컨텍스트

### 영속성 컨텍스트란?

- 엔티티를 영구 저장하는 환경
- EntityManager.persist(entity); → 엔티티를 영속성 컨텍스트라는 곳에 저장
- 논리적인 개념, 눈에 보이지 않음
- 엔티티 매니저를 통해 접근

### 엔티티의 생명주기

- 비영속


    Member member = new Member();
    member.setId(2L);
    member.setName("HelloB");


- 영속


    em.persist(member);


- 준영속


    // 특정 엔티티만 준영속 상태로 전환
    em.detach(member);
    
    // 영속성 컨텍스트를 완전히 초기화
    em.clear();
    
    // 영속성 컨텍스트를 종료
    em.close();


→ 영속성 컨텍스트가 제공하는 기능을 사용하지 못함

- 삭제


    em.remove(member);


### 엔티티 조회

- 1차 캐시에 해당되는 키값을 가진 엔티티가 존재하면 1차 캐시로부터 조회를 함
- 만약 1차 캐시에 존재하지 않으면 데이터베이스로부터 조회를 한 뒤, 조회한 내용을 1차 캐시에 저장


    Member member = new Member();
    member.setId(2L);
    member.setName("HelloB");
    
    em.persist(member); // 1차 캐시에 저장이 됨
    
    Member m = em.find(Member.class, 2L); // 1차 캐시에서 조회


### 엔티티 등록

- 엔티티는 커밋하는 순간 데이터베이스에 저장이 된다.


    // 트랜잭션 시작
    transaction.begin();
    
    // 1차 캐시에 저장이 되고 쓰기 지연 SQL 저장소에 생성한 SQL문을 쌓아둠
    em.persist(member);
    
    // 커밋하는 순간 데이터베이스에 SQL을 보냄
    transaction.commit();


### 엔티티 수정

- 수정을 할 시 엔티티를 다시 데베에 저장할 필요가 없음
- flush()가 호출되면, JPA는 1차캐시에 저장된 엔티티와 스냅샷을 비교
- 이때 스냅샷은 최초로 1차캐시에 들어온 상태를 저장해둔 것
- 만약 스냅샷과 다른 부분이 있다면 JPA는 UPDATE 쿼리를 쓰기지연 SQL저장소에 저장
- 마지막으로 해당 SQL문을 DB에 보냄


    Member m = em.find(Member.class, 2L);
    
    // 데이터 수정
    m.setName("hello2");
    
    // em.persist(member)나 em.update(member) 필요 없음
    
    transaction.commit();


### 엔티티 삭제


    Member m = em.find(Member.class, 2L);
    
    // 엔티티 삭제
    em.remove(m);


## 플러시

- 영속성 컨텍스트 변경 내용을 DB에 반영하는 것으로 데이터베이스 트랜잭션이 커밋되면 자동으로 flush가 발생
- em.flush()를 통해 플러시를 직접 호출할 수도 있음
- flush가 발생해도 영속성 컨텍스트를 비우지 않음
- 따라서 커밋 직전에만 수정사항이 다 이루어지면 됨