package org.edgegallery.developer.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.spencerwi.either.Either;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
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
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.ContainerImageMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.SystemImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service("imageServiceV2")
public class ImageServiceV2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceV2.class);

    private static final String SUBDIR_CONIMAGE = "ContainerImage";

    private static CookieStore cookieStore = new BasicCookieStore();

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Value("${upload.tempPath}")
    private String filePathTemp;

    @Value("${imagelocation.domainname:}")
    private String devRepoEndpoint;

    @Value("${imagelocation.username:}")
    private String devRepoUsername;

    @Value("${imagelocation.password:}")
    private String devRepoPassword;

    @Value("${imagelocation.project:}")
    private String devRepoProject;

    @Value("${security.oauth2.resource.jwt.key-uri:}")
    private String loginUrl;

    @Autowired
    private ContainerImageMapper containerImageMapper;

    @Autowired
    private ContainerImageMgmtServiceV2 containerImageMgmtServiceV2;

    /**
     * uploadHarborImage.
     *
     * @param request http request
     * @param chunk file chunk
     * @param imageId harbor imageId
     * @return
     */
    public ResponseEntity uploadHarborImage(HttpServletRequest request, Chunk chunk, String imageId) {
        try {
            LOGGER.info("upload harbor image file, fileName = {}, identifier = {}, chunkNum = {}", chunk.getFilename(),
                chunk.getIdentifier(), chunk.getChunkNumber());

            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                LOGGER.error("upload request is invalid.");
                throw new DeveloperException("upload request is invalid", ResponseConsts.RET_REQUEST_INVALID);
            }

            MultipartFile file = chunk.getFile();
            if (file == null) {
                LOGGER.error("there is no needed file");
                throw new DeveloperException("there is no needed file", ResponseConsts.RET_NO_NEEDED_FILE);
            }

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                LOGGER.error("invalid chunk number.");
                throw new DeveloperException("invalid chunk number", ResponseConsts.RET_CHUNK_NUMBER_INVALID);
            }

            LOGGER.info("save file to local directory.");
            String rootDir = getUploadSysImageRootDir(imageId);
            File uploadRootDir = new File(rootDir);
            if (!uploadRootDir.exists()) {
                boolean isMk = uploadRootDir.mkdirs();
                if (!isMk) {
                    String mkErr = "create temporary upload path failed";
                    LOGGER.error(mkErr);
                    throw new DeveloperException(mkErr, ResponseConsts.RET_TEMPORARY_PATH_FAILED);
                }
            }

            File outFile = new File(rootDir + chunk.getIdentifier(), chunkNumber + ".part");
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            String errMsg = "upload container image file exception.";
            LOGGER.error(errMsg);
            throw new DeveloperException(errMsg, ResponseConsts.RET_UPLOAD_CONTAINER_IMAGE_FAILED);
        }
    }

    /**
     * mergeHarborImage.
     *
     * @param fileName file name
     * @param guid file guid
     * @param imageId harbor imageId
     * @return
     */
    public ResponseEntity mergeHarborImage(String fileName, String guid, String imageId) {
        try {
            LOGGER.info("merge harbor image file, harborImage = {}, fileName = {}, guid = {}", imageId, fileName, guid);
            String rootDir = getUploadSysImageRootDir(imageId);
            String partFilePath = rootDir + guid;
            File partFileDir = new File(partFilePath);
            if (!partFileDir.exists() || !partFileDir.isDirectory()) {
                LOGGER.error("uploaded part file path not found!");
                throw new DeveloperException("uploaded part file path not found",
                    ResponseConsts.RET_FILE_PATH_NOT_FOUND);
            }

            File[] partFiles = partFileDir.listFiles();
            if (partFiles == null || partFiles.length == 0) {
                LOGGER.error("uploaded part file not found!");
                throw new DeveloperException("uploaded part file not found", ResponseConsts.RET_FILE_NOT_FOUND);
            }

            File mergedFile = new File(rootDir + File.separator + fileName);
            FileOutputStream mergedFileStream = new FileOutputStream(mergedFile, true);
            for (int i = 1; i <= partFiles.length; i++) {
                File partFile = new File(partFilePath, i + ".part");
                FileUtils.copyFile(partFile, mergedFileStream);
                partFile.delete();
            }
            mergedFileStream.close();
            //create repo by current user id
            String userName = AccessUserUtil.getUser().getUserName();
            // judge user private harbor repo is exist
            boolean isExist = isExsitOfProject(userName);
            if (!isExist && !SystemImageUtil.isAdminUser()) {
                String msg = createHarborRepo(imageId, userName);
                if (msg.equals("error")) {
                    LOGGER.error("create harbor repo failed!");
                    throw new DeveloperException("create harbor repo failed!",
                        ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
                }
            }
            //push image to created repo by current user id
            if (!pushImageToRepo(mergedFile, rootDir, userName, imageId, fileName)) {
                LOGGER.error("push image to repo failed!");
                throw new DeveloperException("push image to repo failed!",
                    ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
            }
            File uploadPath = new File(rootDir);
            FileUtils.cleanDirectory(uploadPath);
            LOGGER.info("harbor image file upload succeed.");
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            LOGGER.error("process merged file exception! {}", e.getMessage());
            throw new DeveloperException("process merged file exception",
                ResponseConsts.RET_PROCESS_MERGED_FILE_EXCEPTION);
        }
    }

    private boolean isExsitOfProject(String userName) {
        try (CloseableHttpClient client = createIgnoreSslHttpClient()) {
            URL url = new URL(loginUrl);
            String isExistUrl = String
                .format(Consts.HARBOR_PRO_IS_EXIST_URL, url.getProtocol(), devRepoEndpoint, userName);
            LOGGER.warn(" isExist Url : {}", isExistUrl);
            HttpGet httpGet = new HttpGet(isExistUrl);
            String encodeStr = encodeUserAndPwd();
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

    private String createHarborRepo(String imageId, String name) {
        String body = "{\"project_name\":\"" + name + "\",\"metadata\":{\"public\":\"true\"}}";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodeUserAndPwd());
        HttpEntity requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            URL url = new URL(loginUrl);
            String createUrl = String.format(Consts.HARBOR_IMAGE_CREATE_REPO_URL, url.getProtocol(), devRepoEndpoint);
            response = REST_TEMPLATE.exchange(createUrl, HttpMethod.POST, requestEntity, String.class);
            LOGGER.warn("create harbor repo log:{}", response);
        } catch (RestClientException | MalformedURLException e) {
            LOGGER.error("Failed create harbor repo {} occur {}", name, e.getMessage());
            return "error";
        }
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return "ok";
        }
        LOGGER.error("Failed create harbor repo!");
        return "error";
    }

    private boolean pushImageToRepo(File imageFile, String rootDir, String userName, String inputImageId,
        String fileName) throws IOException {
        DockerClient dockerClient = getDockerClient(devRepoEndpoint, devRepoUsername, devRepoPassword);
        try (InputStream inputStream = new FileInputStream(imageFile)) {
            //import image pkg
            dockerClient.loadImageCmd(inputStream).exec();
        } catch (FileNotFoundException e) {
            LOGGER.error("can not find image file,{}", e.getMessage());
            return false;
        }

        //Unzip the image package，Find out manifest.json middle RepoTags
        String repoTags = deCompressAndGetRePoTags(rootDir, imageFile);
        if (repoTags.equals("")) {
            return false;
        }
        //get image id
        String imageId = getImageIdFromRepoTags(repoTags, dockerClient);
        if (imageId.equals("")) {
            return false;
        }
        //push
        boolean ret = retagAndPush(dockerClient, imageId, userName, repoTags);
        if (!ret) {
            return false;
        }
        //create container image
        boolean retContainer = createContainerImage(repoTags, inputImageId, fileName, userName);
        if (!retContainer) {
            return false;
        }
        return true;
    }

    private String deCompressAndGetRePoTags(String rootDir, File imageFile) throws IOException {
        File file = new File(rootDir);
        boolean res = deCompress(imageFile.getCanonicalPath(), file);
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

    private boolean retagAndPush(DockerClient dockerClient, String imageId, String userName, String repoTags) {
        String uploadImgName = "";
        String[] names = repoTags.split(":");
        if (SystemImageUtil.isAdminUser()) {
            uploadImgName = new StringBuilder(devRepoEndpoint).append("/").append(devRepoProject).append("/")
                .append(names[0]).toString();
        } else {
            uploadImgName = new StringBuilder(devRepoEndpoint).append("/").append(userName).append("/").append(names[0])
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

    private boolean createContainerImage(String repoTags, String inputImageId, String fileName, String userName) {
        String uploadImgName = "";
        String[] names = repoTags.split(":");
        if (SystemImageUtil.isAdminUser()) {
            uploadImgName = new StringBuilder(devRepoEndpoint).append("/").append(devRepoProject).append("/")
                .append(names[0]).toString();
        } else {
            uploadImgName = new StringBuilder(devRepoEndpoint).append("/").append(userName).append("/").append(names[0])
                .toString();
        }
        ContainerImage containerImage = new ContainerImage();
        containerImage.setImageId(inputImageId);
        containerImage.setImageType("private");
        containerImage.setImageName(names[0]);
        containerImage.setImageVersion(names[1].trim());
        containerImage.setImagePath(uploadImgName + ":" + names[1].trim());
        containerImage.setUserId(AccessUserUtil.getUser().getUserId());
        containerImage.setUserName(AccessUserUtil.getUser().getUserName());
        containerImage.setFileName(fileName);
        Either<FormatRespDto, ContainerImage> either = containerImageMgmtServiceV2.createContainerImage(containerImage);
        if (either.isLeft()) {
            LOGGER.error("create harbor image db record failed!");
            return false;
        }

        return true;

    }

    private String getImageIdFromRepoTags(String repoTags, DockerClient dockerClient) {
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

    private DockerClient getDockerClient(String repo, String userName, String password) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerTlsVerify(true)
            .withDockerCertPath("/usr/app/ssl").withRegistryUrl("https://" + repo).withRegistryUsername(userName)
            .withRegistryPassword(password).build();
        LOGGER.warn("docker register url: {}", config.getRegistryUrl());
        return DockerClientBuilder.getInstance(config).build();
    }

    private boolean deCompress(String tarFile, File destFile) {
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

    private String getUploadSysImageRootDir(String imageId) {
        return filePathTemp + File.separator + SUBDIR_CONIMAGE + File.separator + imageId + File.separator;
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
        String user = devRepoUsername + ":" + devRepoPassword;
        String base64encodedString = Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8));
        return base64encodedString;
    }

    private static String getCsrf() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("__csrf")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private static String getGorillaCsrf() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("_gorilla_csrf")) {
                return cookie.getValue();
            }
        }
        return "";
    }
}
