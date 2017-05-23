package vn.com.vtcc.browser.api.aspect;

/**
 * Created by giang on 17/04/2017.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private final List<String> BLACKLIST_IPS = Arrays.asList("192.168.107.211", "192.168.107.212","192.168.107.213",
            "192.168.107.214","192.168.107.215","171.224.122.145");
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
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip_address = request.getRemoteAddr();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String now = dateFormat.format(date);
            String methodName = joinPoint.getSignature().getName();
            Object result = joinPoint.proceed();
            if (BLACKLIST_IPS.contains(ip_address)) {
                return result;
            }
            long elapsedTime = System.currentTimeMillis() - start;
            String params = Arrays.toString(joinPoint.getArgs());
            String requestParams = params.split("(?<=}, )")[0].replace("[{","{").replace("}, ","}");
            String notificationId = request.getHeader("notificationId") == null ? "undefined" : request.getHeader("notificationId");
            String message = now + " " + request.getRemoteAddr() + " " + request.getMethod() + " " + methodName
                    + " " + requestParams + " " + elapsedTime + " " + notificationId;

            log.info(message);
            return result;
        } catch (IllegalArgumentException e) {
            log.info("!!!!! Illegal argument " + Arrays.toString(joinPoint.getArgs()) + " in "
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
