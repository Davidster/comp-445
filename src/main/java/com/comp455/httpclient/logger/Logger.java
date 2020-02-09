package com.comp455.httpclient.logger;

import java.util.List;

public class Logger {
    public static LogLevel logLevel = LogLevel.INFO;

    private static List<LogLevel> logLevelOrdering = List.of(
            LogLevel.VERBOSE,
            LogLevel.INFO,
            LogLevel.ERROR);

    public static void log(String msg) {
        log(msg, LogLevel.INFO);
    }

    public static void log(String msg, LogLevel minLogLevel) {
        if(logLevelOrdering.indexOf(minLogLevel) >= logLevelOrdering.indexOf(logLevel)) {
            System.out.println(msg);
        }
    }

    public static void logError(String msg) {
        System.err.println(msg);
    }
}
