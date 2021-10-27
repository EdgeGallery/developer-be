package org.edgegallery.developer.util;

import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class applicationUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(applicationUtil.class);

    public static String getPackageBasePath(String applicationId, String packageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + applicationId
            + File.separator + packageId;
    }

    public static String getApplicationBasePath(String applicationId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + applicationId;
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

}
