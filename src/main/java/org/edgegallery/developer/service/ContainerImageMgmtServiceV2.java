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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
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
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.ContainerImageMapper;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.model.containerimage.ContainerImageReq;
import org.edgegallery.developer.model.containerimage.EnumContainerImageStatus;
import org.edgegallery.developer.model.containerimage.HarborImage;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.ListUtil;
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

    private static final String SUBDIR_CONIMAGE = "ContainerImage";

    @Value("${upload.tempPath}")
    private String filePathTemp;

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
                if (imageName.equals(image.getImageName())) {
                    String errorMsg = "exist the same imageName";
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
        String imageType = containerImageReq.getImageType();
        String imageStatus = containerImageReq.getImageStatus();
        List<String> types = new ArrayList<>();
        List<String> status = new ArrayList<>();
        if (StringUtils.isNotEmpty(imageType)) {
            types = addTypeOrStatusToList(imageType);
        }
        if (StringUtils.isNotEmpty(imageStatus)) {
            status = addTypeOrStatusToList(imageStatus);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("imageName", containerImageReq.getImageName());
        map.put("createTimeBegin", containerImageReq.getCreateTimeBegin());
        map.put("createTimeEnd", containerImageReq.getCreateTimeEnd());
        map.put("userId", containerImageReq.getUserId());
        map.put("sortBy", containerImageReq.getSortBy());
        map.put("sortOrder", containerImageReq.getSortOrder());
        map.put("imageType", types);
        map.put("imageStatus", status);
        PageInfo pageInfo = null;
        if (SystemImageUtil.isAdminUser()) {
            pageInfo = new PageInfo<>(containerImageMapper.getAllImageByAdminAuth(map));
        } else {
            pageInfo = new PageInfo<>(containerImageMapper.getAllImageByOrdinaryAuth(map));
        }
        if (pageInfo != null) {
            LOGGER.info("Get all container image success.");
            return new Page<ContainerImage>(pageInfo.getList(), containerImageReq.getLimit(),
                containerImageReq.getOffset(), pageInfo.getTotal());
        }
        return null;
    }

    /**
     * getAllContainerImages.
     */
    public List<ContainerImage> getAllImages(String userId) {
        List<ContainerImage> list;
        if (SystemImageUtil.isAdminUser()) {
            list = containerImageMapper.getAllImageByAdmin();
        } else {
            list = containerImageMapper.getAllImageByOrdinary(userId);
        }
        return list;
    }

    private List<String> addTypeOrStatusToList(String imageType) {
        List<String> typeList = new ArrayList<>();
        if (imageType.contains(",")) {
            String[] types = imageType.split(",");
            for (String type : types) {
                typeList.add(type);
            }
        } else {
            typeList.add(imageType);
        }
        return typeList;
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
        if (!oldImage.getImageName().equals(imageName)) {
            List<ContainerImage> imageList = containerImageMapper.getAllImage();
            if (!CollectionUtils.isEmpty(imageList)) {
                for (ContainerImage image : imageList) {
                    if (imageName.equals(image.getImageName())) {
                        String errorMsg = "exist the same imageName";
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

    /**
     * cancelUploadHarborImage.
     *
     * @param imageId harbor image Id
     * @return
     */
    public ResponseEntity cancelUploadHarborImage(String imageId) {
        LOGGER.info("cancel upload harbor image file, harborImageId = {}, ", imageId);

        ContainerImage containerImage = containerImageMapper.getContainerImage(imageId);
        if (EnumContainerImageStatus.UPLOADING_MERGING == containerImage.getImageStatus()) {
            LOGGER.error("harbor image is merging, it cannot be cancelled.");
            throw new DeveloperException("harbor image is merging, it cannot be cancelled",
                ResponseConsts.RET_CONTAINER_IMAGE_CANCELLED_FAILED);
        }

        LOGGER.info("update status and remove local directory.");
        int updateRes = containerImageMapper
            .updateContainerImageStatus(imageId, EnumContainerImageStatus.UPLOAD_CANCELLED.toString());
        if (updateRes < 1) {
            LOGGER.error("update image status failed.");
            throw new DeveloperException("update image status failed",
                ResponseConsts.RET_CONTAINER_IMAGE_CANCELLED_FAILED);
        }
        String rootDir = getUploadSysImageRootDir(imageId);
        SystemImageUtil.cleanWorkDir(new File(rootDir));
        return ResponseEntity.ok().build();
    }

    /**
     * synchronizeHarborImage.
     */
    public ResponseEntity synchronizeHarborImage() {
        LOGGER.info("begin synchronize image...");
        // get imagePath list from db
        List<ContainerImage> containerImages = containerImageMapper.getAllImageByAdmin();
        List<String> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(containerImages)) {
            for (ContainerImage containerImage : containerImages) {
                String image = containerImage.getImagePath();
                if (StringUtils.isNotEmpty(image)) {
                    list.add(image.substring(image.indexOf("/") + 1).trim());
                }
            }
        }
        // get Harbor image list
        List<String> harborList = getHarborImageList();
        if (CollectionUtils.isEmpty(harborList)) {
            LOGGER.error("no need synchronize!");
            throw new DeveloperException("no need synchronize!", ResponseConsts.RET_SYNCHRONIZE_IMAGE_NO_NEED);
        }
        List<String> imageList = new ArrayList<>();
        for (String harbor : harborList) {
            imageList.add(harbor.substring(harbor.indexOf("/") + 1, harbor.indexOf("+")));
        }
        if (ListUtil.isEquals(list, imageList) || list.containsAll(imageList)) {
            LOGGER.error("no need synchronize!");
            throw new DeveloperException("no need synchronize!", ResponseConsts.RET_SYNCHRONIZE_IMAGE_NO_NEED);
        }

        for (String harborImage : harborList) {
            ContainerImage containerImage = new ContainerImage();
            containerImage.setImageId(UUID.randomUUID().toString());
            String imageName = harborImage.substring(harborImage.indexOf("/") + 1, harborImage.indexOf(":"));
            containerImage.setImageName(imageName);
            containerImage
                .setImageVersion(harborImage.substring(harborImage.indexOf(":") + 1, harborImage.indexOf("+")));
            containerImage.setUserId(AccessUserUtil.getUser().getUserId());
            containerImage.setUserName(AccessUserUtil.getUser().getUserName());
            String pushTime = harborImage.substring(harborImage.indexOf("+") + 1);
            containerImage.setUploadTime(new Date(Instant.parse(pushTime).toEpochMilli()));
            containerImage.setCreateTime(new Date());
            containerImage.setImageType("private");
            containerImage.setImagePath(
                imageDomainName + "/" + harborImage.substring(harborImage.indexOf("/") + 1, harborImage.indexOf("+")));
            containerImage.setImageStatus(EnumContainerImageStatus.UPLOAD_SUCCEED);
            containerImage.setFileName(imageName + ".tar");
            int res = containerImageMapper.createContainerImage(containerImage);
            if (res < 1) {
                LOGGER.error("create container image failed!");
                throw new DeveloperException("create container image failed",
                    ResponseConsts.RET_CREATE_CONTAINER_IMAGE_FAILED);
            }
        }
        LOGGER.info("end synchronize image...");
        return ResponseEntity.ok().build();

    }

    private List<String> getHarborImageList() {
        //create project
        try (CloseableHttpClient client = createIgnoreSslHttpClient()) {
            //get all image
            URL url = new URL(loginUrl);
            String getImageUrl = String
                .format(Consts.HARBOR_IMAGE_GET_LIST_URL, url.getProtocol(), imageDomainName, imageProject);
            LOGGER.warn("getImageUrl : {}", getImageUrl);
            HttpGet httpImage = new HttpGet(getImageUrl);
            String encodeStrImage = encodeUserAndPwd();
            if (encodeStrImage.equals("")) {
                LOGGER.error("encode user and pwd failed!");
            }
            httpImage.setHeader("Authorization", "Basic " + encodeStrImage);
            CloseableHttpResponse resImage = client.execute(httpImage);
            InputStream inputStreamImage = resImage.getEntity().getContent();
            String imageRes = IOUtils.toString(inputStreamImage, StandardCharsets.UTF_8);
            LOGGER.info("image response : {}", imageRes);
            Gson gson = new Gson();
            Type type = new TypeToken<List<HarborImage>>() { }.getType();
            List<HarborImage> imageList = gson.fromJson(imageRes, type);
            List<String> names = new ArrayList<>();
            for (HarborImage harborImage : imageList) {
                String name = harborImage.getName();
                if (!name.substring(10).contains("/")) {
                    //get tags of one image
                    String getTagUrl = String
                        .format(Consts.HARBOR_IMAGE_GET_TAGS_URL, url.getProtocol(), imageDomainName, imageProject,
                            name.substring(10).trim());
                    LOGGER.info("getTagUrl : {}", getTagUrl);
                    HttpGet httpTag = new HttpGet(getTagUrl);
                    String encodeStrTag = encodeUserAndPwd();
                    if (encodeStrTag.equals("")) {
                        LOGGER.error("encode user and pwd failed!");
                    }
                    httpTag.setHeader("Authorization", "Basic " + encodeStrImage);
                    CloseableHttpResponse tagImage = client.execute(httpTag);
                    InputStream inputStreamTag = tagImage.getEntity().getContent();
                    String tagRes = IOUtils.toString(inputStreamTag, StandardCharsets.UTF_8);
                    // convert string to json
                    JsonParser jp = new JsonParser();
                    JsonArray jsonArray = jp.parse(tagRes).getAsJsonArray();
                    for (JsonElement jsonElement : jsonArray) {
                        JsonObject ob = jsonElement.getAsJsonObject();
                        if (!ob.get("tags").isJsonNull()) {
                            JsonElement eleTag = ob.get("tags");
                            JsonArray jsonArrayTag = eleTag.getAsJsonArray();
                            for (JsonElement element : jsonArrayTag) {
                                JsonObject object = element.getAsJsonObject();
                                if (!object.get("name").isJsonNull() && !object.get("push_time").isJsonNull()) {
                                    String image = name + ":" + object.get("name").getAsString() + "+" + object
                                        .get("push_time").getAsString();
                                    names.add(image.trim());
                                }
                            }
                        }
                    }
                }
            }
            return names;
        } catch (IOException e) {
            LOGGER.error("get image list from harbor repo {}", e.getMessage());
            throw new DeveloperException("get image list from harbor repo failed!",
                ResponseConsts.RET_GET_IMAGE_FROM_HARBOR_FAILED);
        }
    }

    private String getUploadSysImageRootDir(String imageId) {
        return filePathTemp + File.separator + SUBDIR_CONIMAGE + File.separator + imageId + File.separator;
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
                String userLoginUrl = String.format(Consts.HARBOR_IMAGE_LOGIN_URL, url.getProtocol(), imageDomainName);
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
                String deleteImageUrl = "";
                if (SystemImageUtil.isAdminUser()) {
                    deleteImageUrl = String
                        .format(Consts.HARBOR_IMAGE_DELETE_URL, url.getProtocol(), imageDomainName, imageProject,
                            imageName, imageVersion);
                } else {
                    deleteImageUrl = String.format(Consts.HARBOR_IMAGE_DELETE_URL, url.getProtocol(), imageDomainName,
                        AccessUserUtil.getUser().getUserId(), imageName, imageVersion);
                }
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
