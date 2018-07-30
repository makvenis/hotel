package com.makvenis.hotel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.makvenis.hotel.tools.Configfile;

/**
 * Activity的跳转
 */

/**
 * 用法：（上下文，跳转而来的ClassName,当前页面的类名称作为键）
 * Bank.goTo(this,mClassName,"ClientInputRegisteActivity");
 * 用法：（上下文，当前页面的类名称作为键）
 * Bank.goBank(this, "ClientInputRegisteActivity");
 * 用户：（在获取参数的页面使用 {@link Bank {@link #goBank(Context, String)}}）
 *
 * 配置文件定义为
 * {@link Configfile}
 * public static String PACKAGE = "com.makvenis.hotel";
 *
 * @OnClick({R.id.mLoginMsgPost})
 * public void mLoginMsgPost(View v){
 * Intent intent = new Intent(this, ClientRegisteActivity.class);
 * intent.putExtra("class", Configfile.PACKAGE+".registe.LoginActivity");
 * startActivity(intent);
 * }
 *
 * public void getparentData() {
 * Intent intent = getIntent();
 * mClassName = intent.getStringExtra("class");
 * 储存返回的计划事件
 * Bank.goTo(this,mClassName,"ClientInputRegisteActivity");
 * }
 *
 */

public class Bank {

    /* xml名称 */
    public static String XML_NAME="activityJump";

    /* 全局定义当取值为空的时候页面返回到主activity */
    public static String HOME_CLASS= Configfile.PACKAGE+".activity.MainActivity";
    /**
     *
     * @param mContext 上下文
     * @param mClassName 预备存储的class名称 （目标class的来源，即时当前class名称）
     * @param thisClassName 当前class名称（也就是当返回的时候需要去获取有哪一个页面跳转而来的）
     */
    public static void goTo(Context mContext,String mClassName,String thisClassName){
        /* 存储用户设置 */
        SharedPreferences pref = mContext.getSharedPreferences(XML_NAME,mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if(mClassName != null && thisClassName != null){
            editor.putString(thisClassName,mClassName);
            Log.e("LOGO","在" + thisClassName + "页面中执行 goTo() 存储类名称" + mClassName + "成功" );
            editor.commit();
        }else {
            new IllegalArgumentException("当前存储时候 [mClassName] [thisClassName] 为空 ["+mClassName+"] ["+thisClassName+"]");
        }
    }

    /**
     *
     * @param mContext 上下文
     * @param thisClassName 当前类名称
     * @return 返回跳转的目标类
     */
    public static String goBank(Context mContext,String thisClassName){
        /* 获取用户的数据 */
        SharedPreferences pref = mContext.getSharedPreferences(XML_NAME,mContext.MODE_PRIVATE);
        String mBankClass = pref.getString(thisClassName, HOME_CLASS);
        if(mBankClass != null){
            Log.e("LOGO","在" + thisClassName + "页面中执行 goBank() 获取类名称 [" + mBankClass + "] 成功" );
            return mBankClass;
        }else {
            return HOME_CLASS;
        }
    }

}
