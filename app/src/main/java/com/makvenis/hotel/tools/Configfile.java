package com.makvenis.hotel.tools;

/* 配置文件 */

import android.content.Context;
import android.widget.Toast;

import json.makvenis.com.mylibrary.json.view.SimpleToast;

/**
 * {@link Configfile} 配置文件 主要用途网络请求上传地址、服务器、静态常量、判断文件、权限的参数配置
 */

public class Configfile {

    /* 服务器IP地址 */
    public static final String IP="http://2w15066y11.imwork.net:21538";
    //public static final String IP="http://192.168.1.3";

    /* 服务器项目名称 */
    public static final String WEB="/im";

    /* app路径 */
    public  static final String PATH=Configfile.IP+Configfile.WEB;
    /* 文件上传地址 */
    public static final String UPLOAD_FILE_PATH=Configfile.PATH+"/MyUploadFile";
    /* 获取验证码 使用方法（http://127.0.0.1/im/apiService/NumberValidationService?condition=110） */
    public static final String GET_CODE=Configfile.PATH+"/apiService/NumberValidationService";
    /* 验证用户验证码信息 */
    public static final String VALIDATION=Configfile.PATH+"/apiService/RegistrationService";
    /* 用户输入注册 */
    public static final String REGISTE_INPUT_POST=Configfile.PATH+"/apiService/RegisteInputService";
    /* 用户账号密码登陆 */
    public static final String LOGINUSER_INPUT_POST=Configfile.PATH+"/apiService/UserloginService";
    /* 天气接口数据 */
    public static final String WEATHER_DATA="https://www.sojson.com/open/api/weather/json.shtml?city=";
    /* 是否允许开启当前广告栏位 */
    public static boolean IS_ADV = false;
    /* 全局文件包名 */
    public static String PACKAGE = "com.makvenis.hotel";
    /* 页面Log */
    public static void Log(Context context, String mLog){
        SimpleToast.makeText(context,mLog, Toast.LENGTH_SHORT).show();
    }
    /* json异常语句 */
    public static final String JSON_EXCEPTION = "不是一个合格的json数据,来源与-->";
    public static String jsonException(String exceptionPath){
        return Configfile.JSON_EXCEPTION+exceptionPath;
    }
    /* 数据来源 START */
    //获取头部幻灯
    public static final String HD_PATH=Configfile.PATH+"/apiService/MySimpleService";
    //增删改查通用服务 参数（book,values）
    public static final String EXECUTE_SQL_SERVICE=Configfile.PATH+"/apiService/SimpleSqlService";
    //请求post服务 此服务限于通过book来配合插入数据到数据库
    public static final String POST_INSERT_SERVICE=Configfile.PATH+"/apiService/SimplePostService";
}
