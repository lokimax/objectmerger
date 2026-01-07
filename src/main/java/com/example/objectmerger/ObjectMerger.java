package com.example.objectmerger;

public class ObjectMerger {
    /**
     * Returns the first non-null value among the two arguments, or null if both are null.
     */
    public static <T> T coalesce(T a, T b) {
        return a != null ? a : b;
    }
}
