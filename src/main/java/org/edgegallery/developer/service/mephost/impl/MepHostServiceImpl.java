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
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.mephost.MepHostLogMapper;
import org.edgegallery.developer.mapper.mephost.MepHostMapper;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.mephost.MepHostLog;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.service.uploadfile.impl.UploadServiceImpl;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.MepHostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private UploadServiceImpl uploadService;

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
            LOGGER.error("mecHost have exit:{}", host.getMecHostIp());
            throw new DeveloperException("mecHost have exit!", ResponseConsts.EXIST_SAME_MEC_HOST);
        }
        if (StringUtils.isBlank(host.getUserId()) || !isAdminUser()) {
            LOGGER.error("Create host failed, userId is empty or not admin");
            throw new DeveloperException("userId is empty or not admin!", ResponseConsts.USER_ID_EMPTY_OR_NOT_ADMIN);
        }
        if ("OpenStack".equals(host.getVimType())) {
            Map<String, String> getParams = InputParameterUtil.getParams(host.getNetworkParameter());
            if (!getParams.containsKey("app_mp1_ip") || !getParams.containsKey("app_n6_ip") || !getParams
                .containsKey("app_internet_ip")) {
                LOGGER.error("Network params config error");
                throw new DeveloperException("Network params config error!", ResponseConsts.NET_WORK_CONFIG_ERROR);
            }
        }
        // health check
        String healRes = HttpClientUtil.getHealth(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort());
        if (healRes == null) {
            LOGGER.error("health check faild,current ip or port cann't be used!");
            throw new DeveloperException("current lcmip or lcmport cann't be used!",
                ResponseConsts.LCM_IP_OR_PORT_CAN_NOT_BE_USED);
        }
        // add mechost to lcm
        boolean addMecHostRes = MepHostUtil.addMecHostToLcm(host);
        if (!addMecHostRes) {
            LOGGER.error("add mec host to lcm fail");
            throw new DeveloperException("add mec host to lcm fail!", ResponseConsts.ADD_MEC_HOST_TO_LCM_FAILED);
        }
        // upload config file
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = MepHostUtil
                .uploadFileToLcm(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort(), uploadedFile.getFilePath(),
                    host.getMecHostIp(), token);
            if (!uploadRes) {
                LOGGER.error("create host failed,upload config file error");
                throw new DeveloperException("upload config file error!", ResponseConsts.CREATE_HOST_CONFIG_FILE_ERROR);
            }
        }
        host.setId(UUID.randomUUID().toString()); // no need to set hostId by user
        host.setMecHostPort(VNC_PORT);
        int ret = mepHostMapper.createHost(host);
        if (ret > 0) {
            LOGGER.info("Crete host {} success ", host.getId());
            return Either.right(true);
        }
        LOGGER.error("Create host failed!");
        throw new DeveloperException("Create host failed!", ResponseConsts.INSERT_DATA_FAILED);
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
            throw new DeveloperException("delete host failed!", ResponseConsts.DELETE_DATA_FAILED);
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
            throw new DeveloperException("current lcmip or lcmport cann't be used!",
                ResponseConsts.LCM_IP_OR_PORT_CAN_NOT_BE_USED);
        }
        // add mechost to lcm
        boolean addMecHostRes = MepHostUtil.addMecHostToLcm(host);
        if (!addMecHostRes) {
            LOGGER.error("add mec host to lcm fail");
            throw new DeveloperException("add mec host to lcm fail!", ResponseConsts.ADD_MEC_HOST_TO_LCM_FAILED);
        }
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = MepHostUtil
                .uploadFileToLcm(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort(), uploadedFile.getFilePath(),
                    host.getMecHostIp(), token);
            if (!uploadRes) {
                LOGGER.error("Create host failed,upload config file error");
                throw new DeveloperException("upload config file error!", ResponseConsts.CREATE_HOST_CONFIG_FILE_ERROR);
            }
        }
        MepHost currentHost = mepHostMapper.getHost(hostId);
        if (currentHost == null) {
            LOGGER.error("Can not find host by {}", hostId);
            throw new DeveloperException("Can not find the host!", ResponseConsts.MEP_HOST_NOT_EXIST);
        }

        host.setId(hostId); // no need to set hostId by user
        host.setUserId(currentHost.getUserId());
        int ret = mepHostMapper.updateHostSelected(host);
        if (ret > 0) {
            LOGGER.info("Update host {} success", hostId);
            return Either.right(true);
        }
        LOGGER.error("Update host {} failed", hostId);
        throw new DeveloperException("update host failed!", ResponseConsts.MODIFY_DATA_FAILED);
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
            throw new DeveloperException("can not find the host!", ResponseConsts.MEP_HOST_NOT_EXIST);
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

    /**
     * upload host config file.
     *
     * @param uploadFile config file
     * @return
     */
    @Override
    public Either<FormatRespDto, UploadedFile> uploadConfigFile(MultipartFile uploadFile) {
        LOGGER.info("Start uploading file");
        String userId = AccessUserUtil.getUser().getUserId();
        UploadedFile result = uploadService.saveFileToLocal(uploadFile, userId);
        if (result == null) {
            LOGGER.error("save config file failed!");
            throw new DeveloperException("Failed to save file!", ResponseConsts.INSERT_DATA_FAILED);
        }
        int ret = uploadedFileMapper.updateFileStatus(result.getFileId(), false);
        if (ret < 1) {
            LOGGER.error("update config file status failed!!");
            throw new DeveloperException("update config file status failed!", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return Either.right(result);
    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        LOGGER.info("user auth:{}", currUserAuth);
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

}
