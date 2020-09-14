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

package org.edgegallery.developer.domain.service;

import java.io.IOException;
import java.io.InputStream;
import org.edgegallery.developer.domain.shared.AFile;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String saveTo(MultipartFile file, FileChecker fileChecker) throws IOException;

    InputStream get(AFile afile) throws IOException;

    String get(String fileAddress, String filePath) throws IOException;

    void delete(AFile afile);
}
