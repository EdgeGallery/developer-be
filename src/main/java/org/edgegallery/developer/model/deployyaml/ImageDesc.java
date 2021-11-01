/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.model.deployyaml;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.resource.vm.VMImage;

@Getter
@Setter
public class ImageDesc {

    private String id;

    private String name;

    private String version;

    private String checksum;

    private String containerFormat = "bare";

    private String diskFormat;

    private int minDisk;

    private int minRam = 6;

    private String architecture;

    private long size;

    private String swImage;

    @SerializedName("hw_scsi_model")
    private String hwScsiModel = "virtio-scsi";

    @SerializedName("hw_disk_bus")
    private String hwDiskBus = "scsi";

    private String operatingSystem;

    private String supportedVirtualisationEnvironment = "linux";

    public ImageDesc() {

    }

    /**
     * constructor.
     *
     * @param vmImage vmImage
     */
    public ImageDesc(VMImage vmImage) {
        String url = vmImage.getDownLoadUrl();
        setSize(vmImage.getImageSize());
        setId(String.valueOf(vmImage.getId()));
        setName(vmImage.getName());
        setVersion(vmImage.getOsVersion());
        setChecksum(vmImage.getFileMd5());
        setDiskFormat(vmImage.getImageFormat());
        setMinDisk(vmImage.getSystemDiskSize());
        setSwImage(url);
        setOperatingSystem(vmImage.getOsType());
    }

}
