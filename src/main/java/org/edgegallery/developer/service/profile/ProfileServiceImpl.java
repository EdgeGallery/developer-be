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

package org.edgegallery.developer.service.profile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.domain.shared.PluginChecker;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.mapper.profile.ProfileMapper;
import org.edgegallery.developer.model.profile.ProfileInfo;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Service("profileService")
public class ProfileServiceImpl implements ProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private static final String PROFILE_FILE = "profile.yaml";

    private static final String BASE_PAHT = InitConfigUtil.getWorkSpaceBaseDir()
        .concat(BusinessConfigUtil.getProfileFilePath());

    @Autowired
    private ProfileMapper profileMapper;

    @Override
    public ProfileInfo createProfile(MultipartFile file) {
        try {
            PluginChecker checker = new PluginChecker();
            checker.check(file);

            String id = UUID.randomUUID().toString();
            String baseFilePath = BASE_PAHT.concat(id);
            String zipFilePath = baseFilePath.concat(".zip");
            File zipFile = new File(zipFilePath);
            if (!zipFile.getParentFile().exists()) {
                zipFile.getParentFile().mkdirs();
            }
            zipFile.createNewFile();
            file.transferTo(zipFile);
            checker.check(zipFile);

            CompressFileUtils.unZip(zipFile, baseFilePath);
            ProfileInfo profileInfo = new ProfileInfo();
            profileInfo.setId(id);
            profileInfo.setFilePath(zipFilePath);
            profileInfo.setCreateTime(new Date());
            analysizeProfile(baseFilePath, profileInfo);

            validateDbOptResult(profileMapper.createProfile(profileInfo), "create profile failed.");
            LOGGER.info("create profile successfully.");
            return profileInfo;
        } catch (IOException e) {
            LOGGER.error("file transfer failed. {}", e);
            throw new FileOperateException("file transfer failed.", ResponseConsts.RET_MERGE_FILE_FAIL);
        }
    }

    @Override
    public ProfileInfo updateProfile(MultipartFile file, String profileId) {
        try {
            PluginChecker checker = new PluginChecker();
            checker.check(file);

            ProfileInfo profileInfo = profileMapper.getProfileById(profileId);
            checkParamNull(profileInfo, "profile does not exist, profileId: ".concat(profileId));

            String baseFilePath = BASE_PAHT.concat(profileId);
            FileUtils.deleteQuietly(new File(profileInfo.getFilePath()));
            FileUtils.deleteQuietly(new File(baseFilePath));

            File zipFile = new File(baseFilePath.concat(".zip"));
            zipFile.createNewFile();
            file.transferTo(zipFile);
            checker.check(zipFile);
            CompressFileUtils.unZip(zipFile, baseFilePath);

            analysizeProfile(baseFilePath, profileInfo);
            validateDbOptResult(profileMapper.updateProfile(profileInfo),
                "update profile failed, profileId: ".concat(profileId));
            LOGGER.info("update profile successfully.");
            return profileInfo;
        } catch (IOException e) {
            LOGGER.error("file transfer failed. {}", e);
            throw new FileOperateException("file transfer failed.", ResponseConsts.RET_MERGE_FILE_FAIL);
        }
    }

    @Override
    public Page<ProfileInfo> getAllProfiles(int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<ProfileInfo> pageInfo = new PageInfo<>(profileMapper.getAllProfiles());
        LOGGER.info("get all profiles successfully.");
        return new Page<ProfileInfo>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    @Override
    public ProfileInfo getProfileById(String profileId) {
        ProfileInfo profileInfo = profileMapper.getProfileById(profileId);
        checkParamNull(profileInfo, "profile does not exist, profileId: ".concat(profileId));
        LOGGER.info("get profile by id successfully.");
        return profileInfo;
    }

    @Override
    public Boolean deleteProfileById(String profileId) {
        ProfileInfo profileInfo = profileMapper.getProfileById(profileId);
        if (null == profileInfo) {
            LOGGER.info("profile does not exists, profileId: {}", profileId);
            return true;
        }
        validateDbOptResult(profileMapper.deleteProfileById(profileId),
            "delete profile failed, profileId: ".concat(profileId));
        FileUtils.deleteQuietly(new File(profileInfo.getFilePath()));
        FileUtils.deleteQuietly(new File(BASE_PAHT.concat(profileId)));
        return null;
    }

    /**
     * validate db operation result.
     *
     * @param result db operation result
     * @param msg error message
     */
    private void validateDbOptResult(int result, String msg) {
        if (result < 1) {
            LOGGER.error(msg);
            throw new DeveloperException(msg);
        }
    }

    /**
     * analysize field from profile file and set profileinfo model.
     *
     * @param baseFilePath baseFilePath
     * @param profileInfo profileInfo
     */
    private void analysizeProfile(String baseFilePath, ProfileInfo profileInfo) {
        try {
            String profileFilePath = baseFilePath.concat(File.separator).concat(PROFILE_FILE);
            File profileFile = new File(profileFilePath);

            String yamlContent = FileUtils.readFileToString(profileFile, StandardCharsets.UTF_8);
            Yaml yaml = new Yaml(new SafeConstructor());
            Map<String, Object> loaded = yaml.load(yamlContent);
            HashMap<String, Object> profile = (HashMap<String, Object>) loaded.get("profile");

            profileInfo.setName((String) profile.get("name"));
            profileInfo.setDescription((String) profile.get("descriptionCh"));
            profileInfo.setDescriptionEn((String) profile.get("descriptionEn"));
            profileInfo.setType((String) profile.get("type"));
            profileInfo.setIndustry((String) profile.get("industry"));
            profileInfo.setConfigFilePath(baseFilePath.concat(File.separator).concat((String) profile.get("config")));
            HashMap<String, Map<String, String>> appList = (HashMap<String, Map<String, String>>) profile.get("app");
            String seq = (String) profile.get("seq");
            profileInfo.setSeq(Arrays.asList(seq.split(",")));

            Map<String, String> deployFilePath = new HashMap<>();
            appList.keySet().stream().forEach(key -> {
                Map<String, String> appInfo = appList.get(key);
                String deploymentFile = appInfo.get("deploymentFile");
                deployFilePath.put(key, baseFilePath.concat(File.separator).concat(deploymentFile));
            });
            profileInfo.setDeployFilePath(deployFilePath);
        } catch (DomainException e) {
            LOGGER.error("Yaml deserialization failed {}", e.getMessage());
            throw new DomainException("Yaml deserialization failed.");
        } catch (IOException e) {
            LOGGER.error("read file to string failed. {}", e);
            throw new FileOperateException("read file to string failed", ResponseConsts.RET_MERGE_FILE_FAIL);
        }
    }

    /**
     * param is null or not.
     *
     * @param param param
     * @param msg error msg
     * @param <T> param type
     */
    private <T> void checkParamNull(T param, String msg) {
        if (null == param) {
            LOGGER.error(msg);
            throw new EntityNotFoundException(msg, ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
    }
}
