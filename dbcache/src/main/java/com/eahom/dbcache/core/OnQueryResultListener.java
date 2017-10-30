package com.eahom.dbcache.core;

/**
 * Created by ekang001 on 2017/7/5.
 */

public interface OnQueryResultListener<D> {

    D onQueryResult(ResultSet resultSet);
}
