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

package org.edgegallery.developer.model.reverseproxy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSshData {
    //operating
    private String operate;

    private String host;

    //The port number defaults to22
    private Integer port = 22;

    private String username;

    private String password;

    private String command = "";

    private String userId;

    private String applicationId;

}
