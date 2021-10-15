/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.exception;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Signals that an attempt to open the file denoted by a specified pathname
 * has failed.
 *
 * <p> This exception will be thrown by the {@link FileInputStream}, {@link
 * FileOutputStream}, and {@link RandomAccessFile} constructors when a file
 * with the specified pathname does not exist.  It will also be thrown by these
 * constructors if the file does exist but for some reason is inaccessible, for
 * example when an attempt is made to open a read-only file for writing.
 *
 * @author  unascribed
 * @since   JDK1.0
 */

public class FileFoundFailException extends RuntimeException {

    private static final long serialVersionUID = -897856973823710492L;

    private ErrorMessage errMsg;

    public FileFoundFailException(String message) {
        super(message);
    }

    /**
     * Constructor to create FileOperateException with retCode and params.
     *
     * @param ret retCode
     */
    public FileFoundFailException(String msg, int ret) {
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
     * Constructor to create FileNotFoundException with retCode and params.
     *
     * @param ret retCode
     * @param args params of error message
     */
    public FileFoundFailException(String msg, int ret, Object... args) {
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

