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

package org.edgegallery.developer.model.resource.mephost;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
@Getter
@Setter
public class MepHost {

    private String id;

    @NotBlank
    @Length(min = 6, max = 50)
    private String name;

    @NotBlank
    private String lcmIp;

    @NotBlank
    private String lcmProtocol;

    @Range(min = 30000, max = 32000)
    private int lcmPort;

    @NotBlank
    private String architecture;

    @NotNull
    private EnumMepHostStatus status;

    @NotBlank
    private String mecHostIp;

    @NotNull
    private EnumVimType vimType;

    private String mecHostUserName;

    private String mecHostPassword;

    private int mecHostPort = 22;

    private String userId;

    private String configId;

    private String networkParameter;

    private String resource;

    @NotBlank
    private String address;

}
