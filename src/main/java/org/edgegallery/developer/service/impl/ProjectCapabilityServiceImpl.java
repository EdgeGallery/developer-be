/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.edgegallery.developer.mapper.ProjectCapabilityMapper;
import org.edgegallery.developer.model.workspace.ApplicationProjectCapability;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectCapabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spencerwi.either.Either;

@Service
public class ProjectCapabilityServiceImpl implements ProjectCapabilityService {
	@Inject
	private ProjectCapabilityMapper projectCapabilityMapper;

	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	@Override
	public Either<FormatRespDto, ApplicationProjectCapability> create(ApplicationProjectCapability projectCapability) {
		int ret = projectCapabilityMapper.insert(projectCapability);
		if (ret <= 0) {
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "insert projectCapability failed."));
		}
		return Either.right(projectCapability);
	}

	@Override
	public Either<FormatRespDto, Boolean> delete(ApplicationProjectCapability projectCapability) {
		int ret = projectCapabilityMapper.delete(projectCapability);
		if (ret <= 0) {
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "delete projectCapability failed."));
		}
		return Either.right(true);
	}

	@Override
	public Either<FormatRespDto, Boolean> deleteByProjectId(String projectId) {
		int ret = projectCapabilityMapper.deleteByProjectId(projectId);
		if (ret <= 0) {
			return Either.left(new FormatRespDto(Status.BAD_REQUEST, "delete projectCapability by projectId failed."));
		}
		return Either.right(true);
	}

	@Override
	public List<ApplicationProjectCapability> findByProjectId(String projectId) {
		return projectCapabilityMapper.selectByProjectId(projectId);
	}

	@Override
	public Either<FormatRespDto, List<ApplicationProjectCapability>> create(
			List<ApplicationProjectCapability> projectCapabilities) {
		SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
		List<ApplicationProjectCapability> result = new ArrayList<>();
		for (ApplicationProjectCapability projectCapability : projectCapabilities) {
			int insertCount = projectCapabilityMapper.insert(projectCapability);
			if (insertCount > 0) {
				result.add(projectCapability);
			}
		}
		sqlSession.commit();
		return Either.right(result);
	}
}
