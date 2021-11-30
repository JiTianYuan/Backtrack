package com.jty.backtrack.data_parser;

public class Util {
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(String s){
        return !isEmpty(s);
    }
}
