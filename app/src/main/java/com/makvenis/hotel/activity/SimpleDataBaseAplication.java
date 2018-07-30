package com.makvenis.hotel.activity;

import android.app.Application;

import org.xutils.x;

/**
 * 数据库的初始化
 */

public class SimpleDataBaseAplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        x.Ext.init(this);
        x.Ext.setDebug(false); // 是否输出debug日志
    }


}
