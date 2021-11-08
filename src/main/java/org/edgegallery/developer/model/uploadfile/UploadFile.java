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

package org.edgegallery.developer.model.uploadfile;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.common.Consts;

@Getter
@Setter
@ToString
public class UploadFile {

    private String fileId;

    private String fileName;

    private String url;

    private String userId;

    private boolean isTemp;

    private Date uploadDate;

    private String filePath;

    public UploadFile() {

    }

    public UploadFile(String fileName, String fileId) {
        this.fileId = fileId;
        this.fileName = fileName;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
        this.url = Consts.DOWNLOAD_FILE_URL_V1 + fileId;
    }

    /**
     * getUploadDate.
     *
     * @return
     */
    public Date getUploadDate() {
        if (this.uploadDate != null) {
            return new Date(this.uploadDate.getTime());
        } else {
            return null;
        }
    }

    /**
     * setUploadDate.
     */
    public void setUploadDate(Date uploadDate) {
        if (uploadDate != null) {
            this.uploadDate = (Date) uploadDate.clone();
        } else {
            this.uploadDate = null;
        }
    }
}
