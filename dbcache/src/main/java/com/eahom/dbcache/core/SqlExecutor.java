package com.eahom.dbcache.core;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.eahom.dbcache.Logger;
import com.eahom.dbcache.annotation.Column;
import com.eahom.dbcache.annotation.FieldType;
import com.eahom.dbcache.annotation.Table;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by eahom on 17/5/27.
 */

final class SqlExecutor {

    private static final String LOG_TAG = "SqlExecutor";

    public static ArrayList<String> allExistsTables(SQLiteDatabase db) {
        ArrayList<String> tableList = new ArrayList<>();
        String sql = "SELECT name FROM Sqlite_master WHERE type='table' ORDER BY name";
        Cursor c = db.rawQuery(sql, null);
        if (c.getCount() > 0)
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                String tableName = c.getString(0);
                if (!"android_metadata".equalsIgnoreCase(tableName))
                    tableList.add(tableName);
            }
        c.close();
        return tableList;
    }

    public static <B> boolean isTableExisted(SQLiteDatabase db, final Class<B> bClass) {
        Table table = bClass.getAnnotation(Table.class);
        if (table == null)
            return false;
        final String TABLE = tableName(bClass, table);
        if (TextUtils.isEmpty(TABLE))
            throw new NullPointerException("TABLE.name:" + TABLE + "is empty");
        String sql = String.format("SELECT COUNT(*) as c FROM Sqlite_master WHERE type='table' and name='%s' ", TABLE);
        Cursor c = db.rawQuery(sql, null);
        boolean result = false;
        if (c.getCount() > 0) {
            c.moveToFirst();
            int count = c.getInt(0);
            if (count > 0)
                result = true;
        }
        c.close();
        return result;
    }

    public static boolean isTableExisted(SQLiteDatabase db, final String TABLE) {
        if (TextUtils.isEmpty(TABLE))
            throw new NullPointerException("TABLE.name:" + TABLE + "is empty");
        String sql = String.format("SELECT COUNT(*) as c FROM Sqlite_master WHERE type='table' and name='%s' COLLATE NOCASE ", TABLE);
        Cursor c = db.rawQuery(sql, null);
        boolean result = false;
        if (c.getCount() > 0) {
            c.moveToFirst();
            int count = c.getInt(0);
            if (count > 0)
                result = true;
        }
        c.close();
        return result;
    }

    public static <B> boolean createTable(SQLiteDatabase db, final Class<B> bClass) {
        Table table = bClass.getAnnotation(Table.class);
        if (table == null)
            throw new IllegalArgumentException(bClass.getSimpleName() + " has not added annotation with Table.class");

        final String TABLE = tableName(bClass, table);

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + TABLE + " (");
        LinkedList<Field> fields = columnFields(bClass);
        int fieldSize = fields.size();
        if (fieldSize == 0)
            return false;
//        ArrayList<String> primaryKeys = new ArrayList<>();
        ArrayList<String> indexColumns = new ArrayList<>();
        for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
            Field field = it.next();
            Column column = field.getAnnotation(Column.class);
            String columnName = columnName(column, field);
            String columnType = columnType(column, field);
            sql.append(columnName).append(' ').append(columnType);

            if (column.primaryKey()) {
                sql.append(" PRIMARY KEY");
                if (FieldType.INT.equals(columnType) && column.autoIncrement())
                    sql.append(" AUTOINCREMENT");
//                primaryKeys.add(columnName);
            }
            if (column.notNull())
                sql.append(" NOT NULL");
            if (column.index() && !column.primaryKey())
                indexColumns.add(columnName);

            sql.append(", ");
        }

