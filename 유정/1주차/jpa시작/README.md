Hello JPA - 프로젝트 생성

H2 데이터베이스 설치와 실행

[http://www.h2database.com/](http://www.h2database.com/)


    cd h2
    cd bin
    ./h2.sh


<aside>
💡 permission denied: ./h2.sh 에러

</aside>


    chmod 755 h2.sh
    ./h2.sh


![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/d721f567-2237-48fc-85db-1d5437f634e9/Untitled.png)

- 저장한 설정 : Generic H2(Server) 실제 데이터베이스처럼 별도로 띄어놓는 것
- 사용자명 : sa
- 비밀번호는 남겨놓도록 하겠습니다

<aside>
💡 http://localhost:8082로 접속

</aside>

## 메이븐 소개

- [https://maven.apache.org/](https://maven.apache.org/)
- 자바 라이브러리, 빌드 관리
- 최근에는 그래들(Gradle)이 점점 유명해지고 있음

프로젝트 생성

- 자바 8 이상(8 권장)
- 메이븐 설정
    - groupId : jpa-basic
    - artifactId : ex1-hello-jpa
    - version : 1.0.0


    <dependencies>
            <!-- JPA 하이버네이트 -->
            <dependency>
                <groupId>org.hibernate</groupId>
                <artifactId>hibernate-entitymanager</artifactId>
                <version>5.3.10.Final</version>
            </dependency>
            <!-- H2 데이터베이스 -->
            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
    
                <version>2.1.214</version>
            </dependency>
    </dependencies>


hibernate가 jpa 인터페이스를 가지고 있음

JPA 설정하기 - persistence.xml

- JPA 설정 파일
- /META-INF/persistence.xml 위치
- persistence-unit name으로 이름 저장
- javax.persistence로 시작 : JPA 표준 속성 (다른 jpa 구현 라이브러리를 써도 바꿀 수 있음)
- hibernate로 시작 : 하이버네이트 전용 속성 (다른 라이브러리를 쓰면 바꿔야 함)


    <?xml version="1.0" encoding="UTF-8"?>
    <persistence version="2.2"
                 xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
        <persistence-unit name="hello">
            <properties>
                <!-- 필수 속성 -->
                <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
                <property name="javax.persistence.jdbc.user" value="sa"/>
                <property name="javax.persistence.jdbc.password" value=""/>
                <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
                <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
                <!-- 옵션 -->
                <property name="hibernate.show_sql" value="true"/>
                <property name="hibernate.format_sql" value="true"/>
                <property name="hibernate.use_sql_comments" value="true"/>
                <!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
            </properties>
        </persistence-unit>
    </persistence>


방언 : sql 표준을 지키지 않는 특정 데이터베이스만의 고유한 기능

하이버네이트는 40가지 이상의 데이터베이스 방언 지원!

## 어플리케이션 개발


    package hellojpa;
    
    import javax.persistence.EntityManager;
    import javax.persistence.EntityManagerFactory;
    import javax.persistence.EntityTransaction;
    import javax.persistence.Persistence;
    import java.util.List;
    
    public class JpaMain {
        public static void main(String[] args) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
            EntityManager em = emf.createEntityManager();
    
            try {
                            ...
            } catch (Exception e) {
                ...
            } finally {
                em.close();
            }
            emf.close();
        }
    }


- 회원 등록


    Member member = new Member();
    member.setId(100L);
    member.setName("HelloB");
    em.persist(member);


- 회원 조회



    Member findMember = em.find(Member.class, 1L);
    System.out.println("findMember.getId() = " + findMember.getId());
    System.out.println("findMember.getName() = " + findMember.getName());


- 회원 수정


    Member findMember = em.find(Member.class, 1L);
    findMember.setName("HelloJPA");


- 회원 삭제


    Member findMember = em.find(Member.class, 1L);
    em.remove(findMember);


❗️엔티티 매니저는 쓰레든 간 공유하지 않음

❗️JPA의 모든 데이터 변경은 트랜잭션 안에서 실행


    EntityTransaction tx = em.getTransaction();
    tx.begin();
        try {
                    ...
            tx.commit(); //커밋하는 시점
    
        } catch (Exception e) {
            tx.rollback();


### JPQL

실습

- JPQL로 전체 회원 검색


    List<Member> result = em.createQuery("select m from Member as m", Member.class)
            .getResultList();
    
    for (Member member : result) {
        System.out.println("member.getName() = " + member.getName());


- JPQL로 ID가 2 이상인 회원만 검색


    List<Member> result = em.createQuery("select me from Member as m where m.id = 1L", Member.class)
        .getResultList();
