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

package org.edgegallery.developer.exception;

import org.edgegallery.developer.domain.shared.Entity;

public class EntityNotFoundException extends CommonException {
    public EntityNotFoundException(String message, int ret) {
        super(message, ret);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, int ret, Object... args) {
        super(message, ret, args);

    }

    public <T extends Entity> EntityNotFoundException(Class<T> entityClass, String id) {
        super("cannot find the " + entityClass.getSimpleName().toLowerCase() + " with id " + id);
    }

}
