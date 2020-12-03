package org.edgegallery.developer.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TrafficRule {

    private String trafficRuleId;

    private String action;

    private String priority;

    private String filterType;

    private List<TrafficFilter> trafficFilters;

}
