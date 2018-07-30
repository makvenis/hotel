package com.makvenis.hotel.newdbhelp;

//数据库帮助类

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link AppMothedHelper} 数据库帮助类  更多的时候需要自定义当前项目常用的SQL操作
 *
 */

public class AppMothedHelper {

    /* 获取数据库的继承 */
    private AppSqliteOpenHelper mOpenHelper;//数据库帮助类

    /* Tag */
    private String TAG = "AppMothedHelper";

    public AppMothedHelper(Context context) {
        mOpenHelper = new AppSqliteOpenHelper(context);
    }

    //获取连接
    //SQLiteDatabase db= mOpenHelper.getWritableDatabase();

    /* 插入数据 */
    public void dbInsert(String[] strings) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db.isOpen()) {
            //执行添加的操作
            db.execSQL("insert into java(key, data) values(?, ?);",
                    strings);
            Log.e(TAG, "执行--插入--dbInsert()操作");
            db.close();
        } else {
            Log.e(TAG, "打开数据库连接失败");
        }
    }

    /* 插入数据 */
    public void dbCursorInsert(String mKey, String mData) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put("key", mKey);
            values.put("data", mData);
            //执行添加的操作
            long id = db.insert("java", null, values);
            Log.e(TAG, "执行--插入--dbCursorInsert()操作");
            db.close();
        } else {
            Log.e(TAG, "打开数据库连接失败");
        }
    }

    /* 删除数据 */
    public void delete(String mKey) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("delete from java where key = ?;", new String[]{mKey});
            //执行添加的操作
            Log.e(TAG, "执行--删除--delete()操作");
            db.close();
        } else {
            Log.e(TAG, "打开数据库连接失败");
        }
    }

    /* 更新数据 （常用） 与他重载的方法是直接输入SQL语句*/
    public void update(String mKey, String mData) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("update java set data = ? where key = ?;", new String[]{mData, mKey});
            //执行添加的操作
            Log.e(TAG, "执行--更新--update()操作");
            db.close();
        } else {
            Log.e(TAG, "打开数据库连接失败");
        }
    }

    /* 更新数据 （常用） 与他重载的方法是通过键来查询（限制查询固定样式）语句*/
    /**
     * @param Sql 预备执行的SQL语句 update Table set data = ? where key = ?
     * @param mKey 更新的条件
     * @param mData 需要更新的数据
     */
    public void update(String Sql, String mKey, String mData) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL(Sql, new String[]{mData, mKey});
            //执行添加的操作
            Log.e(TAG, "执行--更新--update()操作");
            db.close();
        } else {
            Log.e(TAG, "打开数据库连接失败");
        }
    }


    /* 原始语句查询 （废弃作用是不能获取到数据） */
    @Deprecated
    public void executeSql(String sql) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        db.execSQL(sql);
    }

    /* 查询 通过键数据（废弃默认查询一条的数据） */
    @Deprecated
    public Map<Object, Object> queryByKey(String condationKey) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Map<Object, Object> map = new HashMap<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from java where key= ?;", new String[]{condationKey});
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                String key = cursor.getString(1);
                String data = cursor.getString(2);

                map.put("id", id);
                map.put("key", key);
                map.put("data", data);

                Log.e(TAG, "执行--查询--queryByKey()操作");

                db.close();
            }
            db.close();
        } else {
            Log.e(TAG, "打开数据库连接失败");
        }

        if (map.size() != 0) {
            return map;
        }
        return new HashMap<Object, Object>();
    }

    /* 查询 通过键数据 （查询数据库中满足条件的所有数据）常用 */
    public List<Map<String, Object>> queryByKeyAddrs(String userName) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        List<Map<String, Object>> data = new ArrayList<>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from addrs where user= ?;", new String[]{userName});

            if (cursor != null) {

                String[] names = cursor.getColumnNames();

                while (cursor.moveToNext()) {
                    Map<String, Object> mMin = new HashMap<>();

                    for (int i = 0; i < names.length; i++) {

                        if (i == 0) {
                            mMin.put(names[i], cursor.getInt(0));
                        } else {
                            mMin.put(names[i], cursor.getString(i));
                        }
                    }

                    data.add(mMin);

                }
            }

            db.close();
        } else {
            Log.e(TAG, "打开数据库连接失败");
        }

        if (data.size() != 0) {
            return data;
        }
        return new ArrayList<>();
    }

    /* 判断当前数据是否存在(废弃 部分使用了废弃的查询方法) */
    @Deprecated
    public static boolean isDismisData(Context mContext, String mKey) {
        //查询当前数据
        AppMothedHelper helper = new AppMothedHelper(mContext);
        Map<Object, Object> objectMap = helper.queryByKey(mKey);
        String data = (String) objectMap.get("data");
        Log.e("TAG", "在执行 AppMothedHelper 类的判断数据是否存在的时候做的查询结果 >>>>" + data); // null
        if (data != null) {
            return true;
        }
        return false;
    }


}
