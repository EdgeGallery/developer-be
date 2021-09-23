package org.edgegallery.developer.util;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemImageUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(SystemImageUtil.class);

    private static final String SUBDIR_SYSIMAGE = "SystemImage";

    private static String tempUploadPath;

    private static String fileServerAddress;

    @Value("${upload.tempPath}")
    public void setTempUploadPath(String uploadPath) {
        tempUploadPath = uploadPath;
    }

    @Value("${fileserver.address}")
    public void setFileServerAddress(String serverAddress) {
        fileServerAddress = serverAddress;
    }

    /**
     * isAdminUser.
     *
     * @return boolean
     */
    public static boolean isAdminUser() {
        String currUserAuth = AccessUserUtil.getUser().getUserAuth();
        return !StringUtils.isEmpty(currUserAuth) && currUserAuth.contains(Consts.ROLE_DEVELOPER_ADMIN);
    }

    /**
     * pushSystemImage.
     *
     * @return boolean
     */
    public static boolean cancelOnRemoteFileServer(String identifier) {
        return HttpClientUtil.cancelSliceUpload(fileServerAddress, identifier);
    }

    public static String mergeOnRemoteFileServer(String identifier, String mergeFileName) {
        try {
            String uploadResult = HttpClientUtil
                .sliceMergeFile(fileServerAddress, identifier, mergeFileName, AccessUserUtil.getUserId());
            if (uploadResult == null) {
                LOGGER.error("merge on remote file server failed.");
                return null;
            }
            Gson gson = new Gson();
            Map<String, String> uploadResultModel = gson.fromJson(uploadResult, Map.class);
            return fileServerAddress + String
                .format(Consts.SYSTEM_IMAGE_DOWNLOAD_URL, uploadResultModel.get("imageId"));
        } catch (Exception e) {
            LOGGER.error("merge on remote file server failed. {}", e.getMessage());
            return null;
        }
    }

    /**
     * cleanWorkDir.
     */
    public static void cleanWorkDir(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            LOGGER.error("delete work directory failed.");
        }
    }

    /**
     * getUploadSysImageRootDir.
     */
    public static String getUploadSysImageRootDir(int systemId) {
        return tempUploadPath + File.separator + SUBDIR_SYSIMAGE + File.separator + systemId + File.separator;
    }

    /**
     * splitParam.
     *
     * @param param param
     * @return
     */
    public static List<String> splitParam(String param) {
        List<String> list = new ArrayList<>();
        if (!param.contains(",")) {
            list.add(param);
        } else {
            String[] arr = param.split(",");
            for (String str : arr) {
                list.add(str);
            }
        }
        return list;
    }
}
