/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VMImageUtil {

    private VMImageUtil() {
        throw new IllegalStateException("MepHostUtil class");
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(VMImageUtil.class);

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
