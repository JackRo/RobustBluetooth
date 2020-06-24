package com.easy.robust.bluetooth;

import android.util.Log;

/**
 * Log Util
 *
 * @author zhuochangjing
 * @since 2018/9/13
 */
public final class LogUtil {

    public static void v(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(cls.getSimpleName(), msg);
        }
    }

    public static void d(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(cls.getSimpleName(), msg);
        }
    }

    public static void i(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(cls.getSimpleName(), msg);
        }
    }

    public static void w(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(cls.getSimpleName(), msg);
        }
    }

    public static void e(Class<?> cls, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(cls.getSimpleName(), msg);
        }
    }
}
