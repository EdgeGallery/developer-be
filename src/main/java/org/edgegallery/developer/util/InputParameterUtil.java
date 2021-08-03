package org.edgegallery.developer.util;

import java.util.HashMap;
import java.util.Map;

public class InputParameterUtil {

    private InputParameterUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * getParams.
     *
     * @param str str
     * @return
     */
    public static Map<String, String> getParams(String str) {
        String[] arr = str.split(";");
        Map<String, String> params = new HashMap<>();
        for (String temp : arr) {
            String[] keyValue = temp.trim().split("=");
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0];
            String value = keyValue[1];
            params.put(key, value);
        }
        return params;

    }


}
