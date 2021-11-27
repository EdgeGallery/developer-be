/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

public class ApplicationProjectCapability {
	private String projectId;
	private String capabilityId;

	public static ApplicationProjectCapability newInstance(String projectId,String capabilityId) {
		ApplicationProjectCapability instance = new ApplicationProjectCapability();
		instance.projectId = projectId;
		instance.capabilityId = capabilityId;
		return instance;
	}
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getCapabilityId() {
		return capabilityId;
	}

	public void setCapabilityId(String capabilityId) {
		this.capabilityId = capabilityId;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(projectId).append(capabilityId).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ApplicationProjectCapability)) {
			return false;
		}
		ApplicationProjectCapability other = (ApplicationProjectCapability) obj;
		return new EqualsBuilder().append(this.projectId, other.projectId).append(this.capabilityId, other.capabilityId)
				.isEquals();
	}

	@Override
	public String toString() {
		return projectId + ',' + capabilityId;
	}
}
