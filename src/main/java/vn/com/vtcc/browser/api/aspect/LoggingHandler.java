package vn.com.vtcc.browser.api.aspect;

/**
 * Created by giang on 17/04/2017.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.script.Script;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.elasticsearch.ESClient;
import vn.com.vtcc.browser.api.model.ApiSearchRequest;
import vn.com.vtcc.browser.api.utils.TextUtils;

@Aspect
@Component
public class LoggingHandler {
    private final List<String> BLACKLIST_IPS = Arrays.asList("192.168.107.211", "192.168.107.212","192.168.107.213",
            "192.168.107.214","192.168.107.215","10.240.152.61","171.255.199.62",
            "171.255.199.63","171.255.199.82","171.255.199.81","171.255.199.40","171.255.199.41","171.255.199.61");
    private final List<String> BLACKLIST_FUNCTIONS = Arrays.asList("getImageFromByteArray","getHotTags",
            "getListCategories","updateRedisHotTags","getListSources","setLogForMessageBox","listLogByJobID");
    Logger log = LoggerFactory.getLogger(this.getClass());
    TransportClient esClient;

    @Autowired
    public LoggingHandler(ESClient es_client) {
        this.esClient = es_client.getClient();
    }

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controller() {
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RequestMapping *)")
    public void requestMapping() {}

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restController() {}

    @Pointcut("execution(* vn.com.vtcc.browser.api.controller.ArticleController.getArticleByNotification(..))" )
    public void detailControllerNotification() {}

    @Pointcut("execution(* vn.com.vtcc.browser.api.controller.ArticleController.postListArticleReleated(..))" )
    public void detailControllerRelated() {}

    @Pointcut("execution(* vn.com.vtcc.browser.api.controller.ArticleController.getArticleById(..))" )
    public void detailController() {}

    /*@Pointcut("execution(* vn.com.vtcc.browser.api.controller.ArticleController.streamingVideo(..))" )
    public void streamVideo() {}*/

    @Pointcut("within(vn.com.vtcc.browser.api..*)")
    public void logAnyFunctionWithinResource() {}

    @After("detailControllerRelated() || detailController()")
    public void updateUserViews(JoinPoint joinPoint) throws JSONException, ExecutionException, InterruptedException {
        Object[] inputParams = joinPoint.getArgs();
        String id = ProductionConfig.EMPTY_STRING;
        for (Object p : inputParams) {
            if (p instanceof String) {
                id = p.toString();
            } else if (p instanceof ApiSearchRequest) {
                id = ((ApiSearchRequest) p).getId();
            }
        }
        if (!ProductionConfig.EMPTY_STRING.equals(id)) {
            UpdateRequest updateRequest = new UpdateRequest(ProductionConfig.ES_INDEX_NAME,ProductionConfig.ES_INDEX_TYPE,id)
                    .script(new Script("ctx._source.viewCount++"));
            this.esClient.update(updateRequest).get();
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
            /*String requestParams = params.split("(?<=}, )")[0].replace("[{","{").replace("}, ","}");
            requestParams = requestParams.replaceAll("\"source\":\".*\",","");*/
            int lastBreakLine = params.lastIndexOf("\n");
            if (lastBreakLine != -1) {
                params = params.substring(0,lastBreakLine) + params.substring(lastBreakLine+1);
            }
            String requestParams = params.replaceAll("\n",",");
            TextUtils.replaceRedundantLogText(requestParams);

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
}
