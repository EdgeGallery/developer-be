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

package org.edgegallery.developer.service.application.vm;

import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.restful.OperationInfoRep;

public interface VMAppOperationService {

    /**
     * instantiate vm.
     *
     * @param applicationId vm application
     * @param vmId vm id
     * @param user operator
     * @return
     */
    OperationInfoRep instantiateVM(String applicationId, String vmId, User user);

    /**
     * upload vm image.
     *
     * @param applicationId vm application
     * @param vmId vm id
     * @param request upload request
     * @param chunk upload file chunk
     * @return
     */
    Boolean uploadFileToVm(String applicationId, String vmId, HttpServletRequest request, Chunk chunk);

    /**
     * merge vm image.
     *
     * @param applicationId vm application
     * @param vmId vm id
     * @param fileName upload file name
     * @param identifier upload file identifier
     * @return
     */
    Boolean mergeAppFile(String applicationId, String vmId, String fileName, String identifier);

    /**
     * prepare to export image.
     *
     * @param applicationId vm application
     * @param vmId vm id
     * @param user operator
     * @return
     */
    OperationInfoRep createVmImage(String applicationId, String vmId, User user);

    /**
     * get vm image export info.
     *
     * @param vmId vm id
     * @return
     */
    ImageExportInfo getImageExportInfo(String vmId);

    /**
     * get vm instantiate info.
     *
     * @param vmId vm id
     * @return
     */
    VMInstantiateInfo getInstantiateInfo(String vmId);

    /**
     * save vm instantiate info.
     *
     * @param vmId vm id
     * @param instantiateInfo instantiate info
     * @return
     */
    Boolean createInstantiateInfo(String vmId, VMInstantiateInfo instantiateInfo);

    /**
     * clean vm test environment.
     *
     * @param mepHostId host id
     * @param vm vm
     * @param user operator
     */
    void cleanVmLaunchInfo(String mepHostId, VirtualMachine vm, User user);

    /**
     * delete vm instantiate info.
     *
     * @param vmId vm id
     */
    void deleteInstantiateInfo(String vmId);

    /**
     * save vm image export info.
     *
     * @param vmId vm id
     * @param imageExportInfo vm image export info
     * @return
     */
    Boolean createExportInfo(String vmId, ImageExportInfo imageExportInfo);

    /**
     * update vm image export info.
     *
     * @param vmId vm id
     * @param imageExportInfo vm image export info
     * @return
     */
    Boolean modifyExportInfo(String vmId, ImageExportInfo imageExportInfo);

    /**
     * delete vm image export info.
     *
     * @param vmId vm id
     * @return
     */
    Boolean deleteExportInfo(String vmId);

    /**
     * update vm instantiate info.
     *
     * @param vmId vm id
     * @param instantiateInfo instantiateInfo
     * @return
     */
    Boolean updateInstantiateInfo(String vmId, VMInstantiateInfo instantiateInfo);

    /**
     * generate vm package.
     *
     * @param application vm application
     * @return
     */
    AppPackage generatePackage(VMApplication application);

}
