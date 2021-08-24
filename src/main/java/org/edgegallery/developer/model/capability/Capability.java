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

package org.edgegallery.developer.model.capability;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Capability {
    private String id;

    private String name;

    private String nameEn;

    private String version;

    private String description;

    private String descriptionEn;

    private String provider;

    private String apiFileId;

    private String guideFileId;

    private String guideFileIdEn;

    private long uploadTime;

    private int port;

    private String host;

    private String protocol;

    private String appId;

    private String packageId;

    private String userId;

    private int selectCount;

    private String iconFileId;

    private String author;

    private String experienceUrl;

    private CapabilityGroup group;

    public String getGroupId() {
        return group == null ? null : group.getId();
    }

    public void setGroupId(String groupId) {
        if (group == null) {
            group = new CapabilityGroup();
        }
        group.setId(groupId);
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder().append(id);
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CapabilityGroup)) {
            return false;
        }
        CapabilityGroup another = (CapabilityGroup) obj;
        EqualsBuilder builder = new EqualsBuilder().append(id, another.getId());
        return builder.build();
    }

    @Override
    public String toString() {
        return "Capability [id=" + id + ", name=" + name + "]";
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIconFileId() {
        return iconFileId;
    }

    public void setIconFileId(String iconFileId) {
        this.iconFileId = iconFileId;
    }

    public int getSelectCount() {
        return selectCount;
    }

    public void setSelectCount(int selectCount) {
        this.selectCount = selectCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiFileId() {
        return apiFileId;
    }

    public void setApiFileId(String apiFileId) {
        this.apiFileId = apiFileId;
    }

    public String getGuideFileId() {
        return guideFileId;
    }

    public void setGuideFileId(String guideFileId) {
        this.guideFileId = guideFileId;
    }

    public String getGuideFileIdEn() {
        return guideFileIdEn;
    }

    public void setGuideFileIdEn(String guideFileIdEn) {
        this.guideFileIdEn = guideFileIdEn;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public CapabilityGroup getGroup() {
        return group;
    }

    public void setGroup(CapabilityGroup group) {
        this.group = group;
    }

    public String getExperienceUrl() {
        return experienceUrl;
    }

    public void setExperienceUrl(String experienceUrl) {
        this.experienceUrl = experienceUrl;
    }
}
