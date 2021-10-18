package org.edgegallery.developer.model.apppackage.appd;

import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Setter
@Getter
@JsonPropertyOrder(alphabetic = true)
public class PlacementGroupProperty {

    @Valid
    @NotEmpty
    private String description;

}
