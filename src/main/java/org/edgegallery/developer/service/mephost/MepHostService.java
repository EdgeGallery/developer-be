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

package org.edgegallery.developer.service.mephost;

import com.spencerwi.either.Either;
import java.util.List;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.mephost.MepHostLog;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.springframework.web.multipart.MultipartFile;

public interface MepHostService {

    /**
     * get all host.
     *
     * @param name host name
     * @param vimType vimType
     * @param architecture architecture
     * @param limit limit
     * @param offset offset
     * @return
     */
    Page<MepHost> getAllHosts(String name, String vimType, String architecture, int limit, int offset);

    /**
     * create host.
     *
     * @param host request body
     * @param token access token
     * @return
     */
    Either<FormatRespDto, Boolean> createHost(MepHost host, String token);

    /**
     * delete host by host id.
     *
     * @param hostId host id
     * @return
     */
    Either<FormatRespDto, Boolean> deleteHost(String hostId);

    /**
     * update host by host id and token.
     *
     * @param hostId host id
     * @param host request body
     * @param token access token
     * @return
     */
    Either<FormatRespDto, Boolean> updateHost(String hostId, MepHost host, String token);

    /**
     * get one host by host id.
     *
     * @param hostId host id
     * @return
     */
    Either<FormatRespDto, MepHost> getHost(String hostId);

    /**
     * get one host log by host id.
     *
     * @param hostId host id
     * @return
     */
    Either<FormatRespDto, List<MepHostLog>> getHostLogByHostId(String hostId);

    /**
     * upload host config file.
     *
     * @param uploadFile config file
     * @return
     */
    Either<FormatRespDto, UploadedFile> uploadConfigFile(MultipartFile uploadFile);

}
