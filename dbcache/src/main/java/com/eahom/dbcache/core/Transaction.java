package com.eahom.dbcache.core;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by eahom on 17/6/3.
 */

public abstract class Transaction<R> {

    private final String TAG = "Transaction";


    public Transaction() {}


    public final R execute() {
        R r = null;
        SQLiteDatabase db = SqliteSession.instance().open();
        db.beginTransaction();
        try {
            r = task(new SqliteExecutor(db));
            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            SqliteSession.instance().close();
            return r;
        }
    }

    protected abstract R task(SqliteExecutor executor) throws Exception;
}
