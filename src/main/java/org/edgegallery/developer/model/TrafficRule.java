package org.edgegallery.developer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenhui
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficRule {

    private String trafficRuleId;

    private String action;

    private String priority;

    private String filterType;

    private List<TrafficFilter> trafficFilter;

    private DstInterface dstInterface;

}
