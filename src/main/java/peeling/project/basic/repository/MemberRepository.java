package peeling.project.basic.repository;

import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import peeling.project.basic.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member , Long> {

    Optional<Member> findByUsername(String username);
}
