package com.makvenis.hotel.newdbhelp;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/* 数据库操作注册类 */
/**
 * 数据库帮助类，用于管理数据库
 * @author Administrator
 * @author 默认创建一张表（id,key,data）适用于存放json等个人用户信息
 *
 */

public class AppSqliteOpenHelper extends SQLiteOpenHelper {

    public AppSqliteOpenHelper(Context context) {
        //数据库名，数据库版本号
        super(context, "book.db", null, 1);
    }

    public AppSqliteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //操作数据库
        String sql="create table java(id integer primary key," +
                "key text," + //存储的键
                "data text);"; //存储的值

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("drop if table exists java");
        onCreate(db);
    }
}
