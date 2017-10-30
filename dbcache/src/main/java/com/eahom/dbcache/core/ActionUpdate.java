package com.eahom.dbcache.core;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eahom on 17/6/3.
 */

final class ActionUpdate {

    private final SQLiteDatabase db;
    private final String sql;
    private final Object[] params;


    ActionUpdate(SQLiteDatabase db, String sql, Object[] params) {
        this.db = db;
        this.sql = sql;
        this.params = params;
    }

    protected int execute() {
        if (params == null || params.length == 0)
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The sql you want to update without binding parameters, please check!");

        BindValue bindValue = new BindValue();
        for (int i = 0; i < params.length; i++)
            bindValue.put((i + 1), params[i]);

        List<BindValue> bindValueList = new ArrayList<>();
        bindValueList.add(bindValue);
        return SqlExecutor.update(db, sql, bindValueList);
    }
}
