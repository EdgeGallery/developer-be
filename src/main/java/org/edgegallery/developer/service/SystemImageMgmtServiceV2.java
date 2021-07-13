
/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.SystemImageMapper;
import org.edgegallery.developer.model.system.MepGetSystemImageReq;
import org.edgegallery.developer.model.system.MepGetSystemImageRes;
import org.edgegallery.developer.model.system.MepSystemQueryCtrl;
import org.edgegallery.developer.model.system.VmSystem;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service("systemImageMgmtServiceV2")
public class SystemImageMgmtServiceV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemImageMgmtService.class);

    @Autowired
    private SystemImageMapper systemImageMapper;

    /**
     * getSystemImage.
     *
     * @param mepGetSystemImageReq mepGetSystemImageReq
     * @return
     */
    public Either<FormatRespDto, MepGetSystemImageRes> getSystemImages(MepGetSystemImageReq mepGetSystemImageReq) {
        try {
            LOGGER.info("Query SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (!isAdminUser()) {
                mepGetSystemImageReq.setUserId(userId);
            }
            MepSystemQueryCtrl queryCtrl = mepGetSystemImageReq.getQueryCtrl();
            if (queryCtrl.getSortBy() == null || queryCtrl.getSortBy().equalsIgnoreCase("createTime")) {
                queryCtrl.setSortBy("create_time");
            } else if (queryCtrl.getSortBy().equalsIgnoreCase("userName")) {
                queryCtrl.setSortBy("user_name");
            }
            if (queryCtrl.getSortOrder() == null) {
                queryCtrl.setSortBy("DESC");
            }
            String createTimeBegin = mepGetSystemImageReq.getCreateTimeBegin();
            String createTimeEnd = mepGetSystemImageReq.getCreateTimeEnd();
            if (!StringUtils.isBlank(createTimeBegin)) {
                mepGetSystemImageReq.setCreateTimeBegin(createTimeBegin + " 00:00:00");
            }
            if (!StringUtils.isBlank(createTimeEnd)) {
                mepGetSystemImageReq.setCreateTimeEnd(createTimeEnd + " 23:59:59");
            }
            mepGetSystemImageReq.setQueryCtrl(queryCtrl);
            MepGetSystemImageRes mepGetSystemImageRes = new MepGetSystemImageRes();
            mepGetSystemImageRes.setTotalCount(systemImageMapper.getSystemImagesCount(mepGetSystemImageReq));
            mepGetSystemImageRes.setImageList(systemImageMapper.getSystemImagesByCondition(mepGetSystemImageReq));
            return Either.right(mepGetSystemImageRes);
        } catch (Exception e) {
            LOGGER.error("Query SystemImages failed");
            throw new DeveloperException("Get system image failed", ResponseConsts.RET_GET_SYSTEM_IMAGE_FAILED);
        }
    }


    /**
     * createSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> createSystemImage(VmSystem vmImage) {
        try {
            LOGGER.info("Create SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (StringUtils.isBlank(vmImage.getSystemName())) {
                LOGGER.error("SystemName is blank.");
                throw new DeveloperException("SystemName is blank", ResponseConsts.RET_SYSTEM_NAME_BLANK);
            }
            vmImage.setUserId(userId);
            if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), null, userId) > 0) {
                LOGGER.error("SystemName can not duplicate.");
                throw new DeveloperException("SystemName can not duplicate", ResponseConsts.RET_SYSTEM_NAME_DUPLICATE);
            }
            vmImage.setUserId(AccessUserUtil.getUser().getUserId());
            vmImage.setUserName(AccessUserUtil.getUser().getUserName());
            vmImage.setStatus(EnumSystemImageStatus.UPLOAD_WAIT);
            int ret = systemImageMapper.createSystemImage(vmImage);
            if (ret > 0) {
                LOGGER.info("Crete SystemImage {} success ", vmImage.getUserId());
                return Either.right(true);
            }
            LOGGER.error("Create SystemImage failed.");
            throw new DeveloperException("Create system image failed", ResponseConsts.RET_CREATE_SYSTEM_IMAGE_FAILED);
        } catch (Exception e) {
            LOGGER.error("Create SystemImages exception.");
            throw new DeveloperException("Create system image exception", ResponseConsts.RET_CREATE_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    /**
     * updateSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> updateSystemImage(VmSystem vmImage, Integer systemId) {
        try {
            LOGGER.info("Update SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            if (!isAdminUser()) {
                vmImage.setUserId(userId);
            }
            VmSystem vmSystemImage = systemImageMapper.getVmImage(systemId);
            if (StringUtils.isAnyBlank(vmImage.getSystemName(), vmSystemImage.getUserId())) {
                LOGGER.error("SystemName is blank or systemImage is not exist.");
                throw new DeveloperException("SystemName is blank or systemImage is not exists", ResponseConsts.RET_SYSTEM_NAME_BLANK_OR_IMAGE_NOT_EXISTS);
            }
            if (systemImageMapper.getSystemNameCount(vmImage.getSystemName(), systemId,
                    vmSystemImage.getUserId()) > 0) {
                LOGGER.error("SystemName can not duplicate.");
                throw new DeveloperException("SystemName can not duplicate", ResponseConsts.RET_SYSTEM_NAME_DUPLICATE);
            }
            vmImage.setSystemId(systemId);

            int ret = systemImageMapper.updateSystemImage(vmImage);
            if (ret > 0) {
                LOGGER.info("Update SystemImage success systemId = {}, userId = {}", systemId, userId);
                return Either.right(true);
            }
            LOGGER.error("Update system image failed.");
            throw new DeveloperException("Update system image failed", ResponseConsts.RET_UPDATE_SYSTEM_IMAGE_FAILED);
        } catch (Exception e) {
            LOGGER.error("Update system image exception.");
            throw new DeveloperException("Update system image exception", ResponseConsts.RET_UPDATE_SYSTEM_IMAGE_EXCEPTION);
        }
    }


    /**
     * publishSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> publishSystemImage(Integer systemId) {
        try {
            LOGGER.info("Publish SystemImage start");
            String userId = AccessUserUtil.getUser().getUserId();
            int ret = systemImageMapper.updateSystemImageStatus(systemId, EnumSystemImageStatus.PUBLISHED.toString());
            if (ret > 0) {
                LOGGER.info("Publish SystemImage {} success ", userId);
                return Either.right(true);
            }
            LOGGER.error("Publish system image failed.");
            throw new DeveloperException("Publish system image exception", ResponseConsts.RET_PUBLISH_SYSTEM_IMAGE_FAILED);
        } catch (Exception e) {
            LOGGER.error("Publish system image exception.");
            throw new DeveloperException("Publish system image exception", ResponseConsts.RET_PUBLISH_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    /**
     * deleteSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteSystemImage(Integer systemId) {
        try {
            LOGGER.info("Delete SystemImage start");
            VmSystem vmImage = new VmSystem();
            String userId = AccessUserUtil.getUser().getUserId();
            if (!isAdminUser()) {
                vmImage.setUserId(userId);
            }
            vmImage.setSystemId(systemId);

            LOGGER.info("delete system image on remote server.");
            if (!deleteImageFileOnRemote(systemId)) {
                LOGGER.error("delete system image on remote server failed.");
                throw new DeveloperException("Delete system image exception", ResponseConsts.RET_DELETE_SYSTEM_IMAGE_ON_REMOTE_SERVER_FAILED);
            }

            LOGGER.info("delete system image record in database.");
            int res = systemImageMapper.deleteSystemImage(vmImage);
            if (res < 1) {
                LOGGER.error("Delete SystemImage {} failed", userId);
                throw new DeveloperException("Delete system image failed", ResponseConsts.RET_DELETE_SYSTEM_IMAGE_FAILED);
            }
            LOGGER.info("Delete SystemImage {} success", userId);
            return Either.right(true);
        } catch (Exception e) {
            LOGGER.error("Delete system image exception.");
            throw new DeveloperException("Delete system image exception", ResponseConsts.RET_DELETE_SYSTEM_IMAGE_EXCEPTION);
        }
    }

    private boolean deleteImageFileOnRemote(Integer systemId) {
        String systemPath = systemImageMapper.getSystemImagesPath(systemId);
        if (StringUtils.isEmpty(systemPath)) {
            LOGGER.debug("system path is invalid, no need to delete.");
            return true;
        }

        try {
            String url = systemPath.substring(0, systemPath.length() - 16);
            if (!HttpClientUtil.deleteSystemImage(url)) {
                LOGGER.error("delete SystemImage on remote failed!");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("delete old SystemImage failed, {}", e.getMessage());
            return false;
        }

        return true;
    }

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }
}
