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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.AppOperationService;
import org.springframework.http.ResponseEntity;
import com.spencerwi.either.Either;

public interface VMAppOperationService {

    OperationInfoRep instantiateVM(String applicationId, String vmId, String accessToken);

    Boolean uploadFileToVm(String applicationId, String vmId, HttpServletRequest request, Chunk chunk);

    ResponseEntity mergeAppFile(String applicationId, String vmId, String fileName, String identifier);

    OperationInfoRep createVmImage(String applicationId, String vmId, String accessToken);

    ImageExportInfo getImageExportInfo(String vmId);

    VMInstantiateInfo getInstantiateInfo(String vmId);

    Boolean createInstantiateInfo(String vmId, VMInstantiateInfo instantiateInfo);

    Boolean updateInstantiateInfo(String vmId, VMInstantiateInfo instantiateInfo);

    AppPackage generatePackage(VMApplication application);

}
