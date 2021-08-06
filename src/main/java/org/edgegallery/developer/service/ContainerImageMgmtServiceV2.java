package org.edgegallery.developer.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.SaveImageCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spencerwi.either.Either;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("containerImageMgmtServiceV2")
public class ContainerImageMgmtServiceV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerImageMgmtServiceV2.class);

    @Autowired
    private ContainerImageMapper containerImageMapper;

    private static CookieStore cookieStore = new BasicCookieStore();

    @Value("${imagelocation.username:}")
    private String harborUsername;

    @Value("${imagelocation.password:}")
    private String harborPassword;

    @Value("${imagelocation.domainname:}")
    private String imageDomainName;

    @Value("${imagelocation.project:}")
    private String imageProject;

    @Value("${imagelocation.port:}")
    private String port;

    @Value("${imagelocation.protocol:}")
    private String protocol;

    @Value("${security.oauth2.resource.jwt.key-uri:}")
    private String loginUrl;

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
        // //keep imageName imageVersion unique
        if (!oldImage.getImageName().equals(imageName) || !oldImage.getImageVersion().equals(imageVersion)) {
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
        if (!SystemImageUtil.isAdminUser() && oldImage != null && !loginUserId.equals(oldImage.getUserId())) {
            String errorMsg = "Cannot modify data created by others";
            LOGGER.error(errorMsg);
            throw new DeveloperException(errorMsg, ResponseConsts.RET_UPDATE_IMAGE_AUTH_CHECK_FAILED);
        }
        //delete remote harbor image
        if (StringUtils.isNotEmpty(oldImage.getImagePath())) {
            boolean isDeleted = deleteHarborImage(oldImage.getImagePath());
            if (!isDeleted) {
                String errorMsg = "delete image from harbor failed!";
                LOGGER.error(errorMsg);
                throw new DeveloperException(errorMsg, ResponseConsts.RET_DEL_CONTAINER_IMAGE_FAILED);
            }
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

    /**
     * downloadHarborImage.
     *
     * @param imageId imageId
     * @return
     */
    public ResponseEntity<InputStreamResource> downloadHarborImage(String imageId) {
        if (StringUtils.isEmpty(imageId)) {
            LOGGER.error("imageId is null");
            throw new DeveloperException("imageId is null", ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
        ContainerImage containerImage = containerImageMapper.getContainerImage(imageId);
        if (containerImage == null) {
            LOGGER.error("imageId is incorrect");
            throw new DeveloperException("imageId is incorrect", ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
        String image = containerImage.getImagePath();
        String fileName = containerImage.getFileName();
        if (StringUtils.isEmpty(image) || StringUtils.isEmpty(fileName)) {
            String msg = "image or fileName is empty";
            LOGGER.error(msg);
            throw new DeveloperException(msg, ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(protocol + "://" + imageDomainName + ":" + port).build();
            DockerCmdExecFactory factory = new NettyDockerCmdExecFactory().withConnectTimeout(100000);
            DockerClient dockerClient = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(factory)
                .build();
            //pull image
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion().close();
            String[] images = image.trim().split(":");
            //save image
            SaveImageCmd saveImage = dockerClient.saveImageCmd(images[0]).withTag(images[1]);
            InputStream input = saveImage.exec();
            if (input == null) {
                String msg = "save image  failed!";
                LOGGER.error(msg);
                throw new DeveloperException(msg, ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
            }
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .body(new InputStreamResource(input));
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            String msg = "download Harbor image occur exception!";
            LOGGER.error("download Harbor image failed! {}", e.getMessage());
            throw new DeveloperException(msg, ResponseConsts.RET_DOWNLOAD_CONTAINER_IMAGE_FAILED);
        }
    }

    private boolean deleteHarborImage(String image) {
        //Split image
        if (!image.contains(imageDomainName)) {
            LOGGER.warn("only delete image in harbor repo");
            return true;
        }
        String[] images = image.trim().split("/");
        String imageName = "";
        String imageVersion = "";
        if (images.length == 3) {
            String[] names = images[2].split(":");
            imageName = names[0];
            imageVersion = names[1];
            try (CloseableHttpClient client = createIgnoreSslHttpClient()) {
                URL url = new URL(loginUrl);
                String userLoginUrl = url.getProtocol() + "://" + imageDomainName + "/c/login";
                LOGGER.warn("harbor login url: {}", userLoginUrl);
                //excute login to harbor repo
                HttpPost httpPost = new HttpPost(userLoginUrl);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody("principal", harborUsername);
                builder.addTextBody("password", harborPassword);
                httpPost.setEntity(builder.build());
                client.execute(httpPost);

                // get _csrf from cookie
                String csrf = getCsrf();
                LOGGER.warn("__csrf: {}", csrf);

                //excute delete image operation
                String deleteImageUrl = "https://" + imageDomainName + "/api/v2.0/projects/" + imageProject
                    + "/repositories/" + imageName + "/artifacts/" + imageVersion;
                LOGGER.warn("delete image url: {}", deleteImageUrl);
                HttpDelete httpDelete = new HttpDelete(deleteImageUrl);
                String encodeStr = encodeUserAndPwd();
                if (encodeStr.equals("")) {
                    LOGGER.error("encode user and pwd failed!");
                    return false;
                }
                httpDelete.setHeader("Authorization", "Basic " + encodeStr);
                httpDelete.setHeader("X-Harbor-CSRF-Token", csrf);
                CloseableHttpResponse res = client.execute(httpDelete);
                InputStream inputStream = res.getEntity().getContent();
                byte[] bytes = new byte[inputStream.available()];
                int byteNums = inputStream.read(bytes);
                if (byteNums > 0) {
                    LOGGER.error("delete harbor image failed!");
                    return false;
                }
            } catch (IOException e) {
                LOGGER.error("call login or delete image interface occur error {}", e.getMessage());
                return false;
            }
        }

        return true;
    }

    private static String getCsrf() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("__csrf")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private static CloseableHttpClient createIgnoreSslHttpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setDefaultCookieStore(cookieStore).setRedirectStrategy(new DefaultRedirectStrategy()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String encodeUserAndPwd() {
        String user = harborUsername + ":" + harborPassword;
        String base64encodedString = "";
        try {
            base64encodedString = Base64.getEncoder().encodeToString(user.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("encode user and pwd failed!");
            return "";
        }
        return base64encodedString;
    }

}
