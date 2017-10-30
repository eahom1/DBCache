package com.eahom.dbcache.core;

/**
 * Created by eahom on 17/6/6.
 */

public interface OnSqliteUpgradeListener {

    void beforeDropOldTables(int oldVersion, int newVersion);
    void afterDropOldTablesAndBeforeCreateTableWithBeanList(int oldVersion, int newVersion);
    void afterCreateTableWithBeanList(int oldVersion, int newVersion);
}
