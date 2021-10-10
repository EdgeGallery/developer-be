package org.edgegallery.developer.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SystemImageUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(SystemImageUtil.class);

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
