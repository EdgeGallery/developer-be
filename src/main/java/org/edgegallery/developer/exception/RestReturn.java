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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

/**
 * Interface Return Contractor.
 */
@Data
@Builder
public class RestReturn {

    // timestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Date timestamp = new Date();

    // Error code.
    private int code;

    // Description of error.
    private String error;

    // Detail of error.
    private String message;

    // request path
    private String path;

    // error code
    private int retCode;

    // error message params
    private List<String> params;

    private Object data;

    /**
     * getTimestamp.
     */
    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RestReturn)) {
            return false;
        }
        RestReturn that = (RestReturn) o;
        return getCode() == that.getCode() && Objects.equals(getTimestamp(), that.getTimestamp())
            && Objects.equals(getError(), that.getError()) && Objects.equals(getMessage(), that.getMessage())
            && Objects.equals(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTimestamp(), getCode(), getError(), getMessage(), getPath());
    }
}