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

package org.edgegallery.developer.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class Chunk {
    private Long id;

    //当前文件块，从1开始
    private Integer chunkNumber;

    //分块大小
    private Long chunkSize;

    //当前分块大小
    private Long currentChunkSize;

    //总大小
    private Long totalSize;

    //文件标识
    private String identifier;

    //文件名
    private String filename;

    //相对路径
    private String relativePath;

    //总块数
    private Integer totalChunks;

    //文件类型
    private String type;

    private MultipartFile file;
}
