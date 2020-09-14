/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.EnumUtils;
import org.edgegallery.developer.common.enums.EnumAppAffinity;
import org.edgegallery.developer.common.enums.EnumAppType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
public class AppRequestParam {

    private String appId;

    private MultipartFile appFile;

    private MultipartFile logoFile;

    private String affinity;

    private String industry;

    private String type;

    private String appDesc;

    private String userId;

    /**
     * checkApp.
     *
     * @return
     */
    public static boolean checkApp(AppRequestParam app) {

        MultipartFile appFile = app.getAppFile();
        MultipartFile logoFile = app.getLogoFile();

        if (appFile == null || appFile.isEmpty()) {
            return false;
        }

        if (logoFile == null || logoFile.isEmpty()) {
            return false;
        }
        String appName = appFile.getOriginalFilename();
        if (StringUtils.isEmpty(appName)) {
            return false;
        }
        if (!appName.endsWith(".csar")) {
            return false;
        }
        if (!validateType(app.getType())) {
            return false;
        }
        return validateAffinity(app.getAffinity());
    }

    /**
     * isAffinityclude.
     *
     * @return
     */
    private static boolean validateAffinity(String affinityStr) {
        if (StringUtils.isEmpty(affinityStr)) {
            return false;
        }
        String[] affinities = affinityStr.split(",");
        for (String aff : affinities) {
            if (!EnumUtils.isValidEnum(EnumAppAffinity.class, aff.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    /**
     * isInclude.
     *
     * @return
     */
    private static boolean validateType(String type) {
        if (StringUtils.isEmpty(type)) {
            return false;
        }
        for (EnumAppType e : EnumAppType.values()) {
            if (e.getEnglishValue().equalsIgnoreCase(type) || e.getChineseValue().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
