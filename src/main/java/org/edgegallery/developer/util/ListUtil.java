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
        } else if (list1 == null || list2 == null) {
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
