
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
import org.edgegallery.developer.response.FormatRespDto;
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

    private boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

}
