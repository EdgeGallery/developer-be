package org.edgegallery.developer.model.restful;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class VMImageQuery {
    @Min(value = -1)
    private int offset;

    @Min(value = 0)
    @Max(value = 100)
    private int limit;

    @ApiModelProperty(example = "userName")
    @Pattern(regexp = "(?i)userName|(?i)createTime")
    private String sortBy;

    @ApiModelProperty(example = "ASC")
    @Pattern(regexp = "(?i)ASC|(?i)DESC")
    private String sortOrder;

}
