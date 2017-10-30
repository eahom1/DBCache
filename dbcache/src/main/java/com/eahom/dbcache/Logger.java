package com.eahom.dbcache;

import android.util.Log;

/**
 * Created by eahom on 17/4/12.
 */

public class Logger {

    private static final boolean DEBUG = true;

    public static final void print(final String tag, final String logText) {
        if (DEBUG)
            Log.e(tag, logText);
    }

    public static final void print(final String tag, final Throwable e) {
        print(tag, e.getMessage());
    }

}
