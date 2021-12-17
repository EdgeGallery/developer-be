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

public class IpCalculateUtil {

    private IpCalculateUtil() {
        throw new IllegalStateException("IpCalculateUtil class");
    }

    /**
     * get mask by mask index
     *
     * @param maskIndex maskIndex
     * @return mask
     */
    public static String getNetMask(String maskIndex) {
        StringBuilder mask = new StringBuilder();
        Integer inetMask = 0;
        try {
            inetMask = Integer.parseInt(maskIndex);
        } catch (NumberFormatException e) {
            return null;
        }
        if (inetMask > 32) {
            return null;
        }
        int num1 = inetMask / 8;
        int num2 = inetMask % 8;
        int[] array = new int[4];
        for (int i = 0; i < num1; i++) {
            array[i] = 255;
        }
        for (int i = num1; i < 4; i++) {
            array[i] = 0;
        }
        for (int i = 0; i < num2; num2--) {
            array[num1] += 1 << 8 - num2;
        }
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                mask.append(array[i]);
            } else {
                mask.append(array[i] + ".");
            }
        }
        return mask.toString();
    }

    /**
     * get start ip by ip range:x.x.x.x/x
     *
     * @param segment segment
     * @return startIp
     */
    public static String getStartIp(String segment, int range) {
        StringBuilder startIp = new StringBuilder();
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String ip = arr[0];
        String maskIndex = arr[1];
        String mask = IpCalculateUtil.getNetMask(maskIndex);
        if (4 != ip.split("\\.").length || mask == null) {
            return null;
        }
        int[] ipArray = new int[4];
        int[] netMaskArray = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
                netMaskArray[i] = Integer.parseInt(mask.split("\\.")[i]);
                if (ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0) {
                    return null;
                }
                ipArray[i] = ipArray[i] & netMaskArray[i];
                if (i == 3) {
                    startIp.append(ipArray[i] + range % 250 + 1);
                } else {
                    startIp.append(ipArray[i] + ".");
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return startIp.toString();
    }
}
