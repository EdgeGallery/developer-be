package org.edgegallery.developer.util;

public class IpCalculateUtil {

    private IpCalculateUtil() {
        throw new IllegalStateException("IpCalculateUtil class");
    }

    /**
     * getNetMask.
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
     * getStartIp.
     *
     * @param segment segment
     * @param range range
     * @return
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
                    startIp.append(ipArray[i] + range % 250 + 3);
                } else {
                    startIp.append(ipArray[i] + ".");
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return startIp.toString();
    }

    /**
     * getEndIp.
     *
     * @param segment segment
     * @return
     */
    public static String getEndIp(String segment) {
        StringBuilder endIp = new StringBuilder();
        String startIp = getStartIp(segment, 0);
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String maskIndex = arr[1];
        int hostNumber = 0;
        int[] startIpArray = new int[4];
        try {
            hostNumber = 1 << 32 - (Integer.parseInt(maskIndex));
            for (int i = 0; i < 4; i++) {
                startIpArray[i] = Integer.parseInt(startIp.split("\\.")[i]);
                if (i == 3) {
                    startIpArray[i] = startIpArray[i] - 1;
                    break;
                }
            }
            startIpArray[3] = startIpArray[3] + (hostNumber - 1);
        } catch (NumberFormatException e) {
            return null;
        }

        if (startIpArray[3] > 255) {
            int k = startIpArray[3] / 256;
            startIpArray[3] = startIpArray[3] % 256;
            startIpArray[2] = startIpArray[2] + k;
        }
        if (startIpArray[2] > 255) {
            int j = startIpArray[2] / 256;
            startIpArray[2] = startIpArray[2] % 256;
            startIpArray[1] = startIpArray[1] + j;
            if (startIpArray[1] > 255) {
                int k = startIpArray[1] / 256;
                startIpArray[1] = startIpArray[1] % 256;
                startIpArray[0] = startIpArray[0] + k;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                startIpArray[i] = startIpArray[i] - 1;
            }
            if ("".equals(endIp.toString()) || endIp.length() == 0) {
                endIp.append(startIpArray[i]);
            } else {
                endIp.append("." + startIpArray[i]);
            }
        }
        return endIp.toString();
    }

}
