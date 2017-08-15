package vn.com.vtcc.browser.api.aspect;

/**
 * Created by giang on 17/04/2017.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component

public class LoggingHandler {
    Gson gson = new Gson();
    private final List<String> BLACKLIST_IPS = Arrays.asList("192.168.107.211", "192.168.107.212","192.168.107.213",
            "192.168.107.214","192.168.107.215","10.240.152.61","171.255.199.62",
            "171.255.199.63","171.255.199.82","171.255.199.81","171.255.199.40","171.255.199.41","171.255.199.61");
    private final List<String> BLACKLIST_FUNCTIONS = Arrays.asList("getImageFromByteArray","getHotTags",
            "getListCategories","updateRedisHotTags","getListSources","setLogForMessageBox","listLogByJobID");
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controller() {
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RequestMapping *)")
    public void requestMapping() {}

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {}

    //@Pointcut("execution(* net.tds.adm.metasolv.customerlink.services.*.get*(..))")
    @Pointcut("execution(* vn.com.vtcc.browser.api.controller.ArticleController.getArticleByNotification(..))" )
    public void detailController() {}

    //@Pointcut("execution(* net.tds.adm.metasolv.customerlink.services.*.get*(..))")
    @Pointcut("execution(* vn.com.vtcc.browser.api.controller.ArticleController.postListArticleReleated(..))" )
    public void detailRelatedController() {}

    @Pointcut("within(vn.com.vtcc.browser.api..*)")
    public void logAnyFunctionWithinResource() {}

    @AfterReturning("detailController() || detailRelatedController()")
    public void updateUserViews(JoinPoint joinPoint) throws JSONException {
        Object[] inputParams = joinPoint.getArgs();
        ArrayList<String> params = new ArrayList<>();
        for (Object p : inputParams) {
            if (p instanceof String) {
                params.add(p.toString());
            } else {
                JSONObject obj = (JSONObject) p;
                params.add(obj.get("id").toString());
            }
        }
    }

    //Around -> Any method within resource annotated with @RestController annotation
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
            if (BLACKLIST_IPS.contains(ip_address) || BLACKLIST_FUNCTIONS.contains(methodName) || methodName == null) {
                return result;
            }
            long elapsedTime = System.currentTimeMillis() - start;
            String params = Arrays.toString(joinPoint.getArgs());
            String requestParams = params.split("(?<=}, )")[0].replace("[{","{").replace("}, ","}");
            requestParams = requestParams.replaceAll("\"source\":\".*\",","");
            String notificationId = request.getHeader("notificationId") == null ? "undefined" : request.getHeader("notificationId");
            String currentAppVersion = request.getHeader("appVersion") == null ? "undefined" : request.getHeader("appVersion");
            String deviceType = request.getHeader("deviceType") == null ? "undefined" : request.getHeader("deviceType");
            String deviceVersion = request.getHeader("deviceVersion") == null ? "undefined" : request.getHeader("deviceVersion");
            String msisdn = request.getHeader("msisdn") == null ? "undefined" : request.getHeader("msisdn");
            String message = now + " " + request.getRemoteAddr() + " " + request.getMethod() + " " + methodName
                    + " " + requestParams + " " + elapsedTime + " " + notificationId + " " + currentAppVersion
                    + " " + deviceType + " " + msisdn + " " + deviceVersion;

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
