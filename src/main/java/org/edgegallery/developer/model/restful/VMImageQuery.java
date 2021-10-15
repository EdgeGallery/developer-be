/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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
