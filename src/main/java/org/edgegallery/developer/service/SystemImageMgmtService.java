package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.SystemImageMapper;
import org.edgegallery.developer.model.vm.VmSystem;
import org.edgegallery.developer.model.workspace.MepGetSystemImageReq;
import org.edgegallery.developer.model.workspace.MepGetSystemImageRes;
import org.edgegallery.developer.model.workspace.MepSystemQueryCtrl;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Date;

@Service("systemImageMgmtService")
public class SystemImageMgmtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemService.class);

    @Autowired
    private SystemImageMapper systemImageMapper;

    /**
     * getSystemImage.
     *
     * @param mepGetSystemImageReq
     * @return
     */
    public Either<FormatRespDto, MepGetSystemImageRes> getSystemImages(MepGetSystemImageReq mepGetSystemImageReq) {
        try {
            String userName = AccessUserUtil.getUser().getUserName();
            String userId = AccessUserUtil.getUser().getUserId();
            if (!StringUtils.equalsIgnoreCase(userName, "admin")) {
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
            mepGetSystemImageReq.setQueryCtrl(queryCtrl);
            MepGetSystemImageRes mepGetSystemImageRes = new MepGetSystemImageRes();
            mepGetSystemImageRes.setTotalCount(systemImageMapper.getSystemImagesCount(mepGetSystemImageReq));
            mepGetSystemImageRes.setImageList(systemImageMapper.getSystemImagesByCondition(mepGetSystemImageReq));
            return Either.right(mepGetSystemImageRes);
        } catch (Exception e) {
            LOGGER.error("Query SystemImages failed");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not query SystemImages."));
        }
    }

    /**
     * createSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> createSystemImage(VmSystem vmImage) {
        vmImage.setCreateTime(new Date(System.currentTimeMillis()));
        vmImage.setUserId(AccessUserUtil.getUser().getUserId());
        vmImage.setUserName(AccessUserUtil.getUser().getUserName());
        vmImage.setStatus("UPLOAD_WAIT");
        LOGGER.info("create SystemImage currentTime:{}", vmImage.getCreateTime());
        int ret = systemImageMapper.createSystemImage(vmImage);
        if (ret > 0) {
            LOGGER.info("Crete SystemImage {} success ", vmImage.getUserId());
            return Either.right(true);
        }
        LOGGER.error("Create SystemImage failed ");
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not create a SystemImage."));
    }

    /**
     * updateSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> updateSystemImage(VmSystem vmImage, Integer systemId) {
        String userName = AccessUserUtil.getUser().getUserName();
        String userId = AccessUserUtil.getUser().getUserId();
        if (!StringUtils.equalsIgnoreCase(userName, "admin")) {
            vmImage.setUserId(userId);
        }
        vmImage.setModifyTime(new Date(System.currentTimeMillis()));
        vmImage.setSystemId(systemId);
        vmImage.setUserName(userName);

        int ret = systemImageMapper.updateSystemImage(vmImage);
        if (ret > 0) {
            LOGGER.info("Update SystemImage {} success ", userId);
            return Either.right(true);
        }
        LOGGER.error("Update SystemImage failed ");
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not update a SystemImage."));
    }

    /**
     * publishSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> publishSystemImage(Integer systemId) {
        String userId = AccessUserUtil.getUser().getUserId();
        VmSystem vmImage = new VmSystem();
        vmImage.setSystemId(systemId);
        vmImage.setStatus("PUBLISHED");
        vmImage.setUploadTime(new Date(System.currentTimeMillis()));
        int ret = systemImageMapper.publishSystemImage(vmImage);
        if (ret > 0) {
            LOGGER.info("Publish SystemImage {} success ", userId);
            return Either.right(true);
        }
        LOGGER.error("Publish SystemImage failed ");
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not publish a SystemImage."));
    }

    /**
     * deleteSystemImage.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteSystemImage(Integer systemId) {
        VmSystem vmImage = new VmSystem();
        String userName = AccessUserUtil.getUser().getUserName();
        String userId = AccessUserUtil.getUser().getUserId();
        if (!StringUtils.equalsIgnoreCase(userName, "admin")) {
            vmImage.setUserId(userId);
        }
        vmImage.setSystemId(systemId);
        int res = systemImageMapper.deleteSystemImage(vmImage);
        if (res < 1) {
            LOGGER.error("Delete SystemImage {} failed", userId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "delete SystemImage failed.");
            return Either.left(error);
        }
        LOGGER.info("Delete SystemImage {} success", userId);
        return Either.right(true);
    }
}
