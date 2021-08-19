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

public class CapabilityGroup {
	private String id;
	private String name;
	private String description;
	private String nameEn;
	private String descriptionEn;
	private String type;
	private String iconFileId;
	private String author;
	private long createTime;
	private long updateTime;
	private CapabilityGroup parent;
	
	public String getParentId() {
		return parent == null ? null : parent.getId();
	}

	public void setParentId(String parentId) {
		if (parent == null) {
			parent = new CapabilityGroup();
		}
		parent.setId(parentId);
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
		return "CapabilityGroup [id=" + id + ", name=" + name + ", type=" + type + "]";
	}

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	public String getDescriptionEn() {
		return descriptionEn;
	}

	public void setDescriptionEn(String descriptionEn) {
		this.descriptionEn = descriptionEn;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIconFileId() {
		return iconFileId;
	}

	public void setIconFileId(String iconFileId) {
		this.iconFileId = iconFileId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public CapabilityGroup getParent() {
		return parent;
	}

	public void setParent(CapabilityGroup parent) {
		this.parent = parent;
	}
}
