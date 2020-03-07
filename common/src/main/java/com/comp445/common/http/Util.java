package com.comp445.common.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class Util {
    public static final Pattern emptyLine = Pattern.compile("^\\s*$");

    public static final int MAX_HEADER_COUNT = 1000;
    public static final int BODY_READ_CHUNK_SIZE = 4000;
    public static final int MAX_BODY_SIZE = 100000000; // 100 MB

    public static boolean isEmptyLine(String s) {
        return emptyLine.matcher(s).matches();
    }

    public static String readLine(BufferedInputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        while(true) {
            int data = input.read();
            if(data == -1) {
                continue;
            }
            char c = (char) data;
            if(c == '\n') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}

