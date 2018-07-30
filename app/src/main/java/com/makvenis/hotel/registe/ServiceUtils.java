package com.makvenis.hotel.registe;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.makvenis.hotel.tools.Configfile;

/**
 * 操作服务器的方法
 */

public class ServiceUtils {

    /* 操作手机发送验证码 */

    /**
     * 向目标发送验证码
     * @param phone 发送的手机号码
     */
    public static void getPostCodeToPhone(final String phone,final Context mContext, final Handler mHandler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = Configfile.GET_CODE+"?condition="+phone;
                new HttpUtils(10000).send(HttpRequest.HttpMethod.GET,
                        path,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Message msg=new Message();
                                    msg.what=1;
                                    msg.obj=result;
                                    mHandler.sendMessage(msg);
                                }
                                Log.e("LOG","获取验证码 "+result);
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                    Configfile.Log(mContext,"连接失败");
                            }
                        });


            }
        }).start();
    }

}
