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

package org.edgegallery.developer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtil {

    private ListUtil() {
        throw new IllegalStateException("ListUtil class");
    }

    /**
     * judge two list is equal.
     *
     * @param list1 one list
     * @param list2 another list
     * @param <T> t
     * @return true or false
     */
    public static <T extends Comparable<T>> boolean isEquals(List<T> list1, List<T> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }
        //Only one of them is null
        else if (list1 == null || list2 == null) {
            return false;
        } else if (list1.size() != list2.size()) {
            return false;
        }

        //copying to avoid rearranging original lists
        list1 = new ArrayList<>(list1);
        list2 = new ArrayList<>(list2);

        Collections.sort(list1);
        Collections.sort(list2);

        return list1.equals(list2);
    }
}
