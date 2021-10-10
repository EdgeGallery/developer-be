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

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@Setter
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

    /**
     * setGroupId.
     */
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

}
