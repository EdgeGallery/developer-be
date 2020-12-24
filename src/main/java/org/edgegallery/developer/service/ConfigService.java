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

package org.edgegallery.developer.service;


import com.spencerwi.either.Either;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.DeployPlatformConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.springframework.stereotype.Service;

@Service("configService")
public class ConfigService {

    /**
     * config deploy platform
     *
     * @return
     */
    public Either<FormatRespDto, DeployPlatformConfig> configDeployPlatform(
        DeployPlatformConfig deployPlatformConfig) {
        if (deployPlatformConfig.getIsVirtualMachine()) {
            if (deployPlatformConfig.getVirtualMachineUrl().equals("")
                || deployPlatformConfig.getVirtualMachineUrl() == null) {
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "openStack url is null");
                return Either.left(error);
            }
            Consts.IS_VIRTUAL_MACHINE.put("isVirtualMachine", true);
            Consts.VIRTUAL_MACHINE_URL.put("virtualMachineUrl", deployPlatformConfig.getVirtualMachineUrl());
        }
        Consts.IS_VIRTUAL_MACHINE.put("isVirtualMachine", false);
        Consts.VIRTUAL_MACHINE_URL.put("virtualMachineUrl", "");

        DeployPlatformConfig response = new DeployPlatformConfig();
        response.setIsVirtualMachine(Consts.IS_VIRTUAL_MACHINE.get("isVirtualMachine"));
        response.setVirtualMachineUrl(Consts.VIRTUAL_MACHINE_URL.get("virtualMachineUrl"));

        return Either.right(response);
    }

    /**
     * get deploy platform config
     *
     * @return
     */
    public Either<FormatRespDto, DeployPlatformConfig> getConfigDeployPlatform() {

        if (Consts.IS_VIRTUAL_MACHINE.isEmpty() || Consts.IS_VIRTUAL_MACHINE.get("isVirtualMachine") == null) {
            Consts.IS_VIRTUAL_MACHINE.put("isVirtualMachine", false);
            Consts.VIRTUAL_MACHINE_URL.put("virtualMachineUrl", "");
        }

        DeployPlatformConfig response = new DeployPlatformConfig();
        response.setIsVirtualMachine(Consts.IS_VIRTUAL_MACHINE.get("isVirtualMachine"));
        response.setVirtualMachineUrl(Consts.VIRTUAL_MACHINE_URL.get("virtualMachineUrl"));

        return Either.right(response);
    }
}
