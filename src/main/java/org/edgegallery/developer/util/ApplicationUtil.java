package org.edgegallery.developer.util;

import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUtil.class);

    public static String getPackageBasePath(String applicationId, String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + applicationId
            + File.separator + packageId;
    }

    public static String getApplicationBasePath(String applicationId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + applicationId;
    }

}
