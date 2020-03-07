package com.comp445.common.http;

import java.util.regex.Pattern;

public class Util {
    public static final Pattern emptyLine = Pattern.compile("^\\s*$");

    public static boolean isEmptyLine(String s) {
        return emptyLine.matcher(s).matches();
    }
}

