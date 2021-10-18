package org.edgegallery.developer.model.apppackage.appd;

import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Setter
@Getter
@JsonPropertyOrder(alphabetic = true)
public class Metadata {

    @Valid
    @NotBlank
    @JsonProperty(value = "template_name")
    private String templateName;

    @Valid
    @NotBlank
    @JsonProperty(value = "template_author")
    private String templateAuthor;

    @Valid
    @NotBlank
    @JsonProperty(value = "template_version")
    private String templateVersion;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfm_type")
    private String vnfmType;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_id")
    private String vnfdId;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_version")
    private String vnfdVersion;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_name")
    private String vnfdName;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_description")
    private String vnfdDescription;

}
