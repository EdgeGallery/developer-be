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

package org.edgegallery.developer.exception;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommonException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1646444285623052490L;

    private ErrorMessage errMsg;

    /**
     * Constructor to create DeveloperException with message.
     *
     * @param message exception message
     */
    public CommonException(String message) {
        super(message);
    }

    /**
     * Constructor to create DeveloperException with message.
     *
     * @param message exception message
     * @param ret retCode
     */
    public CommonException(String message, int ret) {
        super(message);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
        errMsg = errorMessage;
    }

    /**
     * Constructor to create DeveloperException with retCode and params.
     *
     * @param ret retCode
     * @param args params of error message
     */
    public CommonException(String message, int ret, Object... args) {
        super(message);
        int length = args == null ? 0 : args.length;
        List<String> params = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            params.add(args[i].toString());
        }
        ErrorMessage errorMessage = new ErrorMessage(ret, params);
        errMsg = errorMessage;
    }

    /**
     * get error message.
     */
    public ErrorMessage getErrMsg() {
        return errMsg;
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
    }
}
