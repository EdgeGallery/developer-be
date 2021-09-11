/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.model.application.vm;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;

@Getter
@Setter
@ToString
public class VirtualMachine {

    private String id;

    private String name;

    private String flavorId;

    private String imageId;

    private VMCertificate vmCertificate;

    private String userData;

    private List<VMPort> portList;

    private String status;

    private String areaZone;

    private String flavorExtraSpecs;

    private VMInstantiateInfo vmInstantiateInfo;

    private ImageExportInfo imageExportInfo;
}
