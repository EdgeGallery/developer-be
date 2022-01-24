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

package org.edgegallery.developer.service.recource.mephost.impl;

import static org.edgegallery.developer.util.HttpClientUtil.getUrlPrefix;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.exception.UnauthorizedException;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.mapper.resource.mephost.MepHostLogMapper;
import org.edgegallery.developer.mapper.resource.mephost.MepHostMapper;
import org.edgegallery.developer.mapper.uploadfile.UploadFileMapper;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.resource.mephost.EnumVimType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.resource.mephost.MepHostLog;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.proxy.ReverseProxyService;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.service.uploadfile.impl.UploadFileServiceImpl;
import org.edgegallery.developer.util.AesUtil;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.MepHostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("mepHostService")
public class MepHostServiceImpl implements MepHostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MepHostServiceImpl.class);

    @Autowired
    private MepHostMapper mepHostMapper;

    @Autowired
    private MepHostLogMapper hostLogMapper;

    @Autowired
    private UploadFileMapper uploadFileMapper;

    @Autowired
    private UploadFileServiceImpl uploadService;

    @Autowired
    private ReverseProxyService reverseProxyService;

    @Value("${client.client-id:}")
    private String clientId;

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
        return new Page<>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * createHost.
     *
     * @return
     */
    @Transactional
    @Override
    public boolean createHost(MepHost host, User user, String token) {
        MepHost mepHost = mepHostMapper.getHostsByMecHostIp(host.getMecHostIp());
        if (mepHost != null) {
            LOGGER.error("mecHost already exists:{}", host.getMecHostIp());
            throw new IllegalRequestException("mecHost already exists!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        host.setId(UUID.randomUUID().toString()); // no need to set hostId by user
        host.setUserId(user.getUserId());
        // AES encryption
        String userNameEncode = AesUtil.encode(clientId, host.getMecHostUserName());
        String passwordEncode = AesUtil.encode(clientId, host.getMecHostPassword());
        host.setMecHostUserName(userNameEncode);
        host.setMecHostPassword(passwordEncode);
        // check host parameter
        checkMepHost(host);
        // config mepHost to lcm
        configMepHostToLCM(host, user.getToken());
        int ret = mepHostMapper.createHost(host);
        if (ret < 1) {
            LOGGER.error("Create host failed!");
            throw new DataBaseException("Create host failed!", ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        if (host.getVimType() != EnumVimType.K8S) {
            LOGGER.info("Crete host {} success ", host.getId());
            reverseProxyService.addReverseProxy(host.getId(), host.getMecHostPort(), token);
        }
        return true;
    }

    /**
     * deleteHost.
     *
     * @return
     */
    @Transactional
    @Override
    public boolean deleteHost(String hostId, String token) {
        MepHost host = mepHostMapper.getHost(hostId);
        if (host == null) {
            LOGGER.error("hostId {} is error!", hostId);
            throw new DataBaseException("query host is empty!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        if (StringUtils.isNotEmpty(host.getConfigId())) {
            boolean res = uploadService.deleteFile(host.getConfigId());
            if (!res) {
                LOGGER.warn("config id {} is error!", host.getConfigId());
                throw new FileOperateException("delete config file failed!", ResponseConsts.RET_DELETE_FILE_FAIL);
            }
        }
        reverseProxyService.deleteReverseProxy(hostId, Consts.DEFAULT_OPENSTACK_VNC_PORT, token);

        int res = mepHostMapper.deleteHost(hostId);
        if (res < 1) {
            LOGGER.error("Delete host {} failed", hostId);
            throw new DataBaseException("delete host failed!", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        LOGGER.info("Delete host {} success", hostId);
        return true;
    }

    /**
     * updateHost.
     *
     * @return
     */
    @Override
    @Transactional
    public boolean updateHost(String hostId, MepHost host, User user) {
        MepHost currentHost = mepHostMapper.getHost(hostId);
        if (currentHost == null) {
            LOGGER.error("Can not find host by {}", hostId);
            throw new EntityNotFoundException("Can not find the host!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        host.setId(hostId); // no need to set hostId by user
        host.setUserId(user.getUserId());
        // check host parameter
        checkMepHost(host);
        // config mepHost to lcm
        configMepHostToLCM(host, user.getToken());
        int ret = mepHostMapper.updateHostSelected(host);
        if (ret > 0) {
            LOGGER.info("Update host {} success", hostId);
            return true;
        }
        LOGGER.error("Update host {} failed", hostId);
        throw new DataBaseException("update host failed!", ResponseConsts.RET_UPDATE_DATA_FAIL);
    }

    /**
     * getHost.
     *
     * @return
     */
    @Override
    public MepHost getHost(String hostId) {
        MepHost host = mepHostMapper.getHost(hostId);
        if (host != null) {
            LOGGER.info("Get host {} success", hostId);
            return host;
        } else {
            LOGGER.error("Can not find host by {}", hostId);
            throw new EntityNotFoundException("can not find the host!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
    }

    /**
     * getHostLogByHostId.
     *
     * @param hostId hostId
     * @return
     */
    @Override
    public List<MepHostLog> getHostLogByHostId(String hostId) {
        List<MepHostLog> hostLogList = hostLogMapper.getHostLogByHostId(hostId);
        LOGGER.info("Get host logs success.");
        return hostLogList;
    }

    /**
     * upload host config file.
     *
     * @param uploadFile config file
     * @return
     */
    @Override
    public UploadFile uploadConfigFile(String userId, MultipartFile uploadFile) {
        LOGGER.info("Start uploading file");
        //check format
        String fileName = uploadFile.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalRequestException("fileName is null", ResponseConsts.RET_FILE_FORMAT_ERROR);
        }
        if (StringUtils.isEmpty(fileName) || fileName.contains(".")) {
            String errMsg = "upload file should not have suffix";
            LOGGER.error(errMsg);
            throw new IllegalRequestException(errMsg, ResponseConsts.RET_FILE_FORMAT_ERROR);
        }
        UploadFile result = uploadService.saveFileToLocal(uploadFile, userId);
        if (result == null) {
            LOGGER.error("save config file failed!");
            throw new DataBaseException("Failed to save file!", ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return result;
    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        LOGGER.info("user auth:{}", currUserAuth);
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

    private void configMepHostToLCM(MepHost host, String token) {
        String basePath = getUrlPrefix(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort());
        String healRes = HttpClientUtil.getHealth(basePath);
        if (healRes == null) {
            String msg = "health check failed,current ip or port can not be used!";
            LOGGER.error(msg);
            throw new IllegalRequestException(msg, ResponseConsts.RET_CREATE_SANDBOX_HEALTH_CHECK_FAIL);
        }
        // add mechost to lcm
        boolean addMecHostRes = MepHostUtil.addMecHostToLcm(host, token);
        if (!addMecHostRes) {
            LOGGER.error("add mec host to lcm fail");
            throw new DeveloperException("add mec host to lcm fail!", ResponseConsts.RET_CALL_LCM_FAIL);
        }
        // upload config file
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadFile uploadedFile = uploadFileMapper.getFileById(host.getConfigId());
            if (uploadedFile == null) {
                LOGGER.error("config file id is error");
                throw new DeveloperException("config file does not exist!", ResponseConsts.RET_QUERY_DATA_EMPTY);
            }
            boolean uploadRes = MepHostUtil.uploadFileToLcm(host, uploadedFile.getFilePath(), token);
            if (!uploadRes) {
                LOGGER.error("create host failed,upload config file error");
                throw new DeveloperException("upload config file to lcm error!", ResponseConsts.RET_CALL_LCM_FAIL);
            }
        }
    }

    private void checkMepHost(MepHost host) {
        if (!isAdminUser()) {
            LOGGER.error("Create host failed, userId is empty or not admin");
            throw new UnauthorizedException("userId is empty or not admin!", ResponseConsts.RET_REQUEST_UNAUTHORIZED);
        }
        if (!EnumVimType.K8S.equals(host.getVimType())) {
            Map<String, String> getParams = InputParameterUtil.getParams(host.getNetworkParameter());
            if (!getParams.containsKey("VDU1_APP_Plane03_IP") || !getParams.containsKey("VDU1_APP_Plane02_IP")
                || !getParams.containsKey("VDU1_APP_Plane01_IP")) {
                LOGGER.error("Network params config error");
                throw new IllegalRequestException("Network params config error!",
                    ResponseConsts.RET_CREATE_SANDBOX_CONFIG_NETWORK_FAIL);
            }
        }
    }
}
