package org.edgegallery.developer.model.apppackage.appd;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Getter
@Setter
@JsonPropertyOrder(alphabetic = true)
public class APPDDefinition {
    @Valid
    @NotBlank
    @JsonProperty(value = "tosca_definitions_version")
    private String toscaDefinitionVersion;

    private String description;

    @Valid
    @NotEmpty
    private List<String> imports;

    @Valid
    @NotNull
    private Metadata metadata;

    @Valid
    @NotBlank
    @JsonProperty(value = "topology_template")
    private TopologyTemplate topologyTemplate;

}
