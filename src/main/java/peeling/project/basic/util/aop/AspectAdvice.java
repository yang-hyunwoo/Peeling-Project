package peeling.project.basic.util.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/*
추후 log table 생성 후 저장 로직 추가
 */
@Component
@Aspect
@Slf4j
public class AspectAdvice {
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
//        String s = methodSignature.getDeclaringTypeName(); package 경로 까지 포함
        String value = "";
        for(Object obj : args) {
            if(obj!=null) {
                value = args[0].toString();
            }
        }
        log.info("before = threadLocalUUID : [{}] httpMethod : [{}] method : [{}] value : [{}]", traceIdHolder.get().getUUID(), httpMethod, classMethodName, value);

    }

    //메소드 실행후
    @AfterReturning(value = "cut()" , returning = "returnObj")
    public void afterReturn(JoinPoint joinPoint , Object returnObj){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod(); //메소드의 이름
        String classMethodName = methodSignature.getDeclaringType().getSimpleName()+"."+method.getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();
        log.info("afterReturn = threadLocalUUID : [{}] httpMethod : [{}] : method : [{}] returnObj : [{}]", traceIdHolder.get().getUUID(), httpMethod, classMethodName, returnObj);
    }

    //메소드 실행후 오류
    @AfterThrowing(value = "cut()" , throwing = "e")
    public void afterThrow(JoinPoint joinPoint , Exception e){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod(); //메소드의 이름
        String classMethodName = methodSignature.getDeclaringType().getSimpleName()+"."+method.getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();
        log.info("afterThrow = threadLocalUUID : [{}] httpMethod : [{}] : method : [{}] exceptionMessage : [{}]", traceIdHolder.get().getUUID(), httpMethod, classMethodName, e.getMessage());
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
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            log.info("timer = threadLocalUUID : [{}] httpMethod : [{}] : method : [{}] timeMs = [{}]", traceIdHolder.get().getUUID(), httpMethod, classMethodName, timeMs);
            traceIdHolder.remove();
        }
    }
}
