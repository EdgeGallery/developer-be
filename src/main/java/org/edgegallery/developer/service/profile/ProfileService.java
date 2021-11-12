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

import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.profile.ProfileInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    /**
     * create profile.
     *
     * @param file profile file
     * @return profile info
     */
    ProfileInfo createProfile(MultipartFile file);

    /**
     * update profile.
     *
     * @param file profile file
     * @param profileId profileId
     * @return profile info
     */
    ProfileInfo updateProfile(MultipartFile file, String profileId);

    /**
     * get all profiles.
     *
     * @param limit limit
     * @param offset offset
     * @return profile info list
     */
    Page<ProfileInfo> getAllProfiles(int limit, int offset);

    /**
     * get profile by id.
     *
     * @param profileId profile id
     * @return profile info
     */
    ProfileInfo getProfileById(String profileId);

    /**
     * delete profile by id.
     *
     * @param profileId profile id
     * @return true
     */
    Boolean deleteProfileById(String profileId);

    /**
     * download profile by id.
     *
     * @param profileId profile id
     * @return profile content
     */
    ResponseEntity<byte[]> downloadProfileById(String profileId);

    /**
     * create application by profile id.
     *
     * @param profileId profile id
     * @param iconFile icon file
     * @return application info
     */
    Application createAppByProfileId(String profileId, MultipartFile iconFile);
}

