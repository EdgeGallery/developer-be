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

package org.edgegallery.developer.common.enums;

import lombok.Getter;

@Getter
public enum EnumAppType {
    TYPE_API("API", "API"),
    TYPE_GAME("Game", "游戏"),
    TYPE_VIDEO_SURVEILLANCE("Video Surveillance", "视频监控"),
    TYPE_AR_VR("AR/VR", "AR/VR"),
    TYPE_SDK("SDK", "SDK"),
    TYPE_MEP("MEP", "MEP"),
    TYPE_VIDEO_APPLICATION("Video Application", "视频应用"),
    TYPE_BLOCKCHAIN("Blockchain", "区块链"),
    TYPE_SMART_DEVICE("Smart Device", "智能设备"),
    TYPE_INTERNET_OF_THINGS("Internet of Things", "物联网"),
    TYPE_BIG_DATA("Big Data", "大数据"),
    TYPE_SAFETY("Safety", "安全");

    private String englishValue;

    private String chineseValue;

    EnumAppType(String englishValue, String chineseValue) {
        this.englishValue = englishValue;
        this.chineseValue = chineseValue;
    }
}