//        int primaryKeyCount = primaryKeys.size();
//        if (primaryKeyCount > 0) {
//            sql.append("CONSTRAINT pk_").append(TABLE).append(" PRIMARY KEY (");
//            if (primaryKeyCount == 1)
//                sql.append(primaryKeys.get(0)).append("))");
//            else {
//                for (String primaryKey : primaryKeys)
//                    sql.append(primaryKey).append(", ");
//                sql.delete(sql.length() - 2, sql.length()).append("))");
//            }
//        }
//        else
            sql.delete(sql.length() - 2, sql.length()).append(")");

        StringBuilder createIndexSql = null;
        if (indexColumns.size() > 0) {
            createIndexSql = new StringBuilder("CREATE INDEX uk_");
            createIndexSql.append(TABLE).append(" ON ").append(TABLE).append('(');
            for (String indexColumn : indexColumns)
                createIndexSql.append(indexColumn).append(", ");
            createIndexSql.delete(createIndexSql.length() - 2, createIndexSql.length());
            createIndexSql.append(')');
        }

        Logger.print(LOG_TAG, sql.toString());
        try {
            db.execSQL(sql.toString());
            if (createIndexSql != null) {
                Logger.print(LOG_TAG, createIndexSql.toString());
                db.execSQL(createIndexSql.toString());
            }
            return true;
        }
        catch (SQLException e) {
            Logger.print(LOG_TAG, e);
            return false;
        }
    }

    public static void dropTable(SQLiteDatabase db, final String TABLE) {
        String sql = String.format("DROP TABLE %s", TABLE);
        db.execSQL(sql);
    }

    public static <B> void tableInfo(SQLiteDatabase db, final Class<B> bClass) {
        Table table = bClass.getAnnotation(Table.class);
        if (table == null)
            return ;
        final String TABLE = tableName(bClass, table);
        String sql = String.format("PRAGMA table_info([%s])", TABLE);
        Cursor c = db.rawQuery(sql, null);
        if (c.getCount() > 0) {
            StringBuilder sb = new StringBuilder();
            int columnCount = c.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                sb.append(c.getColumnName(i));
                if (i < columnCount - 1)
                    sb.append("---");
            }
            Logger.print(LOG_TAG, sb.toString());

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                StringBuilder columnInfo = new StringBuilder();
                for (int i = 0; i < columnCount; i++) {
                    columnInfo.append(c.getString(i));
                    if (i < columnCount - 1)
                        columnInfo.append("---");
                }
                Logger.print(LOG_TAG, columnInfo.toString());
            }
        }
        c.close();
    }

    public static String tableName(Class bClass, Table table) {
        String tableName = table.nameAutoUpperCase() ? table.name().toUpperCase() : table.name();
        if (TextUtils.isEmpty(tableName))
            tableName = table.nameAutoUpperCase() ? bClass.getSimpleName().toUpperCase() : bClass.getSimpleName();
        return tableName;
    }

    public static LinkedList<Field> columnFields(Class clazz) {
        Class theClass = clazz;
        LinkedList<Field> fieldList = new LinkedList<>();
        do {
            Field fields[] = theClass.getDeclaredFields();
            if (fields == null)
                continue;

            for (Field field : fields) {
                if (field != null && field.getAnnotation(Column.class) != null)
                    fieldList.add(field);
            }
        }
        while ((theClass = theClass.getSuperclass()) != null &&
                !(theClass.getName().startsWith("java.") ||
                        theClass.getName().startsWith("javax.") ||
                        theClass.getName().startsWith("android.")));
        return fieldList;
    }

    public static Field getDeclaredField(Class clazz, String fieldName) {
        if (TextUtils.isEmpty(fieldName))
            return null;

        Class theClass = clazz;
        Field field = null;
        do {
            try {
                field = theClass.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e) {
            }
        }
        while (field == null && (theClass = theClass.getSuperclass()) != null &&
                !(theClass.getName().startsWith("java.") || theClass.getName().startsWith("javax.") || theClass.getName().startsWith("android.")));

        return field;
    }

    public static Field[] primaryKeyFields(Class bClass) {
        Field fields[] = bClass.getDeclaredFields();
        List<Field> primaryKeys = new ArrayList<>();
        for (Field field : fields) {
            Column column;
            if ((column = field.getAnnotation(Column.class)) != null && column.primaryKey())
                primaryKeys.add(field);
        }
        fields = new Field[primaryKeys.size()];
        int i = 0;
        for (Iterator<Field> it = primaryKeys.iterator(); it.hasNext(); ) {
            fields[i++] = it.next();
        }
        return fields;
    }

    public static String columnName(Column column, Field field) {
        boolean autoUpperCase = column.nameAutoUpperCase();
        String columnName = autoUpperCase ? column.name().toUpperCase() : column.name();
        if (TextUtils.isEmpty(columnName) && field != null)
            columnName = (autoUpperCase ? field.getName().toUpperCase() : field.getName()).trim();

        if (TextUtils.isEmpty(columnName))
            throw new IllegalArgumentException("Column name could not be empty!");

        if (columnName.contains(" "))
            throw new IllegalArgumentException("Column name: '" + columnName + "' that you defined contained space!");

        return columnName;
    }

    public static String columnType(Column column, Field field) {
        String fieldType = column.type();
        if (!TextUtils.isEmpty(fieldType))
            return fieldType;
        Class type = field.getType();
        if (type.isAssignableFrom(Integer.TYPE) || type.isAssignableFrom(Long.TYPE) || type.isAssignableFrom(Short.TYPE))
            fieldType = FieldType.INT;

        else if (type.isAssignableFrom(Float.TYPE) || type.isAssignableFrom(Double.TYPE))
            fieldType = FieldType.REAL;

        else if (type.isAssignableFrom(String.class))
            fieldType = FieldType.TEXT;

        else if (type.isAssignableFrom(Date.class) || type.isAssignableFrom(Timestamp.class))
            fieldType = FieldType.DATE;

        else if (type.isAssignableFrom(Boolean.TYPE))
            fieldType = FieldType.INT;

        else
            fieldType = FieldType.NONE;
        return fieldType;
    }

    public static <B> Object columnValue(String table, Column column, Field field, B bean) throws IllegalAccessException {
        field.setAccessible(true);
        Object value = field.get(bean);
        if (FieldType.DATE.equals(columnType(column, field))) {
            String specifiedFormat = column.dateFormatIfDate();
            if (TextUtils.isEmpty(specifiedFormat))
                throw new IllegalArgumentException("DateFormat must be valued in FieldDateFormat.class");
            if (value != null)
                value = new SimpleDateFormat(specifiedFormat).format(((Date) value));
        }
        else if (field.getType().isAssignableFrom(Boolean.class)) {
            if (value != null) {
                Boolean bool = (Boolean) value;
                if (bool)
                    value = 1;
                else
                    value = 0;
            }
            else
                value = 0;
        }
        if (column.notNull() && value == null)
            throw new NullPointerException(table + ", column:" + columnName(column, field) + " has been specified that field value should not be null!");

        return value;
    }

    /*
    public static final List<Long> insert(SQLiteDatabase db, final String sql, List<BindValue> bindValueList) {
        ArrayList<Long> results = new ArrayList<>();
        SQLiteStatement statement = db.compileStatement(sql);
        for (BindValue bindValue : bindValueList) {
            for (int i : bindValue.keySet()) {
                bindData(statement, i, bindValue.get(i));
            }
            results.add(statement.executeInsert());
        }
        return results;
    }
    */

    /**
     * 单次插入的上限
     */
    private static final int MAX_COUNT_ONCE_INSERTION = 1000;

    public static final List<Long> insert(SQLiteDatabase db, final String sql, List<BindValue> bindValueList) {
        ArrayList<Long> results = new ArrayList<>();
        if (bindValueList == null || bindValueList.size() == 0)
            return results;
        SQLiteStatement statement = db.compileStatement(sql);
        List<BindValue> internalBindValueList = new ArrayList<>();
        while (bindValueList.size() > MAX_COUNT_ONCE_INSERTION) {
            int i = 0;
            for (Iterator<BindValue> it = bindValueList.iterator(); it.hasNext(); ) {
                if (i >= MAX_COUNT_ONCE_INSERTION)
                    break;
                internalBindValueList.add(it.next());
                it.remove();
                i++;
            }
            insertInternal(statement, internalBindValueList, results);
            internalBindValueList.clear();
        }
        insertInternal(statement, bindValueList, results);
        return results;
    }

    private static final void insertInternal(SQLiteStatement statement, List<BindValue> bindValueList, ArrayList<Long> results) {
//        Logger.print(LOG_TAG, "insertInternal. count: " + bindValueList.size());
        if (bindValueList == null || bindValueList.size() > MAX_COUNT_ONCE_INSERTION)
            return;
        for (BindValue bindValue : bindValueList) {
            for (int i : bindValue.keySet()) {
                bindData(statement, i, bindValue.get(i));
            }
            results.add(statement.executeInsert());
        }
    }

    public static final int delete(SQLiteDatabase db, final String sql, List<BindValue> bindValueList) {
        return SqlExecutor.deleteOrUpdate(db, sql, bindValueList);
    }

    public static final int update(SQLiteDatabase db, final String sql, List<BindValue> bindValueList) {
        return SqlExecutor.deleteOrUpdate(db, sql, bindValueList);
    }

    private static final int deleteOrUpdate(SQLiteDatabase db, final String sql, List<BindValue> bindValueList) {
        SQLiteStatement statement = db.compileStatement(sql);
        int count = 0;
        for (BindValue bindValue : bindValueList) {
            for (int i : bindValue.keySet()) {
                bindData(statement, i, bindValue.get(i));
            }
            count += statement.executeUpdateDelete();
        }
        return count;
    }

    private static final void bindData(SQLiteStatement statement, int i, Object value) {
        if (value == null)
            statement.bindNull(i);
//        else if (value.getClass().isArray())
        else if (value instanceof byte[])
            statement.bindBlob(i, (byte[]) value);
        else if (value instanceof Long)
            statement.bindLong(i, (Long) value);
        else if (value instanceof Double) {
            statement.bindDouble(i, (Double) value);
        }
        else
            statement.bindString(i, String.valueOf(value));
    }

    /**
     * If find a record at least, will return a QueryResult instance, otherwise will return null.
     * So please check return value whether null.
     * @param db
     * @param sql
     * @param params
     * @return
     */
    public static final <D> D query(SQLiteDatabase db, final String sql, String[] params, OnQueryResultListener<D> listener) {
        Cursor c = null;
        try {
            c = db.rawQuery(sql, params);
            D data = null;
            if (listener != null)
                data = listener.onQueryResult(new ResultSet(c));
            return data;
        }
        finally {
            if (c != null && !c.isClosed())
                c.close();
        }
    }

    /**
     * If find a record at least, will return a QueryResult instance, otherwise will return null.
     * So please check return value whether null.
     * @param db
     * @param sql
     * @param params
     * @return
     */
    public static final <D> D query(SQLiteDatabase db, final String sql, String[] params, Class<D> dClass) throws IllegalAccessException, InstantiationException, ParseException {
        Cursor c = null;
        D d = null;
        Table table = dClass.getAnnotation(Table.class);
        if (table == null)
            return d;
        if (!isTableExisted(db, dClass)) {
            boolean create = SqlExecutor.createTable(db, dClass);
            if (!create)
                throw new IllegalArgumentException(SqlActionResult.ERROR + "The table which you want to insert data was not exists and also created failed!");
        }

        LinkedList<Field> fieldList = columnFields(dClass);
        if (fieldList.size() == 0)
            return d;

        try {
            c = db.rawQuery(sql, params);
            if (c.getCount() > 0) {
                HashMap<Integer, Field> columnIndicesAndFields = getColumnIndicesAndFieldsMapWhichReturnDataFieldEqualsColumn(fieldList, c);
                if (columnIndicesAndFields.size() > 0) {
                    c.moveToFirst();
                    d = dClass.newInstance();
                    for (Integer columnIndex : columnIndicesAndFields.keySet()) {
                        Field field = columnIndicesAndFields.get(columnIndex);
                        field.setAccessible(true);
                        Object value = getColumnValueFromCursor(field, c, columnIndex);
                        field.set(d, value);
                    }
                }
            }
            return d;
        }
        finally {
            if (c != null && !c.isClosed())
                c.close();
        }
    }

    public static final <D> List<D> queryList(SQLiteDatabase db, final String sql, String[] params, OnQueryResultListener<List<D>> listener) {
        Cursor c = null;
        try {
            c = db.rawQuery(sql, params);
            List<D> dList = null;
            if (listener != null)
                dList = listener.onQueryResult(new ResultSet(c));
            return dList;
        }
        finally {
            if (c != null && !c.isClosed())
                c.close();
        }
    }

    public static final <D> List<D> queryList(SQLiteDatabase db, final String sql, String[] params, Class<D> dClass) throws IllegalAccessException, InstantiationException, ParseException {
        Cursor c = null;
        List<D> dList = new ArrayList<>();
        Table table = dClass.getAnnotation(Table.class);
        if (table == null)
            return dList;
        if (!isTableExisted(db, dClass)) {
            boolean create = SqlExecutor.createTable(db, dClass);
            if (!create)
                throw new IllegalArgumentException(SqlActionResult.ERROR + "The table which you want to insert data was not exists and also created failed!");
        }
        LinkedList<Field> fieldList = columnFields(dClass);
        if (fieldList.size() == 0)
            return dList;
        try {
            c = db.rawQuery(sql, params);
            if (c.getCount() > 0) {
//                printCursorColumn(c);
                HashMap<Integer, Field> columnIndicesAndFields = getColumnIndicesAndFieldsMapWhichReturnDataFieldEqualsColumn(fieldList, c);
                if (columnIndicesAndFields.size() > 0) {
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        D d = dClass.newInstance();
                        for (Integer columnIndex : columnIndicesAndFields.keySet()) {
                            Field field = columnIndicesAndFields.get(columnIndex);
                            field.setAccessible(true);
                            Object value = getColumnValueFromCursor(field, c, columnIndex);
//                            Logger.print(LOG_TAG, "queryList, field:" + field.getName() + ", value:" + value);
                            field.set(d, value);
                        }
                        dList.add(d);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            dList.clear();
        }
        finally {
            if (c != null && !c.isClosed())
                c.close();
            return dList;
        }
    }

    private static final void printCursorColumn(Cursor c) {
        if (c == null || c.isClosed())
            return;
        StringBuilder columns = new StringBuilder("printCursorColumn. columns: ");
        for (int i = 0, count = c.getColumnCount(); i < count; i++) {
            columns.append(c.getColumnName(i)).append(", ");
        }
        Logger.print(LOG_TAG, columns.toString());
    }

    private static final Object getColumnValueFromCursor(Cursor c, int columnIndex) {
        int type = c.getType(columnIndex);
        if (type == Cursor.FIELD_TYPE_STRING)
            return c.getString(columnIndex);

        else if (type == Cursor.FIELD_TYPE_INTEGER)
            return c.getInt(columnIndex);

        else if (type == Cursor.FIELD_TYPE_FLOAT)
            return c.getFloat(columnIndex);

        else if (type == Cursor.FIELD_TYPE_NULL)
            return null;

        else if (type == Cursor.FIELD_TYPE_BLOB)
            return c.getBlob(columnIndex);

        else
            return null;
    }

    private static final Object getColumnValueFromCursor(Field field, Cursor c, int columnIndex) throws ParseException {
        Class type = field.getType();
        if (type.isAssignableFrom(String.class))
            return c.getString(columnIndex);
        else if (type.isAssignableFrom(Integer.TYPE))
            return c.getInt(columnIndex);
        else if (type.isAssignableFrom(Long.TYPE))
            return c.getLong(columnIndex);
        else if (type.isAssignableFrom(Short.TYPE))
            return c.getShort(columnIndex);
        else if (type.isAssignableFrom(Float.TYPE))
            return c.getFloat(columnIndex);
        else if (type.isAssignableFrom(Double.TYPE))
            return c.getDouble(columnIndex);
        else if (type.isAssignableFrom(Character.TYPE)) {
            String str = c.getString(columnIndex);
            return TextUtils.isEmpty(str) ? null : str.charAt(0);
        }
        else if (type.isAssignableFrom(Boolean.TYPE))
            return c.getInt(columnIndex) >= 1 ? true : false;
        else if (type.isAssignableFrom(Date.class) || type.isAssignableFrom(Timestamp.class)) {
            String dateTime = c.getString(columnIndex);
            if (dateTime != null)
                dateTime = dateTime.trim();
            return TextUtils.isEmpty(dateTime) || "NULL".equalsIgnoreCase(dateTime) ?
                    null : new SimpleDateFormat(field.getAnnotation(Column.class).dateFormatIfDate()).parse(dateTime);
        }
        else
            return null;
    }

    private static final HashMap<Integer, Field> getColumnIndicesAndFieldsMapWhichReturnDataFieldEqualsColumn(LinkedList<Field> fieldList, Cursor c) {
        HashMap<Integer, Field> sameIndicesBetweenReturnDataFieldsAndColumns = new HashMap<>();
        if (fieldList == null || fieldList.size() == 0 || c == null || c.isClosed() || c.getColumnCount() == 0)
            return sameIndicesBetweenReturnDataFieldsAndColumns;

        for (int i = 0, columnCount = c.getColumnCount(); i < columnCount; i++) {
            for (Iterator<Field> it = fieldList.iterator(); it.hasNext(); ) {
                Field field = it.next();
                if (columnName(field.getAnnotation(Column.class), field).equals(c.getColumnName(i))) {
                    sameIndicesBetweenReturnDataFieldsAndColumns.put(i, field);
                    it.remove();
                    break;
                }
            }
        }
        return sameIndicesBetweenReturnDataFieldsAndColumns;
    }
}
