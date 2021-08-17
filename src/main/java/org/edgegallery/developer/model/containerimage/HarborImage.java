package org.edgegallery.developer.model.containerimage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HarborImage {

    @JsonProperty(value = "artifact_count")
    private Integer artifactCount;

    @JsonProperty(value = "creation_time")
    private String creationTime;

    private Integer id;

    private String name;

    @JsonProperty(value = "project_id")
    private Integer projectId;

    @JsonProperty(value = "pull_count")
    private Integer pullCount;

    @JsonProperty(value = "update_time")
    private String updateTime;


}
