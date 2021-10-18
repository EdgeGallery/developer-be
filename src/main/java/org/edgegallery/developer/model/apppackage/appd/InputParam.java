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
public class InputParam {

    @Valid
    @NotBlank
    private String type;

    @Valid
    @JsonProperty(value = "default")
    private Object defaultValue;

    @Valid
    private String description;

}
