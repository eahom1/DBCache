package com.eahom.dbcache.core;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.util.List;

/**
 * Created by eahom on 17/6/5.
 */

public final class SqliteExecutor {

    private final SQLiteDatabase db;

    SqliteExecutor(SQLiteDatabase db) {
        this.db = db;
    }

    public boolean isTableExists(final String tableName) {
        return SqlExecutor.isTableExisted(this.db, tableName);
    }

    public void execSQL(String sql) {
        db.execSQL(sql);
    }

    public void execSQL(String sql, Object[] bindArgs) {
        db.execSQL(sql, bindArgs);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    public <D> long insertOne(D data) throws IllegalAccessException {
        return new ActionInsertOne<>(db, data).execute();
    }

    public <D> List<Long> insertList(List<D> data) throws IllegalAccessException {
        return new ActionInsertList<>(db, data).execute();
    }

    public int delete(String sql, Object[] params) {
        return new ActionDelete(db, sql, params).execute();
    }

    public <D> int deleteAccording2Fields(List<D> data, String[] fieldNamesForSelection) throws NoSuchFieldException, IllegalAccessException {
        return new ActionDeleteAccording2Fields<>(db, data, fieldNamesForSelection).execute();
    }

    public int update(String sql, Object[] params) {
        return new ActionUpdate(db, sql, params).execute();
    }

    public <D> int updateAccording2Fields(List<D> data, String[] fieldNamesForSelection) throws NoSuchFieldException, IllegalAccessException {
        return new ActionUpdateAccording2Fields<>(db, data, fieldNamesForSelection).execute();
    }

    public <D> int updateFieldsAccording2Fields(List<D> data, String[] fieldNamesForUpdate, String[] fieldNamesForSelection) throws NoSuchFieldException, IllegalAccessException {
        return new ActionUpdateFieldsAccording2Fields<>(db, data, fieldNamesForUpdate, fieldNamesForSelection).execute();
    }

    public <D> D query(String sql, String[] params, OnQueryResultListener<D> listener) {
        return SqlExecutor.query(db, sql, params, listener);
    }

    public <D> D query(String sql, String[] params, Class<D> dClass) throws InstantiationException, IllegalAccessException, ParseException {
        return SqlExecutor.query(db, sql, params, dClass);
    }

    public <D> List<D> queryList(String sql, String[] params, OnQueryResultListener<List<D>> listener) {
        return SqlExecutor.queryList(db, sql, params, listener);
    }

    public <D> List<D> queryList(String sql, String[] params, Class<D> dClass) throws InstantiationException, IllegalAccessException, ParseException {
        return SqlExecutor.queryList(db, sql, params, dClass);
    }

    public <D> int insertOrUpdateOneAccording2Fields(D data, String[] fieldNamesForSelection) throws NoSuchFieldException, IllegalAccessException {
        return new ActionInsertOrUpdateOneAccording2Fields<>(db, data, fieldNamesForSelection).execute();
    }

    public <D> boolean insertOrUpdateListAccording2Fields(List<D> data, String[] fieldNamesForSelection) throws NoSuchFieldException, IllegalAccessException {
        return new ActionInsertOrUpdateListAccording2Fields<>(db, data, fieldNamesForSelection).execute();
    }
}
