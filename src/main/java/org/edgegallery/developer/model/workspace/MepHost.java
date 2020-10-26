/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.model.workspace;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class MepHost {

    private String hostId;

    @NotBlank
    @Length(min = 6, max = 50)
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String architecture;

    @NotNull
    private EnumHostStatus status;

    @NotBlank
    private String ip;

    private String protocol;

    @Range(min = 30000, max = 30400)
    private int port;

    private String os;

    private int portRangeMin;

    private int portRangeMax;

}
