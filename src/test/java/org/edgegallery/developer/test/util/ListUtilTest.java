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

package org.edgegallery.developer.test.util;

import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.util.ListUtil;
import org.junit.Assert;
import org.junit.Test;

public class ListUtilTest {

    @Test
    public void testTwoListIsEquals1() {
        Assert.assertTrue(ListUtil.isEquals(null, null));
    }

    @Test
    public void testTwoListIsEquals2() {
        List list = new ArrayList();
        Assert.assertFalse(ListUtil.isEquals(null, list));
    }

    @Test
    public void testTwoListIsEquals3() {
        List list = new ArrayList();
        Assert.assertFalse(ListUtil.isEquals(list, null));
    }

    @Test
    public void testTwoListIsEquals4() {
        List list1 = new ArrayList<>();
        list1.add("a");
        List list2 = new ArrayList<>();
        list2.add("b");
        list2.add("c");
        Assert.assertFalse(ListUtil.isEquals(list1, list2));
    }

    @Test
    public void testTwoListIsEquals5() {
        List list1 = new ArrayList<>();
        list1.add("a");
        list1.add("b");
        List list2 = new ArrayList<>();
        list2.add("a");
        list2.add("b");
        Assert.assertTrue(ListUtil.isEquals(list1, list2));
    }
}
