package com.eahom.dbcache.core;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.eahom.dbcache.Logger;
import com.eahom.dbcache.annotation.Column;
import com.eahom.dbcache.annotation.FieldType;
import com.eahom.dbcache.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.eahom.dbcache.core.SqlExecutor.columnName;
import static com.eahom.dbcache.core.SqlExecutor.columnType;


/**
 * Created by eahom on 17/6/3.
 */

final class ActionInsertOne<D> {

    private static final String TAG = "ActionInsertOne";

    private final SQLiteDatabase db;
    private final D data;

    ActionInsertOne(SQLiteDatabase db, D data) {
        this.db = db;
        this.data = data;
    }

    protected long execute() throws IllegalAccessException {
        if (data == null)
            return -1;
        Class<D> dClass = (Class<D>) data.getClass();
        if (!SqlExecutor.isTableExisted(db, dClass)) {
            boolean create = SqlExecutor.createTable(db, dClass);
            if (!create)
                throw new IllegalArgumentException(SqlActionResult.ERROR + "The table which you want to insert data was not exists and also created failed!");
        }
        Table table = dClass.getAnnotation(Table.class);
        if (table == null)
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The class of the data which you want to insert has not been added 'Table' annotation!");

        LinkedList<Field> fields = SqlExecutor.columnFields(dClass);
        int fieldSize = fields.size();
        if (fieldSize == 0)
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The class of the data which you want to insert did not find any field added 'Column' annotation!");

        final String TABLE = SqlExecutor.tableName(dClass, table);
        if (TextUtils.isEmpty(TABLE))
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The class of the data which you want to insert has an empty table name of 'Table' annotation!");

        StringBuilder sql = new StringBuilder("INSERT INTO " + TABLE + "(");
        StringBuilder params = new StringBuilder(" VALUES(");
        List<BindValue> bindValueList = new ArrayList<>();

        BindValue bindValue = new BindValue();
        int i = 1;
        for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
            Field field = it.next();
            Column column = field.getAnnotation(Column.class);
            if (!(FieldType.INT.equals(columnType(column, field)) && column.autoIncrement())) {
                sql.append(columnName(column, field)).append(", ");
                params.append("?, ");
                bindValue.put(i, SqlExecutor.columnValue(TABLE, column, field, data));
                i++;
            }
        }
        bindValueList.add(bindValue);

        sql.delete(sql.length() - 2, sql.length());
        params.delete(params.length() - 2, params.length());
        sql.append(")").append(params).append(")");

        Logger.print(TAG, sql.toString());

        List<Long> results = SqlExecutor.insert(db, sql.toString(), bindValueList);
        if (results.size() == 1)
            return results.get(0);

        return -1;
    }
}
