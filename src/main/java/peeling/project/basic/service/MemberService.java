package peeling.project.basic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import peeling.project.basic.domain.member.Member;
import peeling.project.basic.dto.request.member.JoinMemberReqDto;
import peeling.project.basic.dto.response.member.JoinMemberResDto;
import peeling.project.basic.exception.CustomApiException;
import peeling.project.basic.exception.error.ErrorCode;
import peeling.project.basic.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {


    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    // 서비스는 DTO를 요청받고 응답한다.
    //트랜잭션이 메서드 시작할 때 , 시작되고 , 종료될때 함께 종료
    public JoinMemberResDto join(JoinMemberReqDto joinReqDto) {

        //1. 동일 유저네임 존재 검사
        memberRepository.findByEmail(joinReqDto.getEmail()).ifPresent(user -> {
            throw new CustomApiException(ErrorCode.DUPLICATED_EMAIL.getMessage());
        });
        //2. 패스워드 인코딩
//        Member member = memberRepository.save(joinReqDto.toEntity(bCryptPasswordEncoder));
        Member member = memberRepository.save(joinReqDto.toEntity(passwordEncoder));
        //3. dto 응답
        return new JoinMemberResDto(member);
    }

    public void memberLgnFailCnt(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomApiException(ErrorCode.MEMBER_INVALIED.getMessage()));
        if(member.getLgnFlrCnt() <=4) {
            member.lgnFlrCntPlus();
        }
    }

    public void memberLgnFailInit(Long id) {
        memberRepository.findById(id).orElseThrow(() -> new CustomApiException(ErrorCode.MEMBER_INVALIED.getMessage())).lgnFlrCntInit();
    }
}
