package org.edgegallery.developer.model.apppackage.appd;

import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Setter
@Getter
@JsonPropertyOrder(alphabetic = true)
public class NodeTemplate {

    @Valid
    @NotBlank
    private String type;

    private Object capabilities;

    private Object properties;

    private Object attributes;

    private Object requirements;

}
