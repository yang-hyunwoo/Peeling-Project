package peeling.project.basic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.dto.request.member.JoinMemberReqDto;
import peeling.project.basic.dto.response.member.JoinMemberResDto;
import peeling.project.basic.exception.CustomApiException;
import peeling.project.basic.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final MemberRepository memberRepository;

    // 서비스는 DTO를 요청받고 응답한다.
    @Transactional //트랜잭션이 메서드 시작할 때 , 시작되고 , 종료될때 함께 종료
    public JoinMemberResDto join(JoinMemberReqDto joinReqDto) {

        //1. 동일 유저네임 존재 검사
        memberRepository.findByUsername(joinReqDto.getUsername()).ifPresent(user -> {
            throw new CustomApiException("동일한 username이 존재합니다.");
        });

        //2. 패스워드 인코딩
        Member member = memberRepository.save(joinReqDto.toEntity(bCryptPasswordEncoder));

        //3. dto 응답

        return new JoinMemberResDto(member);
    }
}
