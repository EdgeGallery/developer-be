package org.edgegallery.developer.model.apppackage.appd;

import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Setter
@Getter
@JsonPropertyOrder(alphabetic = true)
public class TopologyTemplate {

    @Valid
    @NotNull
    private LinkedHashMap<String, InputParam> inputs;

    @Valid
    @NotNull
    @JsonProperty(value = "node_templates")
    private LinkedHashMap<String, NodeTemplate> nodeTemplates;

    @Valid
    private LinkedHashMap<String, PlacementGroup> groups;

    @Valid
    private List<LinkedHashMap<String, AntiAffinityRule>> policies;

}
