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

package org.edgegallery.developer.model.deployyaml;

public class DeployYamls {
    private DeployYaml[] deployYamls;

    /**
     * getDeployYamls.
     *
     * @return
     */
    public DeployYaml[] getDeployYamls() {
        if (deployYamls != null) {
            return deployYamls.clone();
        }
        return new DeployYaml[0];
    }

    /**
     * setDeployYamls.
     *
     * @param deployYamls deployYamls
     */
    public void setDeployYamls(DeployYaml[] deployYamls) {
        if (deployYamls != null) {
            this.deployYamls = deployYamls.clone();
        } else {
            this.deployYamls = null;
        }
    }
}
