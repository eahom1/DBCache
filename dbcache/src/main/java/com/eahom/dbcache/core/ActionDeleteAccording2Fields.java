package com.eahom.dbcache.core;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.eahom.dbcache.annotation.Column;
import com.eahom.dbcache.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by eahom on 17/6/3.
 */

final class ActionDeleteAccording2Fields<D> {

    private final String TAG = "ActionDeleteAccording2Fields";

    private final SQLiteDatabase db;
    private final List<D> data;
    private final String[] fieldNames;


    ActionDeleteAccording2Fields(SQLiteDatabase db, List<D> data, String[] fieldNames) {
        this.db = db;
        this.data = data;
        this.fieldNames = fieldNames;
    }


    protected int execute() throws NoSuchFieldException, IllegalAccessException {
        if (data == null || data.size() == 0)
            return 0;

        if (fieldNames == null || fieldNames.length == 0)
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The data you want to delete without indicating the fields that according to, please check!");

        Class<D> dClass = (Class<D>) data.get(0).getClass();
        if (!SqlExecutor.isTableExisted(db, dClass)) {
            boolean create = SqlExecutor.createTable(db, dClass);
            if (!create)
                throw new IllegalArgumentException(SqlActionResult.ERROR + "The table which you want to delete data was not exists and also created failed!");
        }
        Table table = dClass.getAnnotation(Table.class);
        if (table == null)
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The class of the data which you want to delete has not been added 'Table' annotation!");

        final String TABLE = SqlExecutor.tableName(dClass, table);
        if (TextUtils.isEmpty(TABLE))
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The class of the data which you want to delete has an empty table name of 'Table' annotation!");

        StringBuilder sql = new StringBuilder("DELETE FROM " + TABLE + " WHERE 1=1 ");

        for (String fieldName : fieldNames) {
            Field field = SqlExecutor.getDeclaredField(dClass, fieldName);
            Column column = field.getAnnotation(Column.class);
            if (column == null)
                throw new IllegalArgumentException(SqlActionResult.ERROR + "The field which you want to using it to delete data without 'Column' annotation!");
            sql.append("AND ").append(SqlExecutor.columnName(column, field)).append("=? ");
        }
        sql.deleteCharAt(sql.length() - 1);

        List<BindValue> bindValueList = new ArrayList<>();
        for (Iterator<D> dataIt = data.iterator(); dataIt.hasNext(); ) {
            D d = dataIt.next();
            BindValue bindValue = new BindValue();
            int i = 1;
            for (String fieldName : fieldNames) {
                Field field = SqlExecutor.getDeclaredField(dClass, fieldName);
                Column column = field.getAnnotation(Column.class);
                bindValue.put(i, SqlExecutor.columnValue(TABLE, column, field, d));
                i++;
            }
            bindValueList.add(bindValue);
        }
        return SqlExecutor.delete(db, sql.toString(), bindValueList);
    }
}
