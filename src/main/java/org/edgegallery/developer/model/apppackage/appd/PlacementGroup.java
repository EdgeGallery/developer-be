package org.edgegallery.developer.model.apppackage.appd;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Setter
@Getter
@JsonPropertyOrder(alphabetic = true)
public class PlacementGroup {

    @Valid
    @NotBlank
    private String type;

    @Valid
    @NotNull
    private PlacementGroupProperty properties;

    @Valid
    @NotNull
    private List<String> members;

}
