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

package org.edgegallery.developer.util.helmcharts.k8sobject;

import com.google.gson.Gson;
import io.kubernetes.client.common.KubernetesObject;

public enum EnumKubernetesObject {
    Pod(V1PodExt.class),
    Deployment(V1DeploymentExt.class),
    Unknown(UnknownObject.class);

    private final Class clzExt;

    EnumKubernetesObject(Class clzExt) {
        this.clzExt = clzExt;
    }

    public static EnumKubernetesObject valueOf(String kind, EnumKubernetesObject defaultValue) {
        try {
            return EnumKubernetesObject.valueOf(kind);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static IContainerImage of(Object obj) {
        if (obj instanceof KubernetesObject) {
            KubernetesObject kubernetesObject = (KubernetesObject) obj;
            String kind = kubernetesObject.getKind();
            EnumKubernetesObject enumKubernetesObject = EnumKubernetesObject
                .valueOf(kind, EnumKubernetesObject.Unknown);
            Gson gson = new Gson();
            switch (enumKubernetesObject) {
                case Pod:
                case Deployment:
                    String json = gson.toJson(obj);
                    return (IContainerImage) gson.fromJson(json, enumKubernetesObject.clzExt);
                default:
                    return new UnknownObject();
            }
        }
        return new UnknownObject();
    }
}
