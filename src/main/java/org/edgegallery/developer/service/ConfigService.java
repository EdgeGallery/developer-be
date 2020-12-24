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
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.developer.model.DeployPlatformConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.springframework.stereotype.Service;

@Service("configService")
public class ConfigService {

    private Map<String, String> virtualMachineUrl = new HashMap<>();

    private Map<String, Boolean> isVirtualMachine = new HashMap<>();

    /**
     * config deploy platform.
     *
     * @return
     */
    public Either<FormatRespDto, DeployPlatformConfig> configDeployPlatform(DeployPlatformConfig deployPlatformConfig) {
        if (deployPlatformConfig.getIsVirtualMachine()) {
            if (deployPlatformConfig.getVirtualMachineUrl().equals("")
                || deployPlatformConfig.getVirtualMachineUrl() == null) {
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "openStack url is null");
                return Either.left(error);
            }
            isVirtualMachine.put("isVirtualMachine", true);
            virtualMachineUrl.put("virtualMachineUrl", deployPlatformConfig.getVirtualMachineUrl());
        }
        isVirtualMachine.put("isVirtualMachine", false);
        virtualMachineUrl.put("virtualMachineUrl", "");

        DeployPlatformConfig response = new DeployPlatformConfig();
        response.setIsVirtualMachine(isVirtualMachine.get("isVirtualMachine"));
        response.setVirtualMachineUrl(virtualMachineUrl.get("virtualMachineUrl"));

        return Either.right(response);
    }

    /**
     * get deploy platform config.
     *
     * @return
     */
    public Either<FormatRespDto, DeployPlatformConfig> getConfigDeployPlatform() {

        if (isVirtualMachine.isEmpty() || isVirtualMachine.get("isVirtualMachine") == null) {
            isVirtualMachine.put("isVirtualMachine", false);
            virtualMachineUrl.put("virtualMachineUrl", "");
        }

        DeployPlatformConfig response = new DeployPlatformConfig();
        response.setIsVirtualMachine(isVirtualMachine.get("isVirtualMachine"));
        response.setVirtualMachineUrl(virtualMachineUrl.get("virtualMachineUrl"));

        return Either.right(response);
    }
}
