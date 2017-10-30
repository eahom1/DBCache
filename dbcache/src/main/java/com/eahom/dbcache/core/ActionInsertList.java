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

import static com.eahom.dbcache.core.SqlExecutor.columnType;

/**
 * Created by eahom on 17/6/3.
 */

final class ActionInsertList<D> {

    private final String TAG = "ActionInsertList";

    private final SQLiteDatabase db;
    private final List<D> data;

    ActionInsertList(SQLiteDatabase db, List<D> data) {
        this.db = db;
        this.data = data;
    }


    protected List<Long> execute() throws IllegalAccessException {
        List<Long> results = new ArrayList<>();
        if (data == null || data.size() == 0)
            return results;

        Class<D> dClass = (Class<D>) data.get(0).getClass();
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
        for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
            Field field = it.next();
            Column column = field.getAnnotation(Column.class);
            final String columnName = SqlExecutor.columnName(column, field);
            if (!(FieldType.INT.equals(columnType(column, field)) && column.autoIncrement())) {
                sql.append(columnName).append(", ");
                params.append("?, ");
            }
            else
                it.remove();
        }
        sql.delete(sql.length() - 2, sql.length());
        params.delete(params.length() - 2, params.length());
        sql.append(")").append(params).append(")");

        Logger.print(TAG, sql.toString());

        List<BindValue> bindValueList = new ArrayList<>();

        for (Iterator<D> dataIt = data.iterator(); dataIt.hasNext(); ) {
            D d = dataIt.next();
            BindValue bindValue = new BindValue();
            int i = 1;
            for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
                Field field = it.next();
                Column column = field.getAnnotation(Column.class);
                bindValue.put(i, SqlExecutor.columnValue(TABLE, column, field, d));
                i++;
            }
            bindValueList.add(bindValue);
        }

        return SqlExecutor.insert(db, sql.toString(), bindValueList);
    }
}
