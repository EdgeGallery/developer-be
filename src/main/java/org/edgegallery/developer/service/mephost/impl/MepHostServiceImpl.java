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

package org.edgegallery.developer.service.mephost.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spencerwi.either.Either;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.mephost.MepHostLogMapper;
import org.edgegallery.developer.mapper.mephost.MepHostMapper;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.mephost.MepHostLog;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.MepHostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service("mepHostService")
public class MepHostServiceImpl implements MepHostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MepHostServiceImpl.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static int VNC_PORT = 22;

    @Autowired
    private MepHostMapper mepHostMapper;

    @Autowired
    private MepHostLogMapper hostLogMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    /**
     * getALlHosts.
     *
     * @return
     */
    @Override
    public Page<MepHost> getAllHosts(String name, String vimType, String architecture, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<MepHost> pageInfo = new PageInfo<>(mepHostMapper.getHostsByCondition(name, vimType, architecture));
        LOGGER.info("Get all hosts success.");
        return new Page<MepHost>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * createHost.
     *
     * @return
     */
    @Transactional
    @Override
    public Either<FormatRespDto, Boolean> createHost(MepHost host, String token) {
        MepHost mepHost = mepHostMapper.getHostsByMecHostIp(host.getMecHostIp());
        if (mepHost != null) {
            LOGGER.info("mecHost have exit:{}", host.getMecHostIp());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "mecHost have exit"));
        }
        if (StringUtils.isBlank(host.getUserId()) || !isAdminUser() ) {
            LOGGER.error("Create host failed, userId is empty or not admin");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "userId is empty or not admin"));
        }
        if ("OpenStack".equals(host.getVimType())) {
            Map<String, String> getParams = InputParameterUtil.getParams(host.getNetworkParameter());
            if (!getParams.containsKey("app_mp1_ip") || !getParams.containsKey("app_n6_ip") || !getParams
                .containsKey("app_internet_ip")) {
                LOGGER.error("Network params config error");
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Network params config error"));
            }
        }
        // health check
        String healRes = HttpClientUtil.getHealth(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort());
        if (healRes == null) {
            String msg = "health check faild,current ip or port cann't be used!";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // add mechost to lcm
        boolean addMecHostRes = MepHostUtil.addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // upload config file
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = MepHostUtil
                .uploadFileToLcm(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort(), uploadedFile.getFilePath(),
                    host.getMecHostIp(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
                return Either.left(dto);
            }
        }
        host.setId(UUID.randomUUID().toString()); // no need to set hostId by user
        host.setMecHostPort(VNC_PORT);
        int ret = mepHostMapper.createHost(host);
        if (ret > 0) {
            LOGGER.info("Crete host {} success ", host.getId());
            return Either.right(true);
        }
        LOGGER.error("Create host failed ");
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not create a host."));
    }

    /**
     * deleteHost.
     *
     * @return
     */
    @Transactional
    @Override
    public Either<FormatRespDto, Boolean> deleteHost(String hostId) {
        int res = mepHostMapper.deleteHost(hostId);
        if (res < 1) {
            LOGGER.error("Delete host {} failed", hostId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "delete failed.");
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
    @Override
    @Transactional
    public Either<FormatRespDto, Boolean> updateHost(String hostId, MepHost host, String token) {
        // health check
        String healRes = HttpClientUtil.getHealth(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort());
        if (healRes == null) {
            String msg = "health check faild,current ip or port cann't be used!";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // add mechost to lcm
        boolean addMecHostRes = MepHostUtil.addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = MepHostUtil
                .uploadFileToLcm(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort(), uploadedFile.getFilePath(),
                    host.getMecHostIp(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
                return Either.left(dto);
            }
        }
        MepHost currentHost = mepHostMapper.getHost(hostId);
        if (currentHost == null) {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }

        host.setId(hostId); // no need to set hostId by user
        host.setUserId(currentHost.getUserId());
        int ret = mepHostMapper.updateHostSelected(host);
        if (ret > 0) {
            LOGGER.info("Update host {} success", hostId);
            return Either.right(true);
        }
        LOGGER.error("Update host {} failed", hostId);
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not update the host"));
    }

    /**
     * getHost.
     *
     * @return
     */
    @Override
    public Either<FormatRespDto, MepHost> getHost(String hostId) {
        MepHost host = mepHostMapper.getHost(hostId);
        if (host != null) {
            LOGGER.info("Get host {} success", hostId);
            return Either.right(host);
        } else {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }
    }

    /**
     * getHostLogByHostId.
     *
     * @param hostId hostId
     * @return
     */
    @Override
    public Either<FormatRespDto, List<MepHostLog>> getHostLogByHostId(String hostId) {
        List<MepHostLog> hostLogList = hostLogMapper.getHostLogByHostId(hostId);
        LOGGER.info("Get host logs success.");
        return Either.right(hostLogList);
    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        LOGGER.info("user auth:{}", currUserAuth);
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

}
