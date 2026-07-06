package com.govn.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for logging to simplify logger usage across the framework.
 */
public class LogUtils {

    /**
     * Gets the logger for the calling class to maintain accurate log origins.
     */
    private static Logger getLogger() {
        // [0] Thread.getStackTrace
        // [1] LogUtils.getLogger
        // [2] LogUtils.info/debug/error
        // [3] Caller class
        String callerClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        return LogManager.getLogger(callerClassName);
    }

    public static void info(String message) {
        getLogger().info(message);
    }

    public static void info(String message, Object... params) {
        getLogger().info(message, params);
    }

    public static void debug(String message) {
        getLogger().debug(message);
    }

    public static void debug(String message, Object... params) {
        getLogger().debug(message, params);
    }

    public static void warn(String message) {
        getLogger().warn(message);
    }

    public static void warn(String message, Object... params) {
        getLogger().warn(message, params);
    }

    public static void error(String message) {
        getLogger().error(message);
    }

    public static void error(String message, Throwable t) {
        getLogger().error(message, t);
    }

    public static void error(String message, Object... params) {
        getLogger().error(message, params);
    }
}
