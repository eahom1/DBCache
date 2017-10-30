package com.eahom.dbcache.core;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by eahom on 17/5/26.
 */

public class DBCache {

    public static void initialize(Context context, String dbName, int dbVersion) {
        DBCache.initialize(context, dbName, dbVersion, null);
    }

    public static void initialize(Context context, String dbName, int dbVersion, ArrayList<Class> beanClassList) {
        SqliteSession.initialize(context, dbName, dbVersion, beanClassList);
    }

    /**
     * This listener was valid only once.
     * @param listener
     */
    public static void setOnSqliteCreateListener(OnSqliteCreateListener listener) {
        SqliteSession.instance().setOnSqliteCreateListener(listener);
    }

    /**
     * This listener was valid only once.
     * @param listener
     */
    public static void setOnSqliteUpgradeListener(OnSqliteUpgradeListener listener) {
        SqliteSession.instance().setOnSqliteUpgradeListener(listener);
    }

}
