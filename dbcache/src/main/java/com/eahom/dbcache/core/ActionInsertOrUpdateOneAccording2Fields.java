package com.eahom.dbcache.core;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.eahom.dbcache.Logger;
import com.eahom.dbcache.annotation.Column;
import com.eahom.dbcache.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by eahom on 17/6/3.
 */

class ActionInsertOrUpdateOneAccording2Fields<D> {

    private final String TAG = "ActionInsertOrUpdateOneAccording2Fields";

    private final SQLiteDatabase db;
    private final D data;
    private final String[] fieldNamesForSelection;


    ActionInsertOrUpdateOneAccording2Fields(SQLiteDatabase db, D data, String[] fieldNamesForSelection) {
        this.db = db;
        this.data = data;
        this.fieldNamesForSelection = fieldNamesForSelection;
    }

    protected int execute() throws NoSuchFieldException, IllegalAccessException {
        if (data == null)
            return 0;

        if (fieldNamesForSelection == null || fieldNamesForSelection.length == 0)
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The data you want to update without indicating the fields that according to, please check!");

        Class<D> dClass = (Class<D>) data.getClass();
        if (!SqlExecutor.isTableExisted(db, dClass)) {
            boolean create = SqlExecutor.createTable(db, dClass);
            if (!create)
                throw new RuntimeException(SqlActionResult.ERROR + "The table which you want to insert of update data was not exists and also created failed!");
        }
        Table table = dClass.getAnnotation(Table.class);
        if (table == null)
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The class of the data which you want to insert or update has not been added 'Table' annotation!");

        final String TABLE = SqlExecutor.tableName(dClass, table);
        if (TextUtils.isEmpty(TABLE))
            throw new IllegalArgumentException(SqlActionResult.ERROR + "The class of the data which you want to insert or update has an empty table name of 'Table' annotation!");

        LinkedList<Field> selectionFields = new LinkedList<>();

        StringBuilder querySql = new StringBuilder("SELECT * FROM ").append(TABLE).append(" WHERE 1=1 ");
        String[] selectionArgs = new String[fieldNamesForSelection.length];
        int i = 0;
        for (String fieldName : fieldNamesForSelection) {
            Field field = SqlExecutor.getDeclaredField(dClass, fieldName);
            if (field == null)
                break;
            Column column = field.getAnnotation(Column.class);
            if (column == null)
                throw new IllegalArgumentException(SqlActionResult.ERROR + "The field which you want to using it to insert or update data without 'Column' annotation!");
            querySql.append("AND ").append(SqlExecutor.columnName(column, field)).append("=? ");
            selectionArgs[i++] = String.valueOf(SqlExecutor.columnValue(TABLE, column, field, data));
            selectionFields.add(field);
        }

        Cursor c = null;
        try {
            c = db.rawQuery(querySql.toString(), selectionArgs);
            if (c.getCount() == 0) {
                Logger.print(TAG, "本次操作，插入");
                long rowId = new ActionInsertOne<>(db, data).execute();
                if (rowId >= 0)
                    return 1;
                else
                    return 0;
            }
            Logger.print(TAG, "本次操作，更新");
            LinkedList<Field> updateFields = SqlExecutor.columnFields(dClass);
            for (Iterator<Field> updateIt = updateFields.iterator(); updateIt.hasNext(); ) {
                Field updateField = updateIt.next();
                for (Iterator<Field> selectionIt = selectionFields.iterator(); selectionIt.hasNext(); ) {
                    Field selectionField = selectionIt.next();
                    if (updateField.getName().equals(selectionField.getName())) {
                        updateIt.remove();
                        break;
                    }
                }
            }
            String[] fieldNamesForUpdate = new String[updateFields.size()];
            i = 0;
            for (Iterator<Field> updateIt = updateFields.iterator(); updateIt.hasNext(); ) {
                Field updateField = updateIt.next();
                fieldNamesForUpdate[i++] = updateField.getName();
            }
            ArrayList<D> list = new ArrayList<>();
            list.add(data);
            return new ActionUpdateFieldsAccording2Fields<>(db, list, fieldNamesForUpdate, fieldNamesForSelection).execute();
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        finally {
            if (c != null && !c.isClosed())
                c.close();
        }
    }
}
