package org.edgegallery.developer.config.ratelimit;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class GarbageCollector {
    private final LimitAccessAspect limitAccessAspect;

    public GarbageCollector(LimitAccessAspect limitAccessAspect) {
        this.limitAccessAspect = limitAccessAspect;
    }

    /**
     * executed every 20 minutes.
     */
    @Scheduled(cron = "0 0/20 * * * ?")
    public void clean() {
        //delete user access interface records stored in LimitAccessAspect
        List<String> expiredKeyList = new ArrayList<>();
        long now = System.currentTimeMillis();
        limitAccessAspect.getLimitMap().forEach((key, millisecondList) -> {
            for (long millisecond : millisecondList) {
                // current time - access time < 10 minutes - not delete
                if (now - millisecond < 600000) {
                    break;
                }
                expiredKeyList.add(key);
            }
        });
        // delete expired data
        if (!expiredKeyList.isEmpty()) {
            log.info("delete expired data, number：{}", expiredKeyList.size());
        }
        expiredKeyList.forEach(key -> limitAccessAspect.getLimitMap().remove(key));
    }
}
