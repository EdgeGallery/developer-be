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

package org.edgegallery.developer.service.apppackage.converter;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import org.edgegallery.developer.model.apppackage.appd.VNFNodeProperty;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class CustomRepresenter extends Representer {

    public CustomRepresenter() {
        super();
        PropertyUtils propertyUtils = new PropertyUtils() {
            @Override
            protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
                Set<Property> properties = super.createPropertySet(type, bAccess);
                Set<Property> linkedProperties = new LinkedHashSet<>();
                Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    for (Property propertyTmp : properties) {
                        if (propertyTmp.getName().equals(field.getName())) {
                            linkedProperties.add(propertyTmp);
                            break;
                        }
                    }
                }
                return linkedProperties;
            }
        };
        setPropertyUtils(propertyUtils);

        this.addClassTag(VNFNodeProperty.class, Tag.MAP);
    }

    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
        Tag customTag) {
        if (null == propertyValue) {
            return null;
        } else {
            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }
    }
}