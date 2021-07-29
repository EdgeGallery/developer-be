package org.edgegallery.developer.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spencerwi.either.Either;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.ContainerImageMapper;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.model.containerimage.ContainerImageReq;
import org.edgegallery.developer.model.containerimage.EnumContainerImageStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.SystemImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("containerImageMgmtServiceV2")
public class ContainerImageMgmtServiceV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerImageMgmtServiceV2.class);

    @Autowired
    private ContainerImageMapper containerImageMapper;

    /**
     * createSystemImage.
     */
    public Either<FormatRespDto, ContainerImage> createContainerImage(ContainerImage containerImage) {
        String imageName = containerImage.getImageName();
        String imageVersion = containerImage.getImageVersion();
        String userId = containerImage.getUserId();
        String userName = containerImage.getUserName();
        if (StringUtils.isEmpty(imageName) || StringUtils.isEmpty(imageVersion) || StringUtils.isEmpty(userId)
            || StringUtils.isEmpty(userName)) {
            String errorMsg
                = "The required parameter is empty. pls check imageName or imageVersion or userId or userName";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_CREATE_CONTAINER_IMAGE_CHECK_PARAM_FAILED);
        }
        //keep imageName imageVersion unique
        List<ContainerImage> imageList = containerImageMapper.getAllImage();
        if (!CollectionUtils.isEmpty(imageList)) {
            for (ContainerImage image : imageList) {
                if (imageName.equals(image.getImageName()) && imageVersion.equals(image.getImageVersion())) {
                    String errorMsg = "exist the same imageName and imageVersion";
                    LOGGER.error(errorMsg);
                    throw new DeveloperException(errorMsg, ResponseConsts.RET_EXIST_SAME_NAME_AND_VERSION);
                }
            }
        }
        String imageId = UUID.randomUUID().toString();
        containerImage.setImageId(imageId);
        containerImage.setCreateTime(new Date());
        containerImage.setImageStatus(EnumContainerImageStatus.UPLOAD_WAIT);
        int retCode = containerImageMapper.createContainerImage(containerImage);
        if (retCode < 1) {
            String errorMsg = "Create ContainerImage failed.";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_CREATE_CONTAINER_IMAGE_FAILED);
        }
        LOGGER.info("create ContainerImage success");
        ContainerImage queryImage = containerImageMapper.getContainerImage(imageId);
        return Either.right(queryImage);
    }

    /**
     * getAllContainerImages.
     */
    public Page<ContainerImage> getAllImage(ContainerImageReq containerImageReq) {
        PageHelper.offsetPage(containerImageReq.getOffset(), containerImageReq.getLimit());
        String createTimeBegin = containerImageReq.getCreateTimeBegin();
        String createTimeEnd = containerImageReq.getCreateTimeEnd();
        if (!StringUtils.isBlank(createTimeBegin)) {
            containerImageReq.setCreateTimeBegin(createTimeBegin + " 00:00:00");
        }
        if (!StringUtils.isBlank(createTimeEnd)) {
            containerImageReq.setCreateTimeEnd(createTimeEnd + " 23:59:59");
        }
        PageInfo pageInfo = null;
        if (SystemImageUtil.isAdminUser()) {
            pageInfo = new PageInfo<ContainerImage>(containerImageMapper.getAllImageByAdminAuth(containerImageReq));
        } else {
            pageInfo = new PageInfo<ContainerImage>(containerImageMapper.getAllImageByOrdinaryAuth(containerImageReq));
        }
        if (pageInfo != null) {
            LOGGER.info("Get all container image success.");
            return new Page<ContainerImage>(pageInfo.getList(), containerImageReq.getLimit(),
                containerImageReq.getOffset(), pageInfo.getTotal());
        }
        return null;
    }

    /**
     * modifySystemImage.
     */
    public Either<FormatRespDto, ContainerImage> updateContainerImage(String imageId, ContainerImage containerImage) {
        String loginUserId = AccessUserUtil.getUser().getUserId();
        ContainerImage oldImage = containerImageMapper.getContainerImage(imageId);
        if (!SystemImageUtil.isAdminUser() && !loginUserId.equals(oldImage.getUserId())) {
            String errorMsg = "Cannot modify data created by others";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_IMAGE_AUTH_CHECK_FAILED);
        }
        String imageName = containerImage.getImageName();
        String imageVersion = containerImage.getImageVersion();
        String userId = containerImage.getUserId();
        String userName = containerImage.getUserName();
        if (StringUtils.isEmpty(imageName) || StringUtils.isEmpty(imageVersion) || StringUtils.isEmpty(userId)
            || StringUtils.isEmpty(userName)) {
            String errorMsg
                = "The required parameter is empty. pls check imageName or imageVersion or userId or userName";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_CREATE_CONTAINER_IMAGE_CHECK_PARAM_FAILED);
        }
        //keep imageName imageVersion unique
        List<ContainerImage> imageList = containerImageMapper.getAllImage();
        if (!CollectionUtils.isEmpty(imageList)) {
            for (ContainerImage image : imageList) {
                if (imageName.equals(image.getImageName()) && imageVersion.equals(image.getImageVersion())) {
                    String errorMsg = "exist the same imageName and imageVersion";
                    LOGGER.error(errorMsg);
                    throw new DeveloperException(errorMsg, ResponseConsts.RET_EXIST_SAME_NAME_AND_VERSION);
                }
            }
        }
        containerImage.setImageId(imageId);
        containerImage.setCreateTime(new Date());
        int retCode;
        if (SystemImageUtil.isAdminUser()) {
            retCode = containerImageMapper.updateContainerImageByAdmin(containerImage);
        } else {
            containerImage.setUserId(loginUserId);
            containerImage.setUserName(AccessUserUtil.getUser().getUserName());
            retCode = containerImageMapper.updateContainerImageByOrdinary(containerImage);
        }
        if (retCode < 1) {
            String errorMsg = "update ContainerImage failed.";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_CONTAINER_IMAGE_FAILED);
        }
        LOGGER.info("update ContainerImage success");
        ContainerImage queryImage = containerImageMapper.getContainerImage(imageId);
        return Either.right(queryImage);
    }

    /**
     * deleteSystemImage.
     */
    public Either<FormatRespDto, Boolean> deleteContainerImage(String imageId) {
        String loginUserId = AccessUserUtil.getUser().getUserId();
        ContainerImage oldImage = containerImageMapper.getContainerImage(imageId);
        if (!SystemImageUtil.isAdminUser() && !loginUserId.equals(oldImage.getUserId())) {
            String errorMsg = "Cannot modify data created by others";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_IMAGE_AUTH_CHECK_FAILED);
        }
        int retCode;
        if (SystemImageUtil.isAdminUser()) {
            retCode = containerImageMapper.deleteContainerImageByAdmin(imageId);
        } else {
            String loginUserName = AccessUserUtil.getUser().getUserName();
            retCode = containerImageMapper.deleteContainerImageByOrdinary(imageId, loginUserId, loginUserName);
        }
        if (retCode < 1) {
            String errorMsg = "delete ContainerImage failed.";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_DEL_CONTAINER_IMAGE_FAILED);
        }
        LOGGER.info("delete ContainerImage success");
        return Either.right(true);
    }

}
