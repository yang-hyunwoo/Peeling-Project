package peeling.project.basic.service.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.dto.request.log.LogReqDto;
import peeling.project.basic.repository.log.LogRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class LogService {

    private final LogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logInsert(LogReqDto logReqDto , LoginUser loginUser) {
        logRepository.save(logReqDto.toEntity(loginUser));
    }

}
