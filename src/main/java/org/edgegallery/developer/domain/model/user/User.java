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

package org.edgegallery.developer.domain.model.user;

import lombok.Getter;

@Getter
public class User {

    private final String userId;

    private final String userName;

    private final String userAuth;

    private final String token;

    /**
     * User.
     *
     * @param userId userId
     * @param userName userName
     */
    public User(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.userAuth = "";
        this.token = "";
    }

    /**
     * User.
     *
     * @param userId userId
     * @param userName userName
     * @param userAuth userAuth
     */
    public User(String userId, String userName, String userAuth) {
        this.userId = userId;
        this.userName = userName;
        this.userAuth = userAuth;
        this.token = "";
    }

    /**
     * User.
     *
     * @param userId userId
     * @param userName userName
     * @param userAuth userAuth
     * @param token token
     */
    public User(String userId, String userName, String userAuth, String token) {
        this.userId = userId;
        this.userName = userName;
        this.userAuth = userAuth;
        this.token = token;
    }
}
