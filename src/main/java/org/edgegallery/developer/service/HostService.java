package org.edgegallery.developer.service;

import java.util.List;

import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.workspace.MepCreateHost;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.response.FormatRespDto;

import com.spencerwi.either.Either;

public interface HostService {
	public Page<MepHost> getAllHosts(String userId, String name, String ip, int limit, int offset);

	public Either<FormatRespDto, Boolean> createHost(MepCreateHost host, String token);

	public Either<FormatRespDto, Boolean> deleteHost(String hostId);

	public Either<FormatRespDto, Boolean> updateHost(String hostId, MepCreateHost host, String token);

	public Either<FormatRespDto, MepHost> getHost(String hostId);

	public Either<FormatRespDto, List<MepHostLog>> getHostLogByHostId(String hostId);
}
