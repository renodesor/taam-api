package com.renodesor.taam.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UtilsTest {

    @Test
    void testMergeTwoObjects() {
        Map map1 = new HashMap<>();
        map1.put("val1", 1);
        map1.put("val2", 2);
        map1.put("val3", 3);
        map1.put("val4", 4);
        Map map2 = new HashMap<>();
        map2.put("val1", 11);
        map2.put("val2", 21);
        map2.put("val5", 51);
        map2.put("val6", 61);

        map1.putAll(map2);
        System.out.println(map1);
    }
}
