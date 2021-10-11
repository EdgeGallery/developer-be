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
