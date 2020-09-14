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

package org.edgegallery.developer.response;

import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FormatRespDto {

    private Response.Status enumStatus;

    private ErrorRespDto errorRespDto;

    public FormatRespDto(Response.Status status, String detail) {
        this.enumStatus = status;
        this.errorRespDto = new ErrorRespDto(status.getStatusCode(), status.toString(), detail);
    }

    public FormatRespDto(Response.Status status) {
        this.enumStatus = status;
        this.errorRespDto = new ErrorRespDto(status.getStatusCode(), status.toString(), null);
    }

    @Override
    public String toString() {
        return "FormatRespDto[" + "status=" + enumStatus + ", ErrorRespDto=" + errorRespDto + ']';
    }

}
