package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import java.util.List;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.workspace.MepCreateHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.response.FormatRespDto;

public interface HostService {
    Page<MepHost> getAllHosts(String userId, String name, String ip, int limit, int offset);

    Page<MepHost> selectAllHosts(String os, String architecture, int limit, int offset);

    Either<FormatRespDto, Boolean> createHost(MepCreateHost host, String token);

    Either<FormatRespDto, Boolean> deleteHost(String hostId);

    Either<FormatRespDto, Boolean> updateHost(String hostId, MepCreateHost host, String token);

    Either<FormatRespDto, MepHost> getHost(String hostId);

    Either<FormatRespDto, List<MepHostLog>> getHostLogByHostId(String hostId);
}
