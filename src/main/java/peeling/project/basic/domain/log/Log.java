package peeling.project.basic.domain.log;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import peeling.project.basic.domain.member.Member;

@NoArgsConstructor
@Getter
@Entity
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "memberId")
    private Member member;

    private String uuid;

    private boolean sucesStts;

    private String methodName;

    private String httpMethod;

    @Column(length = 4000)
    private String response;
    @Column(length = 4000)
    private String request;
    @Column(length = 4000)
    private String errorMsg;

    @Builder
    public Log(Member member,
               String uuid,
               boolean sucesStts,
               String methodName,
               String httpMethod,
               String response,
               String request,
               String errorMsg) {
        this.member = member;
        this.uuid = uuid;
        this.sucesStts = sucesStts;
        this.methodName = methodName;
        this.httpMethod = httpMethod;
        this.response = response;
        this.request = request;
        this.errorMsg = errorMsg;
    }
}
