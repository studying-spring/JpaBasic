### SQL 중심적인 개발의 문제점

무한 반복, 지루한 코드

쿼리를 계속 작성을 해야 하는 과정을 무한 반복

→ SQL에 의존적인 개발을 피하기가 어렵고 의존적인 개발을 한다면 기획의 요구사항 변경에 따라 리소스가 낭비될 수 밖에 없다.

### JPA 등장

JPA란?

자바 진영에서 ORM(Object-Relational Mapping) 기술 표준으로 사용되는 인터페이스의 모음

JPA를 구현한 대표적인 오픈소스로는 Hibernate (80% 이상이 사용)

ORM이란?

객체와 관계형 데이터베이스랑 맵핑 (**어플리케이션의 객체를 RDB 테이블에 자동으로 영속화 해주는 것)**

### JPA를 왜 사용해야 하는가?

**생산성**

자바 컬렉션에 저장하듯이 한줄 코드로 해결


    저장 : jpa.persit(member)
    
    조회: Member member = jpa.find(memberId)
    
    수정: member.setName(”변경할 이름”)
    
    삭제: jpa.remove(member)


**유지보수**

- 기존 : 필드 변경시 모든 SQL 수정
- JPA : 필드만 추가하면 됨, SQL은 JPA가 처리


    public class Item {
        private int id;
        private String name;
        private int price;
    }


**JPA와 패러다임의 불일치 해결**

JPA와 상속

1. 저장


    개발자가 할일
    jpa.persist(album);
    
    나머진 JPA가 처리
    INSERT INTO ITEM (ID, NAME, PRICE) VALUE (1, 'BE_TOGETHER', 18600);
    INSERT INTO ALBUM (ARTIST) VALUE ('BTOB');


1. 조회


    //개발자가 할일
    Album album = jpa.find(Album.class, albumId);
    
    //나머진 JPA가 처리 (JPA가 다 조인해줌)
    SELECT I.*, A.*
      FROM ITEM I
      JOIN ALBUM A ON I.ITEM_ID = A.ITEM_ID


JPA와 연관관계 : **Class에서 또 다른 Class Type을 필드 변수로 가지고 있는것**

JPA와 객체 그래프 탐색


    //연관관계 저장
    Item item = new Item();
    item.setId(1);
    item.setName("BE_TOGETHER");
    item.setPrice(18600);
    
    Album album = new Album();
    album.setArtist("BTOB");
    
    item.setAlbum(album);
    jpa.persist(item);
    
    //객체 그래프 탐색
    Item item = jpa.find(Item.class, id);
    Artist artist = item.getArtist();


JPA와 비교하기

→ 동일한 트랜잭션에서 조회한 엔티티는 같음을 보장


    int id = 1;
    Item item1 = jpa.find(Item.class, id);
    Item item2 = jpa.find(Item.class, id);
    
    item1 == item2; // 같다. 


**JPA의 성능 최적화 기능**

1. 1차 캐시와 동일성 보장
    - **같은 트랜잭션** 안에서는 같은 엔티티를 반환 - 약간의 조회 성능 향상

   <1차 캐시 과정>

    1. 조회 시 처음 1차 캐시에 해당 데이터가 있는지 탐색을 한다. -> 만약 있으면 바로 리턴
    2. 조회 결과 1차 캐시에 데이터가 없으면 데이터베이스에 접근해 값을 탐색한다.
    3. 탐색 결과를 바로 리턴하는 것이 아닌 다음 탐색에서 재사용할 수 있도록 1차 캐시에 저장한다.


    Int id = 1;
    Item item1 = jpa.find(Item.class, id); //SQL
    Item item2 = jpa.find(Item.class, id); //캐시
    
    println(item1 == item2) //true


   → 두 번째 조회하는 시점에서는 쿼리가 나가지 않아 SQL 1번만 실행 (1차 캐시에 저장되어있기 때문에)

2. 트랜잭션을 지원하는 쓰기 지연 (데이터를 버퍼로 모음)
    - 엔티티 매니저는 트랜잭션을 커밋하기 직전까지 DB에 저장하지 않고 내부 쿼리 저장소에 INSERT SQL을 모음
    - 트랜잭션 커밋할 때 모아둔 쿼리를 DB에 보냄 (JDBC BATCH SQL 기능을 사용해서 한번에 SQL 전송)
3. 지연 로딩, 즉시 로딩
    - 지연 로딩 : 객체가 실제 사용될 때 로딩
    - 즉식 로딩 : JOIN SQL로 한번에 연관된 객체까지 미리 조회