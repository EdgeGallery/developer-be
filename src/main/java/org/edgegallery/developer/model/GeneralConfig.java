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
package org.edgegallery.developer.model;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
public class GeneralConfig {

        private String projectName;

        private String inputSpec; //--input-spec

        private String apiPackage;

        private String invokerPackage;

        private String  modelPackage;

        private String  output;

        /**
         *Different configuration files can be selected according to different languages
         */
        private String config;

        /**
         * java config
         */
        private String artifactId;
        /**
         *Specify the version of the artifact of pom.xml;
         */
        private String artifactVersion ;
        /**
         *Specify the value of groupId in pom.xml;
         */
        private String groupId;







}
