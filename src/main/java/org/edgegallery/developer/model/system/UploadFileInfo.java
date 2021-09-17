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

package org.edgegallery.developer.model.system;

import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;

@Getter
@Setter
public class UploadFileInfo {
    private boolean succeeded = false;

    private int respStatusCode;

    private String errorType = null;

    private Integer systemId;

    private String fileName;

    private String fileMd5;

    private String fileFormat;

    private Integer fileSize;

    private String status;

    private String systemPath;

    /**
     * constructor.
     *
     * @param respStatusCode resp status code
     * @param errorType error type
     */
    public UploadFileInfo(int respStatusCode, String errorType) {
        this.succeeded = false;
        setRespStatusCode(respStatusCode);
        setErrorType(errorType);
    }

    /**
     * constructor.
     *
     * @param fileName File Name
     * @param fileMd5 File Md5
     * @param fileFormat File Format
     */
    public UploadFileInfo(String fileName, String fileMd5, String fileFormat) {
        this.succeeded = true;
        setFileName(fileName);
        setFileMd5(fileMd5);
        setFileFormat(fileFormat);
    }

    /**
     * constructor.
     *
     * @param fileName File Name
     * @param fileMd5 File Md5
     * @param fileFormat File Format
     * @param fileSize File size
     */
    public UploadFileInfo(String fileName, String fileMd5, String fileFormat, int fileSize) {
        this.succeeded = true;
        setFileName(fileName);
        setFileMd5(fileMd5);
        setFileFormat(fileFormat);
        setFileSize(fileSize);
    }

    /**
     * assign value.
     *
     * @param systemId system id
     * @param status status
     * @param systemPath system path
     */
    public void assign(int systemId, EnumSystemImageStatus status, String systemPath) {
        setSystemId(systemId);
        setStatus(status.toString());
        setSystemPath(systemPath);
    }
}
