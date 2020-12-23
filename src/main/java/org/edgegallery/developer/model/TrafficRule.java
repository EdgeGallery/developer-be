package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficRule {

    private String trafficRuleId;

    private String action;

    private String priority;

    private String filterType;

    private List<TrafficFilter> trafficFilter;

    private List<DstInterface> dstInterface;

}
