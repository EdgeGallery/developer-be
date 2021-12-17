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

package org.edgegallery.developer.filter.security;

import org.edgegallery.developer.model.common.User;


public final class AccessUserUtil {

    private AccessUserUtil() {
        throw new IllegalStateException("AccessUserUtil class");
    }

    private static final ThreadLocal<User> user = new ThreadLocal<>();

    public static void setUser(String userId, String userName) {
        user.set(new User(userId, userName));
    }

    public static void setUser(String userId, String userName, String userAuth) {
        user.set(new User(userId, userName, userAuth));
    }

    public static void setUser(String userId, String userName, String userAuth, String token) {
        user.set(new User(userId, userName, userAuth, token));
    }

    public static User getUser() {
        return user.get();
    }

    public static String getUserId() {
        return user.get() == null ? null : user.get().getUserId();
    }

    public static String getToken() {
        return user.get() == null ? null : user.get().getToken();
    }

    public static void unload() {
        user.remove();
    }
}
