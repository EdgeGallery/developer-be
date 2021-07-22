package org.edgegallery.developer.config.ratelimit;

import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class CurrentLimitInterceptor implements HandlerInterceptor {
    private final static String SEPARATOR = "-";

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //Get method RequestLimit annotation through HandlerMethod
            RequestLimit currentLimit = handlerMethod.getMethodAnnotation(RequestLimit.class);
            //If this method has a current limit annotation
            if (currentLimit != null) {
                int number = currentLimit.number();
                long time = currentLimit.time();
                //If the number of times and the time limit are greater than 0,
                // it proves that the current limit is required here
                if (time > 0 && number > 0) {
                    //What can be defined here is the project path + API path + ip,
                    // of course I didn't get the actual ip here.
                    // The key can be set according to the actual scene of your project
                    String key = request.getContextPath() + SEPARATOR + request.getServletPath() + SEPARATOR + "ip";
                    //Get the number of visits in the reids cache
                    Long numberRedis = (Long) redisTemplate.opsForValue().get(key);
                    //If it is the first visit, set the number of times this ip visits this API to 1,
                    // and set the expiration time to the time in the comment
                    if (null == numberRedis) {
                        redisTemplate.opsForValue().set(key, 1L, time, TimeUnit.SECONDS);
                        return true;
                    }
                    //If the number of visits is greater than the annotation setting, an exception will be thrown
                    if (numberRedis >= number) {
                        throw new RuntimeException("Frequent requests, please try again later!");
                    }
                    //Update the cache times if the current limit condition is met
                    redisTemplate.opsForValue().set(key, numberRedis + 1);
                }
            }
        }
        return true;
    }

}
