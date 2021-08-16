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

package org.edgegallery.developer.service.capability.impl;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.capability.CapabilityGroupService;
import org.edgegallery.developer.service.capability.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.spencerwi.either.Either;

@Service("v2-capabilityService")
public class CapabilityServiceImpl implements CapabilityService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityServiceImpl.class);
	@Autowired
	private CapabilityMapper capabilityMapper;
	@Autowired
	private CapabilityGroupService capbilityGroupService;
	@Autowired
	private UploadedFileMapper uploadedFileMapper;

	@Transactional(rollbackFor = RuntimeException.class)
	@Override
	public Either<FormatRespDto, Capability> create(Capability capability) {
		String name = capability.getName();
		if (StringUtils.isBlank(name)) {
			LOGGER.error("Create capability {} , name is null", capability.getName());
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Api file id is wrong"));
		}

		if (StringUtils.isEmpty(capability.getDescriptionEn())) {
			capability.setDescriptionEn(capability.getDescription());
		}

		if (StringUtils.isEmpty(capability.getNameEn())) {
			capability.setNameEn(capability.getName());
		}

		if (StringUtils.isBlank(capability.getApiFileId())) {
			LOGGER.error("Create capability {} , api file id is null", name);
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Api file id is wrong"));
		}
		if (StringUtils.isBlank(capability.getGuideFileId())) {
			LOGGER.error("Create capability {} failed, guide file id is null", name);
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Guide file id is wrong"));
		}

		// CapabilityGroup check begin
		CapabilityGroup group = capability.getGroup();
		if (group == null) {
			LOGGER.info("The capability {} group is null.", name);
			return Either.right(capability);
		}

		String groupId = group.getId();
		if (StringUtils.isEmpty(groupId)) {
			// Create CapibilityGroup
			List<CapabilityGroup> findGroups = capbilityGroupService.findByNameOrNameEn(group.getName(),
					group.getNameEn());
			if (CollectionUtils.isEmpty(findGroups)) {
				// not exist,then create it.
				Either<FormatRespDto, CapabilityGroup> result = capbilityGroupService.create(group);
				if (result.isLeft()) {
					return Either.left(result.getLeft());
				}
			} else {
				if (findGroups.size() == 1) {
					CapabilityGroup findGroup = findGroups.get(0);
					capability.setGroupId(findGroup.getId());
				} else {
					LOGGER.error("Find capability group size {} ,{}/{} is conflict.", findGroups.size(),
							group.getName(), group.getNameEn());
					return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Guide file id is wrong"));
				}
			}
		} else {
			// Validate the groupId
			CapabilityGroup capabilityGroup = capbilityGroupService.findById(groupId);
			if (capabilityGroup == null) {
				LOGGER.error("Create capability {} failed,capability group id {} is invalid.", name, groupId);
				return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Guide file id is wrong"));
			}
		}
		// CapbilityGroup check end

		List<Capability> findedCapabilities = capabilityMapper.selectByNameOrNameEn(name, capability.getNameEn());
		if (!CollectionUtils.isEmpty(findedCapabilities)) {
			LOGGER.error("The capability name {} has exist.", capability.getName());
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "The capability is exist"));
		}

		capability.setUploadTime(System.currentTimeMillis());
		capability.setId(UUID.randomUUID().toString());
		int result = capabilityMapper.insert(capability);
		if (result > 0) {
			LOGGER.info("Create capability {} success", capability.getName());
			// update api file to un temp
			int api = uploadedFileMapper.updateFileStatus(capability.getApiFileId(), false);
			int guide = uploadedFileMapper.updateFileStatus(capability.getGuideFileId(), false);
			int guideEn = uploadedFileMapper.updateFileStatus(capability.getGuideFileIdEn(), false);
			if (api <= 0 || guide <= 0 || guideEn <= 0) {
				String msg = "update api or guide or guide-en file status occur db error";
				LOGGER.error(msg);
				throw new DeveloperException(msg);
			}
		} else {
			LOGGER.error("save capability {} failed!", capability.getName());
			throw new DeveloperException("Create capability failed");
		}
		return Either.right(capability);
	}

	@Transactional
	@Override
	public Either<FormatRespDto, Capability> updateById(Capability capability) {
		capability.setUploadTime(System.currentTimeMillis());
		try {
			int ret = capabilityMapper.updateById(capability);
			if (ret <= 0) {
				LOGGER.error("Update capability {} failed!{}", capability.getId(), capability.getName());
				return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Update capability failed."));
			}
			int api = uploadedFileMapper.updateFileStatus(capability.getApiFileId(), false);
			int guide = uploadedFileMapper.updateFileStatus(capability.getGuideFileId(), false);
			int guideEn = uploadedFileMapper.updateFileStatus(capability.getGuideFileIdEn(), false);
			if (api <= 0 || guide <= 0 || guideEn <= 0) {
				String msg = "update api or guide or guide-en file status occur db error";
				return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, msg));
			}
		} catch (Exception ex) {
			return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, ex.getMessage()));
		}

		return Either.right(capability);
	}

	@Transactional(rollbackFor = RuntimeException.class)
	@Override
	public Either<FormatRespDto, Capability> deleteById(String id) {
		Capability capability = capabilityMapper.selectById(id);
		if (capability == null) {
			LOGGER.error("Delete capability {} is not exist!", id);
			Capability notExistCapability = new Capability();
			notExistCapability.setId(id);
			return Either.right(notExistCapability);
		}

		int ret = capabilityMapper.deleteById(id);
		if (ret <= 0) {
			LOGGER.error("Delete capability {} failed!", id);
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Delete capability failed."));
		}

		uploadedFileMapper.updateFileStatus(capability.getApiFileId(), true);
		uploadedFileMapper.updateFileStatus(capability.getGuideFileId(), true);
		uploadedFileMapper.updateFileStatus(capability.getGuideFileIdEn(), true);

		String groupId = capability.getGroupId();
		if (StringUtils.isEmpty(groupId)) {
			LOGGER.info("Delete capability {} success", capability.getName());
			return Either.right(capability);
		}

		List<Capability> remainCapabilities = this.findByGroupId(groupId);
		if (remainCapabilities.isEmpty()) {
			Either<FormatRespDto, String> deleteCapabilityGroupResult = capbilityGroupService.deleteById(groupId);
			if (deleteCapabilityGroupResult.isLeft()) {
				return Either.left(deleteCapabilityGroupResult.getLeft());
			}
		}

		LOGGER.info("Delete capability {} success", capability.getName());
		return Either.right(capability);
	}

	@Override
	public List<Capability> findAll() {
		return capabilityMapper.selectAll();
	}

	@Override
	public Capability findById(String id) {
		return capabilityMapper.selectById(id);
	}

	@Override
	public List<Capability> findByGroupId(String groupId) {
		return capabilityMapper.selectByGroupId(groupId);
	}

	@Override
	public List<Capability> findByProjectId(String projectId) {
		return capabilityMapper.selectByProjectId(projectId);
	}

	@Override
	public Capability findByName(String name) {
		return capabilityMapper.selectByName(name);
	}

	@Override
	public List<Capability> findByNameWithFuzzy(String name) {
		return capabilityMapper.selectByNameWithFuzzy(name);
	}

	@Override
	public List<Capability> findByNameEnWithFuzzy(String nameEn) {
		return capabilityMapper.selectByNameEnWithFuzzy(nameEn);
	}

	@Override
	public List<Capability> findByNameOrNameEn(String name, String nameEn) {
		return capabilityMapper.selectByNameOrNameEn(name, nameEn);
	}

	@Override
	public boolean updateSelectCountByIds(List<String> ids) {
		if(CollectionUtils.isEmpty(ids)) {
			return true;
		}
		
		return capabilityMapper.updateSelectCountByIds(ids)>0;
	}
}
