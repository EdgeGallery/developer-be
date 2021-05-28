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

package org.edgegallery.developer.mapper;

import org.edgegallery.developer.model.vm.VmSystem;
import org.edgegallery.developer.model.workspace.MepGetSystemImageReq;

import java.util.List;

public interface SystemImageMapper {

    Integer getSystemImagesCount(MepGetSystemImageReq mepGetSystemImageReq);

    List<VmSystem> getSystemImagesByCondition(MepGetSystemImageReq mepGetSystemImageReq);

    int createSystemImage(VmSystem VmSystem);

    int updateSystemImage(VmSystem VmSystem);

    int deleteSystemImage(VmSystem VmSystem);

    int publishSystemImage(VmSystem vmSystem);

    int updateSystemImageStatus(VmSystem vmSystem);
}
