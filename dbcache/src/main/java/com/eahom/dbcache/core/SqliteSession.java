package com.eahom.dbcache.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.eahom.dbcache.Logger;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * It it a proxy of SqliteDatabase.
 * Use it to open and close Sqlite.
 */
class SqliteSession {

	private static final String LOG_TAG = "SqliteSession";
	
	private AtomicInteger mOpenCounter = new AtomicInteger();
	
	private static SqliteSession mInstance;
	
	private static class SqliteSessionHolder {
        private static SqliteSession mHolder = new SqliteSession();
    }  
	
	private static SqliteHelper mHelper;
	private SQLiteDatabase mDatabase;
	
	public static synchronized void initialize(Context context, String dbName, int dbVersion, ArrayList<Class> beanClassList) {
        if (mInstance == null) {
        	mInstance = SqliteSessionHolder.mHolder;
        	mHelper = new SqliteHelper(context, dbName, dbVersion, beanClassList);
        }
    }
	
	public static synchronized SqliteSession instance() {
		if (mInstance == null)
			throw new NullPointerException("SqliteManager未被初始化");
		return mInstance;
	}

	public void setOnSqliteCreateListener(OnSqliteCreateListener listener) {
		mHelper.setOnCreateListener(listener);
	}

	public void setOnSqliteUpgradeListener(OnSqliteUpgradeListener listener) {
		mHelper.setOnUpgradeListener(listener);
	}
	
	public SQLiteDatabase open() {
		synchronized (SqliteSession.class) {
			if (mOpenCounter.incrementAndGet() == 1)
				mDatabase = mHelper.getWritableDatabase();
			Logger.print(LOG_TAG, "open, current connecting count: " + mOpenCounter.get());
		}
		return mDatabase;
	}
	
	public void close() {
		synchronized (SqliteSession.class) {
			if (mOpenCounter.decrementAndGet() == 0) {
				Logger.print(LOG_TAG, "close, need to close, current connecting count: " + mOpenCounter.get());
				mDatabase.close();
			}
			else
				Logger.print(LOG_TAG, "close, need not to close, current connecting count: " + mOpenCounter.get());
		}
	}
	
}
