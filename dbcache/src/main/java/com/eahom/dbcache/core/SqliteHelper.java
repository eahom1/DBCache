package com.eahom.dbcache.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.eahom.dbcache.Logger;

import java.util.ArrayList;

class SqliteHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = "SqliteHelper";

	private final ArrayList<Class> mBeanClassList = new ArrayList<>();

	private OnSqliteCreateListener mOnCreateListener;
	private OnSqliteUpgradeListener mOnUpgradeListener;

	public SqliteHelper(Context context, String dbName, int dbVersion, ArrayList<Class> beanClassList) {
		this(context, dbName, null, dbVersion);
		if (beanClassList != null && beanClassList.size() > 0)
			mBeanClassList.addAll(beanClassList);
	}

	private SqliteHelper(Context context, String name, CursorFactory factory,
						 int version) {
		super(context, name, factory, version);
	}

	public void setOnCreateListener(OnSqliteCreateListener listener) {
		this.mOnCreateListener = listener;
	}

	public void setOnUpgradeListener(OnSqliteUpgradeListener listener) {
		this.mOnUpgradeListener = listener;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Logger.print(LOG_TAG, "正在创建数据库表...");

		if (mOnCreateListener != null)
			mOnCreateListener.beforeCreateTableWithBeanList();

		for (Class clazz : mBeanClassList)
			SqlExecutor.createTable(db, clazz);

		if (mOnCreateListener != null)
			mOnCreateListener.afterCreateTableWithBeanList();

		mOnCreateListener = null;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.print(LOG_TAG, "正在升级数据库表...");

		if (mOnUpgradeListener != null)
			mOnUpgradeListener.beforeDropOldTables(oldVersion, newVersion);

		for (String existedTable : SqlExecutor.allExistsTables(db))
			SqlExecutor.dropTable(db, existedTable);

		if (mOnUpgradeListener != null)
			mOnUpgradeListener.afterDropOldTablesAndBeforeCreateTableWithBeanList(oldVersion, newVersion);

		for (Class clazz : mBeanClassList)
			SqlExecutor.createTable(db, clazz);

		if (mOnUpgradeListener != null)
			mOnUpgradeListener.afterCreateTableWithBeanList(oldVersion, newVersion);

		mOnUpgradeListener = null;
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.print(LOG_TAG, "数据库降级了...");
		super.onDowngrade(db, oldVersion, newVersion);
	}
}
