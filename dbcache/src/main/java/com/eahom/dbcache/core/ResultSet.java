package com.eahom.dbcache.core;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

/**
 * The {@link ResultSet} was proxy of cursor.
 *
 * Created by eahom on 17/6/5.
 */
public final class ResultSet {

    private final Cursor c;

    ResultSet(Cursor c) {
        this.c = c;
    }



    public int getCount() {
        return c.getCount();
    }


    public int getPosition() {
        return c.getPosition();
    }


    public boolean move(int offset) {
        return c.move(offset);
    }


    public boolean moveToPosition(int position) {
        return c.moveToPosition(position);
    }


    public boolean moveToFirst() {
        return c.moveToFirst();
    }


    public boolean moveToLast() {
        return c.moveToLast();
    }


    public boolean moveToNext() {
        return c.moveToNext();
    }


    public boolean moveToPrevious() {
        return c.moveToPrevious();
    }


    public boolean isFirst() {
        return c.isFirst();
    }


    public boolean isLast() {
        return c.isLast();
    }


    public boolean isBeforeFirst() {
        return c.isBeforeFirst();
    }


    public boolean isAfterLast() {
        return c.isAfterLast();
    }


    public int getColumnIndex(String columnName) {
        return c.getColumnIndex(columnName);
    }


    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        return c.getColumnIndexOrThrow(columnName);
    }


    public String getColumnName(int columnIndex) {
        return c.getColumnName(columnIndex);
    }


    public String[] getColumnNames() {
        return c.getColumnNames();
    }


    public int getColumnCount() {
        return c.getColumnCount();
    }


    public byte[] getBlob(int columnIndex) {
        return c.getBlob(columnIndex);
    }


    public String getString(int columnIndex) {
        return c.getString(columnIndex);
    }


    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        c.copyStringToBuffer(columnIndex, buffer);
    }


    public short getShort(int columnIndex) {
        return c.getShort(columnIndex);
    }


    public int getInt(int columnIndex) {
        return c.getInt(columnIndex);
    }


    public long getLong(int columnIndex) {
        return c.getLong(columnIndex);
    }


    public float getFloat(int columnIndex) {
        return c.getFloat(columnIndex);
    }


    public double getDouble(int columnIndex) {
        return c.getDouble(columnIndex);
    }


    public int getType(int columnIndex) {
        return c.getType(columnIndex);
    }


    public boolean isNull(int columnIndex) {
        return c.isNull(columnIndex);
    }


    public void deactivate() {
        c.deactivate();
    }


    public boolean requery() {
        return c.requery();
    }

//
//    public void close() {
//
//    }

//
//    public boolean isClosed() {
//        return false;
//    }


    public void registerContentObserver(ContentObserver observer) {
        c.registerContentObserver(observer);
    }


    public void unregisterContentObserver(ContentObserver observer) {
        c.unregisterContentObserver(observer);
    }


    public void registerDataSetObserver(DataSetObserver observer) {
        c.registerDataSetObserver(observer);
    }


    public void unregisterDataSetObserver(DataSetObserver observer) {
        c.unregisterDataSetObserver(observer);
    }


    public void setNotificationUri(ContentResolver cr, Uri uri) {
        c.setNotificationUri(cr, uri);
    }


    public Uri getNotificationUri() {
        if (Build.VERSION.SDK_INT >= 19)
            return c.getNotificationUri();
        return null;
    }


    public boolean getWantsAllOnMoveCalls() {
        return c.getWantsAllOnMoveCalls();
    }


    public void setExtras(Bundle extras) {
        if (Build.VERSION.SDK_INT >= 23)
            c.setExtras(extras);
    }


    public Bundle getExtras() {
        return c.getExtras();
    }


    public Bundle respond(Bundle extras) {
        return c.respond(extras);
    }
}
