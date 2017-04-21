package vn.com.vtcc.browser.api.aspect;

/**
 * Created by giang on 17/04/2017.
 */

import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggingHandler {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controller() {
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RequestMapping *)")
    public void requestMapping() {}

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {
    }

    @Pointcut("within(vn.com.vtcc.browser.api..*)")
    public void logAnyFunctionWithinResource() {
    }

    //before -> Any resource annotated with @RestController annotation
    //and all method and function taking HttpServletRequest as first parameter

    /*@Before("restController()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request != null) {
            log.info("The user with ip: " + request.getRemoteAddr() + " is requesting to method " + className + "."
                    + methodName + "() " + " with arguments: " + Arrays.toString(joinPoint.getArgs()));
        }
    }*/
    //After -> All method within resource annotated with @Controller annotation
    // and return a  value
    @AfterReturning(pointcut = "controller() && restController()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String returnValue = this.getValue(result);
        log.debug("Method Return value : " + returnValue);
    }

    //Around -> Any method within resource annotated with @Controller annotation
    @Around("restController()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            log.info("=========> User with ip: " + request.getRemoteAddr() + " is requesting to method " + className
                    + "." + methodName + "() " + " with arguments: " + Arrays.toString(joinPoint.getArgs())
                    + " execution time : " + elapsedTime + " ms ");
            return result;
        } catch (IllegalArgumentException e) {
            log.info("#######!!!!! Illegal argument " + Arrays.toString(joinPoint.getArgs()) + " in "
                    + joinPoint.getSignature().getName() + "()");
            throw e;
        }
    }

    private String getValue(Object result) {
        String returnValue = null;
        if (null != result) {
            if (result.toString().endsWith("@" + Integer.toHexString(result.hashCode()))) {
                returnValue = ReflectionToStringBuilder.toString(result);
            } else {
                returnValue = result.toString();
            }
        }
        return returnValue;
    }
}
