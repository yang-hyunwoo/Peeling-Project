package peeling.project.basic.util.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import peeling.project.basic.auth.LoginUser;
import peeling.project.basic.dto.request.log.LogReqDto;
import peeling.project.basic.service.log.LogService;
import java.lang.reflect.Method;

/* TODO SecurityContextHolder.getContext().getAuthentication().getPrincipal() 값이 있을 경우에만 디비에 로그가 저장이 된다.
        log 테이블에 member가 연관관계로 되어 있어서 그렇다.
        이대로 갈지 로그 테이블을 조금 수정을 해야 될지 고민을 해 봐야 겠다..
*/

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class AspectAdvice {

    private final LogService logService;
    private ThreadLocal<AspectUUID> traceIdHolder = new ThreadLocal<>();

    @Pointcut("execution(* peeling.project.basic.controller..*.*(..))")
    private void cut() {

    }

    //uuid 생성
    private void syncTraceId() {
        traceIdHolder.set(new AspectUUID());
    }

    //메소드 실행전
    @Before("cut()")
    public void before(JoinPoint joinPoint) {
        syncTraceId();

        Object[] args = joinPoint.getArgs();    //joinPoint 안의 argument값을 가져옴
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod(); //메소드의 이름
        String classMethodName = methodSignature.getDeclaringType().getSimpleName()+"."+method.getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();
        String uuid = traceIdHolder.get().getUUID();
//        String s = methodSignature.getDeclaringTypeName(); package 경로 까지 포함
        String value = "";
        for(Object obj : args) {
            if(obj!=null) {
                value = args[0].toString();
            }
        }
        loginUserChk(uuid , true , classMethodName, httpMethod, value,null,null);
        log.info("before = threadLocalUUID : [{}] httpMethod : [{}] method : [{}] value : [{}]", uuid, httpMethod, classMethodName, value);
    }

    //메소드 실행후
    @AfterReturning(value = "cut()" , returning = "returnObj")
    public void afterReturn(JoinPoint joinPoint , Object returnObj){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod(); //메소드의 이름
        String classMethodName = methodSignature.getDeclaringType().getSimpleName()+"."+method.getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();
        String uuid = traceIdHolder.get().getUUID();
        loginUserChk(uuid , true , classMethodName, httpMethod, null,returnObj.toString(),null);
        log.info("afterReturn = threadLocalUUID : [{}] httpMethod : [{}] : method : [{}] returnObj : [{}]", uuid, httpMethod, classMethodName, returnObj);
    }

    //메소드 실행후 오류
    @AfterThrowing(value = "cut()" , throwing = "e")
    public void afterThrow(JoinPoint joinPoint , Exception e){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod(); //메소드의 이름
        String classMethodName = methodSignature.getDeclaringType().getSimpleName()+"."+method.getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();
        String uuid = traceIdHolder.get().getUUID();
        loginUserChk(uuid , false , classMethodName, httpMethod, null,null,e.getMessage());
        log.info("afterThrow = threadLocalUUID : [{}] httpMethod : [{}] : method : [{}] exceptionMessage : [{}]", uuid, httpMethod, classMethodName, e.getMessage());
    }

    //메소드 실행전,후 timer
    @Around("cut()")
    public Object timer(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod(); //메소드의 이름
        String classMethodName = methodSignature.getDeclaringType().getSimpleName()+"."+method.getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();
        try {
            return joinPoint.proceed();
        } finally {
            String uuid = traceIdHolder.get().getUUID();
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            log.info("timer = threadLocalUUID : [{}] httpMethod : [{}] : method : [{}] timeMs = [{}]", uuid, httpMethod, classMethodName, timeMs);
            traceIdHolder.remove();

        }
    }

    /**
     * log 테이블 insert principal 이 anonymousUser 일 경우는 로그인이 되어 있지 않은 상태여서 member를 가져올수가 없어서 분기 처리
     * @param uuid
     * @param sucesStts
     * @param classMethodName
     * @param httpMethod
     * @param response
     * @param request
     * @param errorMsg
     */
    private void loginUserChk(String uuid,boolean sucesStts,String classMethodName, String httpMethod, String response,String request , String errorMsg) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LogReqDto logReqDto = LogReqDto.of(uuid,
                sucesStts,
                classMethodName,
                httpMethod,
                response,
                request,
                errorMsg);
        if (!principal.equals("anonymousUser")) {
            LoginUser loginUser = (LoginUser) principal;
            logService.logInsert(logReqDto, loginUser);
        }
    }
}

