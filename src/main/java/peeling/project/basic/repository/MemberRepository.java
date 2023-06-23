package peeling.project.basic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import peeling.project.basic.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member , Long> {

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndProvider(String email, String provider);

    Optional<Member> findByRefreshToken(String token);

}
