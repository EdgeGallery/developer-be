/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("hostService")
public class HostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostService.class);

    @Autowired
    private HostMapper hostMapper;

    /**
     * getALlHosts.
     *
     * @return
     */
    public Either<FormatRespDto, List<MepHost>> getAllHosts(String userId) {
        List<MepHost> list;
        if (StringUtils.isNoneBlank(userId)) {
            list = hostMapper.getHostsByUserId(userId);
        } else {
            list = hostMapper.getAllHosts();
        }
        LOGGER.info("Get all hosts success.");
        return Either.right(list);
    }

    /**
     * createHost.
     *
     * @return
     */
    public Either<FormatRespDto, MepHost> createHost(MepHost host) {
        host.setHostId(UUID.randomUUID().toString()); // no need to set hostId by user
        host.setProtocol("https");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32000);
        List<MepHost> hostList = hostMapper.getHostsByUserId(host.getUserId());
        if (hostList == null || hostList.isEmpty()) {
            int ret = hostMapper.saveHost(host);
            if (ret > 0) {
                LOGGER.info("Crete host {} success ", host.getHostId());
                return Either.right(hostMapper.getHostsByUserId(host.getUserId()).get(0));
            }
        } else {
            int ret = hostMapper.updateHost(host);
            if (ret > 0) {
                LOGGER.info("Update host {} success", host.getIp());
                return Either.right(hostMapper.getHostsByUserId(host.getUserId()).get(0));
            }
        }
        LOGGER.error("Create host failed ");
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Can not create a host."));
    }

    /**
     * deleteHost.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteHost(String hostId) {
        int res = hostMapper.deleteHost(hostId);
        if (res < 1) {
            LOGGER.error("Delete host {} failed", hostId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "delete failed.");
            return Either.left(error);
        }
        LOGGER.info("Delete host {} success", hostId);
        return Either.right(true);
    }

    /**
     * updateHost.
     *
     * @return
     */
    public Either<FormatRespDto, MepHost> updateHost(String hostId, MepHost host) {
        MepHost currentHost = hostMapper.getHost(hostId);
        if (currentHost == null) {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }

        host.setHostId(hostId); // no need to set hostId by user
        host.setUserId(currentHost.getUserId());
        int ret = hostMapper.updateHost(host);
        if (ret > 0) {
            LOGGER.info("Update host {} success", hostId);
            return Either.right(hostMapper.getHostsByUserId(host.getUserId()).get(0));
        }
        LOGGER.error("Update host {} failed", hostId);
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Can not update the host"));
    }

    /**
     * getHost.
     *
     * @return
     */
    public Either<FormatRespDto, MepHost> getHost(String hostId) {
        MepHost host = hostMapper.getHost(hostId);
        if (host != null) {
            LOGGER.info("Get host {} success", hostId);
            return Either.right(host);
        } else {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }
    }
}
