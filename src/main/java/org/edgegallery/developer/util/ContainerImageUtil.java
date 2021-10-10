package org.edgegallery.developer.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLContext;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.HarborException;
import org.edgegallery.developer.model.containerimage.HarborImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public final class ContainerImageUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerImageUtil.class);

    private static CookieStore cookieStore = new BasicCookieStore();

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String HARBOR_PROTOCOL = "https";

    private ContainerImageUtil() {
        throw new IllegalStateException("ContainerImageUtil class");
    }

    /**
     * base64 encoding.
     *
     * @param userName username
     * @param password pwd
     * @return
     */
    public static String encodeUserAndPwd(String userName, String password) {
        String user = userName + ":" + password;
        return Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * unzip image tar.
     *
     * @param tarFile image file
     * @param destFile file in image file
     * @return
     */
    public static boolean deCompressTar(String tarFile, File destFile) {
        TarArchiveInputStream tis = null;
        try (FileInputStream fis = new FileInputStream(tarFile)) {

            if (tarFile.contains(".tar")) {
                tis = new TarArchiveInputStream(new BufferedInputStream(fis));
            } else {
                GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fis));
                tis = new TarArchiveInputStream(gzipInputStream);
            }

            TarArchiveEntry tarEntry;
            while ((tarEntry = tis.getNextTarEntry()) != null) {
                if (tarEntry.isDirectory()) {
                    continue;
                } else {
                    File outputFile = new File(destFile + File.separator + tarEntry.getName());
                    LOGGER.info("deCompressing... {}", outputFile.getName());
                    boolean result = outputFile.getParentFile().mkdirs();
                    LOGGER.debug("create directory result {}", result);
                    IOUtils.copy(tis, new FileOutputStream(outputFile));
                }
            }
        } catch (IOException ex) {
            LOGGER.error("failed to decompress, IO exception  {} ", ex.getMessage());
            return false;
        } finally {
            if (tis != null) {
                try {
                    tis.close();
                } catch (IOException ex) {
                    LOGGER.error("failed to close tar input stream {} ", ex.getMessage());
                }
            }
        }
        return true;
    }

    /**
     * get docker client.
     *
     * @return
     */
    public static DockerClient getDockerClient() {
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerTlsVerify(true)
            .withDockerCertPath("/usr/app/ssl").withRegistryUrl("https://" + imageConfig.getDomainname())
            .withRegistryUsername(imageConfig.getUsername()).withRegistryPassword(imageConfig.getPassword()).build();
        LOGGER.warn("docker register url: {}", config.getRegistryUrl());
        return DockerClientBuilder.getInstance(config).build();
    }

    /**
     * create http client.
     *
     * @return
     */
    public static CloseableHttpClient createIgnoreSslHttpClient() {
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

    /**
     * judge project exist before create.
     *
     * @param projectName login user name
     * @return
     */
    public static boolean isExist(String projectName) {
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        try (CloseableHttpClient client = ContainerImageUtil.createIgnoreSslHttpClient()) {
            String isExistUrl = String
                .format(Consts.HARBOR_PRO_IS_EXIST_URL, HARBOR_PROTOCOL, imageConfig.getDomainname(), projectName);
            LOGGER.warn(" isExist Url : {}", isExistUrl);
            HttpGet httpGet = new HttpGet(isExistUrl);
            String encodeStr = ContainerImageUtil
                .encodeUserAndPwd(imageConfig.getUsername(), imageConfig.getPassword());
            if (encodeStr.equals("")) {
                LOGGER.error("encode user and pwd failed!");
                throw new HarborException("encode user and pwd failed!", ResponseConsts.RET_HARBOR_ENCODE_FAIL);
            }
            httpGet.setHeader("Authorization", "Basic " + encodeStr);
            CloseableHttpResponse res = client.execute(httpGet);
            InputStream inputStream = res.getEntity().getContent();
            String imageRes = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            if (imageRes.equals("null")) {
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("call get one project occur error {}", e.getMessage());
            throw new HarborException("call get one project occur error!",
                ResponseConsts.RET_QUERY_HARBOR_PROJECT_FAIL);
        }
        return true;
    }

    /**
     * create harbor project.
     *
     * @param projectName project Name
     * @return
     */
    public static boolean createHarborRepo(String projectName) {
        String body = "{\"project_name\":\"" + projectName + "\",\"metadata\":{\"public\":\"true\"}}";
        HttpHeaders headers = new HttpHeaders();
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        headers.set("Authorization",
            "Basic " + ContainerImageUtil.encodeUserAndPwd(imageConfig.getUsername(), imageConfig.getPassword()));
        HttpEntity requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            String createUrl = String
                .format(Consts.HARBOR_IMAGE_CREATE_REPO_URL, HARBOR_PROTOCOL, imageConfig.getDomainname());
            response = REST_TEMPLATE.exchange(createUrl, HttpMethod.POST, requestEntity, String.class);
            LOGGER.warn("create harbor repo log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed create harbor repo {} occur {}", projectName, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return true;
        }
        LOGGER.error("Failed create harbor repo!");
        return false;
    }

    /**
     * decompress image tar and get image tag.
     *
     * @param rootDir decompress path
     * @param imageFile image file
     * @return
     */
    public static String deCompressAndGetRePoTags(String rootDir, File imageFile) throws IOException {
        File file = new File(rootDir);
        boolean res = ContainerImageUtil.deCompressTar(imageFile.getCanonicalPath(), file);
        String repoTags = "";
        if (res) {
            //Readmanifest.jsonContent
            File manFile = new File(rootDir + "manifest.json");
            String fileContent = FileUtils.readFileToString(manFile, "UTF-8");
            JsonParser jp = new JsonParser();
            JsonArray jsonArray = jp.parse(fileContent).getAsJsonArray();
            List<String> tagList = new ArrayList<>();
            for (JsonElement jsonElement : jsonArray) {
                JsonObject ob = jsonElement.getAsJsonObject();
                if (!ob.get("RepoTags").isJsonNull()) {
                    JsonElement eleTag = ob.get("RepoTags");
                    JsonArray jsonArrayTag = eleTag.getAsJsonArray();
                    for (JsonElement element : jsonArrayTag) {
                        String tag = element.getAsString();
                        tagList.add(tag);
                    }
                }
            }
            LOGGER.warn("tagList {}", tagList);
            if (!CollectionUtils.isEmpty(tagList)) {
                repoTags = tagList.get(0);
            }
        }
        if (StringUtils.isNotEmpty(repoTags) && repoTags.contains("/")) {
            LOGGER.error("pls retag image tar,insure image name not clude '/'");
            return "";
        }
        LOGGER.warn("repoTags: {} res {} ", repoTags, res);
        return repoTags;
    }

    /**
     * image retag and push to harbor.
     *
     * @param dockerClient docker client
     * @param imageId imageId
     * @param projectName login user name
     * @param repoTags tags in image tar file
     * @return
     */
    public static boolean retagAndPush(DockerClient dockerClient, String imageId, String projectName, String repoTags) {
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        String[] images = repoTags.split(":");
        String imageName = images[0];
        String imageVersion = images[1];
        String uploadImgName = imageConfig.getDomainname() + "/" + projectName + "/" + imageName;
        LOGGER.warn("uploadImgName: {}", uploadImgName);
        //image retag,push
        if (!imageId.equals("")) {
            //tag image
            dockerClient.tagImageCmd(imageId, uploadImgName, imageVersion).withForce().exec();
            LOGGER.warn("Upload tagged docker image: {}", uploadImgName);
            //push image
            try {
                dockerClient.pushImageCmd(uploadImgName).start().awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("failed to push image {}", e.getMessage());
                return false;
            }
            return true;
        } else {
            LOGGER.error("imageId is null");
            return false;
        }
    }

    /**
     * get image id from tags.
     *
     * @param repoTags tags of image
     * @param dockerClient dockerClient
     * @return
     */
    public static String getImageIdFromRepoTags(String repoTags, DockerClient dockerClient) {
        String[] imageArr = repoTags.split(":");
        //Judge the compressed package manifest.json in RepoTags And the value of load Are the
        // incoming mirror images equal
        String imageName = imageArr[0];
        LOGGER.warn(imageArr[0]);
        List<Image> lists = dockerClient.listImagesCmd().withImageNameFilter(imageName).exec();
        LOGGER.debug("lists is empty ?{},lists size {},number 0 {}", CollectionUtils.isEmpty(lists), lists.size(),
            lists.get(0));
        String imageId = "";
        if (!CollectionUtils.isEmpty(lists) && !StringUtils.isEmpty(repoTags)) {
            for (Image image : lists) {
                LOGGER.warn(image.getRepoTags()[0]);
                String[] images = image.getRepoTags();
                if (images[0].equals(repoTags)) {
                    imageId = image.getId();
                    LOGGER.warn(imageId);
                }
            }
        }
        LOGGER.warn("imageID: {} ", imageId);
        return imageId;
    }

    /**
     * deleteImage.
     *
     * @param image image
     * @param userName userName
     * @return
     */
    public static boolean deleteImage(String image, String userName) {
        //Split image
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        if (!image.contains(imageConfig.getDomainname())) {
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
            //excute delete image operation
            String deleteImageUrl = "";
            if (SystemImageUtil.isAdminUser() && AccessUserUtil.getUser().getUserName().equals(userName)) {
                deleteImageUrl = String
                    .format(Consts.HARBOR_IMAGE_DELETE_URL, HARBOR_PROTOCOL, imageConfig.getDomainname(),
                        imageConfig.getProject(), imageName, imageVersion);
            } else {
                userName = userName.replaceAll(Consts.PATTERN, "").toLowerCase();
                deleteImageUrl = String
                    .format(Consts.HARBOR_IMAGE_DELETE_URL, HARBOR_PROTOCOL, imageConfig.getDomainname(), userName,
                        imageName, imageVersion);
            }
            LOGGER.warn("delete image url: {}", deleteImageUrl);
            boolean deleteRes = deleteHarborImage(image, deleteImageUrl, imageConfig.getUsername(),
                imageConfig.getPassword());
            if (!deleteRes) {
                LOGGER.error("delete harbor repo failed!");
                return false;
            }
        }
        return true;
    }

    private static boolean deleteHarborImage(String image, String url, String repoUserName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + ContainerImageUtil.encodeUserAndPwd(repoUserName, password));
        HttpEntity requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            LOGGER.warn("delete harbor image log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed delete harbor image {} occur {}", image, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return true;
        }
        LOGGER.error("Failed delete harbor image!");
        return false;
    }

    /**
     * get image list from harbor repo.
     *
     * @return
     */
    public static List<String> getHarborImageList() {
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        //create project
        try (CloseableHttpClient client = createIgnoreSslHttpClient()) {
            //get all image
            String getImageUrl = String
                .format(Consts.HARBOR_IMAGE_GET_LIST_URL, HARBOR_PROTOCOL, imageConfig.getDomainname(),
                    imageConfig.getProject());
            LOGGER.warn("getImageUrl : {}", getImageUrl);
            HttpGet httpImage = new HttpGet(getImageUrl);
            String encodeStrImage = encodeUserAndPwd(imageConfig.getUsername(), imageConfig.getPassword());
            if (encodeStrImage.equals("")) {
                LOGGER.error("encode user and pwd failed!");
            }
            httpImage.setHeader("Authorization", "Basic " + encodeStrImage);
            CloseableHttpResponse resImage = client.execute(httpImage);
            InputStream inputStreamImage = resImage.getEntity().getContent();
            String imageRes = IOUtils.toString(inputStreamImage, StandardCharsets.UTF_8);
            LOGGER.info("image response : {}", imageRes);
            if (StringUtils.isNotEmpty(imageRes) && imageRes.equals("[]")) {
                return Collections.emptyList();
            }
            Gson gson = new Gson();
            Type type = new TypeToken<List<HarborImage>>() { }.getType();
            List<HarborImage> imageList = gson.fromJson(imageRes, type);
            List<String> harborImageList = new ArrayList<>();
            for (HarborImage harborImage : imageList) {
                String imageName = harborImage.getName();
                if (!imageName.substring(10).contains("/")) {
                    getTagsOfImages(imageName, harborImageList, client, encodeStrImage, imageConfig.getDomainname(),
                        imageConfig.getProject());
                }
            }
            return harborImageList;
        } catch (IOException e) {
            String err = "get image list from harbor repo failed!";
            LOGGER.error("get image list from harbor repo {}", e.getMessage());
            throw new HarborException(err, ResponseConsts.RET_GET_HARBOR_IMAGE_LIST_FAIL);
        }
    }

    private static void getTagsOfImages(String imageName, List<String> harborImageList, CloseableHttpClient client,
        String encode, String imageDomainName, String imageProject) throws IOException {
        //get tags of one image
        String getTagUrl = String
            .format(Consts.HARBOR_IMAGE_GET_TAGS_URL, HARBOR_PROTOCOL, imageDomainName, imageProject,
                imageName.substring(10).trim());
        LOGGER.info("getTagUrl : {}", getTagUrl);
        HttpGet httpTag = new HttpGet(getTagUrl);
        httpTag.setHeader("Authorization", "Basic " + encode);
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
                        String image = imageName + ":" + object.get("name").getAsString() + "+" + object
                            .get("push_time").getAsString();
                        harborImageList.add(image.trim());
                    }
                }
            }
        }
    }

}
