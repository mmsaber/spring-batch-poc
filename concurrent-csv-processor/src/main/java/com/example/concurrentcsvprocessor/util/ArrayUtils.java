package com.example.concurrentcsvprocessor.util;

import java.util.Arrays;

public final class ArrayUtils {


    public static <T> T[] concatenate(T[] arr1, T[] arr2) {
        T[] concatenated = Arrays.copyOf(arr1, arr1.length + arr2.length);
        System.arraycopy(arr2, 0, concatenated, arr1.length, arr2.length);
        return concatenated;
    }
}
