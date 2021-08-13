package org.edgegallery.developer.service.impl;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.edgegallery.developer.mapper.ProjectCapabilityMapper;
import org.edgegallery.developer.model.workspace.ApplicationProjectCapability;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectCapabilityService;
import org.springframework.stereotype.Service;

import com.spencerwi.either.Either;

@Service
public class ProjectCapabilityServiceImpl implements ProjectCapabilityService {
	@Inject
	private ProjectCapabilityMapper projectCapabilityMapper;

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

}
