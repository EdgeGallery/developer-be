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


import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.domain.shared.Entity;

public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 5224743617068936039L;

    public EntityNotFoundException(String message) {
        super(message);
    }

    public <T extends Entity> EntityNotFoundException(Class<T> entityClass, String id) {
        super("cannot find the " + entityClass.getSimpleName().toLowerCase() + " with id " + id);
    }

    private ErrorMessage errMsg;

    /**
     * Constructor to create EntityNotFoundException with retCode and params.
     *
     * @param ret retCode
     */
    public EntityNotFoundException(String msg, int ret) {
        super(msg);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
        errMsg = errorMessage;
    }

    /**
     * get error message.
     *
     */
    public ErrorMessage getErrMsg() {
        return errMsg;
    }

    /**
     * Constructor to create EntityNotFoundException with retCode and params.
     *
     * @param ret retCode
     * @param args params of error message
     */
    public EntityNotFoundException(String msg, int ret, Object... args) {
        super(msg);
        List<String> params = new ArrayList<>();
        int length = args == null ? 0 : args.length;
        for (int i = 0; i < length; i++) {
            params.add(args[i].toString());
        }
        ErrorMessage errorMessage = new ErrorMessage(ret, params);
        errMsg = errorMessage;
    }
}
