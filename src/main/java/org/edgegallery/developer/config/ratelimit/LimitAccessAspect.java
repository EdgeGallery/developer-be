package org.edgegallery.developer.config.ratelimit;

import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Log4j2
@Order
public class LimitAccessAspect {
    @Getter
    private Map<String, List<Long>> limitMap = new HashMap<>();

    @Pointcut("@annotation(limitAccess)")
    public void limitAccessPointCut(LimitAccess limitAccess) {

    }

    @Around(value = "limitAccessPointCut(limitAccess)", argNames = "point,limitAccess")
    public Object doAround(ProceedingJoinPoint point, LimitAccess limitAccess) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null != attributes) {
            String className = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            HttpServletRequest request = attributes.getRequest();
            String key = className + "." + methodName + "#" + request.getSession().getId();
            List<Long> millisecondList = limitMap.get(key);
            long now = System.currentTimeMillis();
            if (null == millisecondList) {
                List<Long> list = new ArrayList<>();
                list.add(now);
                limitMap.put(key, list);
            } else {
                List<Long> newMillisecondList = new ArrayList<>(millisecondList.size());
                millisecondList.forEach(millisecond -> {
                    // current access time - history access time < limit time
                    if (now - millisecond < limitAccess.millisecond()) {
                        newMillisecondList.add(millisecond);
                    }
                });
                // exceed the upper limit of the access frequency - blocking
                if (newMillisecondList.size() >= limitAccess.frequency()) {
                    log.info("interface invoking is too frequent {}", key);
                    FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST,
                        "interface invoking is too frequent!");
                    return ResponseDataUtil.buildResponse(Either.left(error));
                }
                newMillisecondList.add(now);
                // update interface access records
                limitMap.put(key, newMillisecondList);
            }
        }
        return point.proceed();
    }
}
