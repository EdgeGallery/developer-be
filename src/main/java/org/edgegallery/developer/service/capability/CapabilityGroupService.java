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

package org.edgegallery.developer.service.capability;

import java.util.List;

import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;

import com.spencerwi.either.Either;

public interface CapabilityGroupService {
	public Either<FormatRespDto, CapabilityGroup> create(CapabilityGroup capabilityGroup);

	public Either<FormatRespDto, CapabilityGroup> updateById(CapabilityGroup capabilityGroup);

	public Either<FormatRespDto, String> deleteById(String id);

	public List<CapabilityGroup> findAll();
	
	public List<CapabilityGroup> findByType(String type);

	public CapabilityGroup findById(String id);
	
	public CapabilityGroup findByName(String name);
	
	public List<CapabilityGroup> findByNameOrNameEn(String name,String nameEn);
}