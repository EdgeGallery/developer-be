/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service.capability;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.capability.ApplicationProjectCapability;
import org.edgegallery.developer.model.restful.FormatRespDto;

import com.spencerwi.either.Either;

public interface ProjectCapabilityService {
	public Either<FormatRespDto, ApplicationProjectCapability> create(ApplicationProjectCapability projectCapability);
	
	public Either<FormatRespDto, List<ApplicationProjectCapability>> create(List<ApplicationProjectCapability> projectCapabilities);
	
	public Either<FormatRespDto, Boolean> delete(ApplicationProjectCapability projectCapability);

	public Either<FormatRespDto, Boolean> deleteByProjectId(@Param("projectId") String projectId);

	public List<ApplicationProjectCapability> findByProjectId(@Param("projectId") String projectId);
}
