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
import java.net.MalformedURLException;
import java.net.URL;
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
import org.edgegallery.developer.exception.DeveloperException;
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
        String base64encodedString = Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8));
        return base64encodedString;
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
     * @param repo image repo
     * @param userName login user
     * @param password login pwd
     * @return
     */
    public static DockerClient getDockerClient(String repo, String userName, String password) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerTlsVerify(true)
            .withDockerCertPath("/usr/app/ssl").withRegistryUrl("https://" + repo).withRegistryUsername(userName)
            .withRegistryPassword(password).build();
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
     * @param userName login user name
     * @param loginUrl login url
     * @param endpoint harbor address
     * @param repoUserName login harbor user name
     * @param repoPwd login harbor password
     * @return
     */
    public static boolean isExsitOfProject(String userName, String loginUrl, String endpoint, String repoUserName,
        String repoPwd) {
        try (CloseableHttpClient client = ContainerImageUtil.createIgnoreSslHttpClient()) {
            URL url = new URL(loginUrl);
            String isExistUrl = String.format(Consts.HARBOR_PRO_IS_EXIST_URL, url.getProtocol(), endpoint, userName);
            LOGGER.warn(" isExist Url : {}", isExistUrl);
            HttpGet httpGet = new HttpGet(isExistUrl);
            String encodeStr = ContainerImageUtil.encodeUserAndPwd(repoUserName, repoPwd);
            if (encodeStr.equals("")) {
                LOGGER.error("encode user and pwd failed!");
                throw new DeveloperException("encode user and pwd failed!",
                    ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
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
            throw new DeveloperException("call get one project occur error!",
                ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
        }
        return true;
    }

    /**
     * create harbor project.
     *
     * @param projectName project Name
     * @param loginUrl loginurl
     * @param endpoint harbor address
     * @param repoUserName login harbor user name
     * @param repoPwd login harbor password
     * @return
     */
    public static String createHarborRepo(String projectName, String loginUrl, String endpoint, String repoUserName,
        String repoPwd) {
        String body = "{\"project_name\":\"" + projectName + "\",\"metadata\":{\"public\":\"true\"}}";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + ContainerImageUtil.encodeUserAndPwd(repoUserName, repoPwd));
        HttpEntity requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            URL url = new URL(loginUrl);
            String createUrl = String.format(Consts.HARBOR_IMAGE_CREATE_REPO_URL, url.getProtocol(), endpoint);
            response = REST_TEMPLATE.exchange(createUrl, HttpMethod.POST, requestEntity, String.class);
            LOGGER.warn("create harbor repo log:{}", response);
        } catch (RestClientException | MalformedURLException e) {
            LOGGER.error("Failed create harbor repo {} occur {}", projectName, e.getMessage());
            return "error";
        }
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return "ok";
        }
        LOGGER.error("Failed create harbor repo!");
        return "error";
    }

    /**
     * decompress image tar and get image tag.
     *
     * @param rootDir decompress path
     * @param imageFile image file
     * @return
     * @throws IOException
     */
    public static String deCompressAndGetRePoTags(String rootDir, File imageFile) throws IOException {
        File file = new File(rootDir);
        boolean res = ContainerImageUtil.deCompressTar(imageFile.getCanonicalPath(), file);
        String repoTags = "";
        if (res) {
            //Readmanifest.jsonContent
            File manFile = new File(rootDir + "manifest.json");
            String fileContent = FileUtils.readFileToString(manFile, "UTF-8");
            String[] st = fileContent.split(",");
            for (String repoTag : st) {
                if (repoTag.contains("RepoTags")) {
                    String[] repo = repoTag.split(":\\[");
                    repoTags = repo[1].substring(1, repo[1].length() - 2);
                }
            }
        }
        LOGGER.warn("repoTags: {} res {} ", repoTags, res);
        return repoTags;
    }

    /**
     * image retag and push to harbor.
     *
     * @param dockerClient docker client
     * @param imageId imageId
     * @param userName login user name
     * @param repoTags tags in image tar file
     * @return
     */
    public static boolean retagAndPush(DockerClient dockerClient, String imageId, String userName, String repoTags,
        String point, String project) {
        String uploadImgName = "";
        String[] names = repoTags.split(":");
        if (SystemImageUtil.isAdminUser()) {
            uploadImgName = new StringBuilder(point).append("/").append(project).append("/").append(names[0])
                .toString();
        } else {
            uploadImgName = new StringBuilder(point).append("/").append(userName).append("/").append(names[0])
                .toString();
        }

        //Mirror tagging，Repush
        String[] repos = repoTags.split(":");
        if (repos.length > 1 && !imageId.equals("")) {
            //tag image
            dockerClient.tagImageCmd(imageId, uploadImgName, repos[1]).withForce().exec();
            LOGGER.warn("Upload tagged docker image: {}", uploadImgName);
            //push image
            try {
                dockerClient.pushImageCmd(uploadImgName).start().awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("failed to push image {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * get image id from tags.
     *
     * @param repoTags tags of image
     * @param dockerClient dockerClient
     * @return
     */
    public static String getImageIdFromRepoTags(String repoTags, DockerClient dockerClient) {
        String[] names = repoTags.split(":");
        //Judge the compressed package manifest.json in RepoTags And the value of load Are the incoming mirror images equal
        LOGGER.debug(names[0]);
        List<Image> lists = dockerClient.listImagesCmd().withImageNameFilter(names[0]).exec();
        LOGGER.debug("lists is empty ?{},lists size {},number 0 {}", CollectionUtils.isEmpty(lists), lists.size(),
            lists.get(0));
        String imageId = "";
        if (!CollectionUtils.isEmpty(lists) && !StringUtils.isEmpty(repoTags)) {
            for (Image image : lists) {
                LOGGER.debug(image.getRepoTags()[0]);
                String[] images = image.getRepoTags();
                if (images[0].equals(repoTags)) {
                    imageId = image.getId();
                    LOGGER.debug(imageId);
                }
            }
        }
        LOGGER.warn("imageID: {} ", imageId);
        return imageId;
    }

    /**
     * addTypeOrStatusToList.
     *
     * @param imageType imageType
     * @return
     */
    public static List<String> addTypeOrStatusToList(String imageType) {
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
     * deleteImage.
     *
     * @param image image
     * @param userName userName
     * @return
     */
    public static boolean deleteImage(String image, String userName, String imageDomainName, String loginUrl,
        String imageProject, String repoUserName, String password) {
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
            URL url = null;
            try {
                url = new URL(loginUrl);
                //excute delete image operation
                String deleteImageUrl = "";
                if (SystemImageUtil.isAdminUser() && AccessUserUtil.getUser().getUserName().equals(userName)) {
                    deleteImageUrl = String
                        .format(Consts.HARBOR_IMAGE_DELETE_URL, url.getProtocol(), imageDomainName, imageProject,
                            imageName, imageVersion);
                } else {
                    deleteImageUrl = String
                        .format(Consts.HARBOR_IMAGE_DELETE_URL, url.getProtocol(), imageDomainName, userName, imageName,
                            imageVersion);
                }
                LOGGER.warn("delete image url: {}", deleteImageUrl);
                String deleteRes = deleteHarborImage(image, deleteImageUrl, repoUserName, password);
                if (deleteRes.equals("error")) {
                    LOGGER.error("delete harbor repo failed!");
                    return false;
                }
            } catch (MalformedURLException e) {
                LOGGER.error("call login or delete image interface occur error {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    private static String deleteHarborImage(String image, String url, String repoUserName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + ContainerImageUtil.encodeUserAndPwd(repoUserName, password));
        HttpEntity requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            LOGGER.warn("delete harbor image log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed delete harbor image {} occur {}", image, e.getMessage());
            return "error";
        }
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return "ok";
        }
        LOGGER.error("Failed delete harbor image!");
        return "error";
    }

    /**
     * get image list from harbor repo.
     *
     * @return
     */
    public static List<String> getHarborImageList(String loginUrl, String imageDomainName, String imageProject,
        String devUserName, String devPwd) {
        //create project
        try (CloseableHttpClient client = createIgnoreSslHttpClient()) {
            //get all image
            URL url = new URL(loginUrl);
            String getImageUrl = String
                .format(Consts.HARBOR_IMAGE_GET_LIST_URL, url.getProtocol(), imageDomainName, imageProject);
            LOGGER.warn("getImageUrl : {}", getImageUrl);
            HttpGet httpImage = new HttpGet(getImageUrl);
            String encodeStrImage = encodeUserAndPwd(devUserName, devPwd);
            if (encodeStrImage.equals("")) {
                LOGGER.error("encode user and pwd failed!");
            }
            httpImage.setHeader("Authorization", "Basic " + encodeStrImage);
            CloseableHttpResponse resImage = client.execute(httpImage);
            InputStream inputStreamImage = resImage.getEntity().getContent();
            String imageRes = IOUtils.toString(inputStreamImage, StandardCharsets.UTF_8);
            LOGGER.info("image response : {}", imageRes);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(imageRes) && imageRes.equals("[]")) {
                return Collections.EMPTY_LIST;
            }
            Gson gson = new Gson();
            Type type = new TypeToken<List<HarborImage>>() { }.getType();
            List<HarborImage> imageList = gson.fromJson(imageRes, type);
            List<String> names = new ArrayList<>();
            for (HarborImage harborImage : imageList) {
                String name = harborImage.getName();
                if (!name.substring(10).contains("/")) {
                    getTagsOfImages(name, names, url, client, encodeStrImage, imageDomainName, imageProject);
                }
            }
            return names;
        } catch (IOException e) {
            LOGGER.error("get image list from harbor repo {}", e.getMessage());
            throw new DeveloperException("get image list from harbor repo failed!",
                ResponseConsts.RET_GET_IMAGE_FROM_HARBOR_FAILED);
        }
    }

    private static void getTagsOfImages(String name, List<String> names, URL url, CloseableHttpClient client,
        String encode, String imageDomainName, String imageProject) throws IOException {
        //get tags of one image
        String getTagUrl = String
            .format(Consts.HARBOR_IMAGE_GET_TAGS_URL, url.getProtocol(), imageDomainName, imageProject,
                name.substring(10).trim());
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
                        String image = name + ":" + object.get("name").getAsString() + "+" + object.get("push_time")
                            .getAsString();
                        names.add(image.trim());
                    }
                }
            }
        }
    }

}
