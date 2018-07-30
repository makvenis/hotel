package com.makvenis.hotel.utils;

/* App静态实用类 */


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

/**
 * {@link AppStaticUtils } App静态实用类 一些经常使用和必须使用的方法
 * {@link #isFirstStartApp} 判断App是否第一次启动
 *
 */
public class AppStaticUtils {

    /* 判断App是否第一次启动 */
    public static boolean isFirstStartApp(Context mContext){
        //加载是否首次启动
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("start",MODE_PRIVATE);
        int count = sharedPreferences.getInt("count",0);
        Log.d("print", String.valueOf(count));
        //判断程序是第几次运行，如果是第一次运行则跳转到引导页面
        if(count == 0){ //不是第一次启动
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //存入数据
            editor.putInt("count",count++);
            //提交修改
            editor.apply();
            return false;
        }
        return true;
    }


}
