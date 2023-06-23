package peeling.project.basic.domain.member;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import peeling.project.basic.domain.constant.MemberEnum;

import java.time.LocalDateTime;

@NoArgsConstructor  //스프링이 User 객체 생성시 빈 생성자로 new를 함
@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 20)
    private String email;
    @Column(nullable = false , length = 20)
    private String username;

    @Column(nullable = false , length = 60) // 패스워드 인코딩(BCrypt)
    private String password;

    @Column(nullable = false , length = 20)
    private String fullname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberEnum role;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String provider;

    private String providerId;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 4000)
    private String refreshToken;

    private int lgnFlrCnt;

    private boolean isUsed;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastAccessDate;

    @Builder
    public Member(Long id,
                  String username,
                  String password,
                  String email,
                  String fullname,
                  MemberEnum role,
                  String refreshToken,
                  int lgnFlrCnt,
                  String provider,
                  String providerId,
                  boolean isUsed,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt,
                  LocalDateTime lastAccessDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.role = role;
        this.refreshToken = refreshToken;
        this.lgnFlrCnt = lgnFlrCnt;
        this.provider = provider;
        this.providerId = providerId;
        this.isUsed = isUsed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastAccessDate = lastAccessDate;
    }

    public void refreshTokenUpdIns(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void lgnFlrCntPlus() {
        this.lgnFlrCnt+= 1;
    }

    public void lgnFlrCntInit() {
        this.lgnFlrCnt = 0;
    }

    public void usedChange() {
        this.isUsed = !this.isUsed;
    }
}
