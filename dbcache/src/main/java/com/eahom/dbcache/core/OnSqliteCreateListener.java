package com.eahom.dbcache.core;

/**
 * Created by eahom on 17/6/6.
 */

public interface OnSqliteCreateListener {

    void beforeCreateTableWithBeanList();
    void afterCreateTableWithBeanList();

}
