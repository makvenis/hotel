package com.makvenis.hotel.registe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.makvenis.hotel.R;
import com.makvenis.hotel.activity.MainActivity;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.xutils.AppHelpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import json.makvenis.com.mylibrary.json.view.SimpleDialogView;
import json.makvenis.com.mylibrary.json.view.SimpleToast;

/**
 * 用户第一次登陆，就去提示用户密码账号登陆
 * 自定义用户名登陆，或者短信验证码登陆
 */
@ContentView(R.layout.activity_registered)
public class LoginActivity extends AppCompatActivity {

    public final Context mContext = LoginActivity.this;
    /* 全局dialog */
    public SimpleDialogView dialog;

    @ViewInject(R.id.mLoginName)
    EditText mLoginName;

    @ViewInject(R.id.mLoginPass)
    EditText mLoginPass;

    /* 登陆 */
    @ViewInject(R.id.mLoginPost)
    Button mLoginPost;

    /* 注册 */
    @ViewInject(R.id.mLoginRegiste)
    Button mLoginRegiste;

    /* 短信验证登陆 */
    @ViewInject(R.id.mLoginMsgPost)
    TextView mLoginMsgPost;

    /* 获取两条分割线 */
    @ViewInject(R.id.mLoginLine1)
    View line1;

    @ViewInject(R.id.mLoginLine2)
    View line2;

    public Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 1:
                    String obj = ((String) msg.obj);
                    if(obj != ""){
                        try {
                            JSONObject object=new JSONObject(obj) ;
                            String state = object.optString("state");
                            if(state.equals("ok")){
                                //存储用户名
                                String bank = object.optString("bank");
                                Log.e("LOGO",bank);
                                boolean existBool = AppHelpMethod.isExistBool(
                                        "select data from user where key = 'localuser' order by id desc limit 1"
                                        , new String[]{"data"});
                                if(existBool){
                                    //更新。。。
                                    Log.e("LOGO","更新。。。");
                                    AppHelpMethod.update("localuser",bank);
                                }else {
                                    //增加。。。
                                    Log.e("LOGO","增加。。。");
                                    boolean localuser = AppHelpMethod.add("localuser", bank);
                                    Log.e("LOGO","增加返回值"+localuser);
                                }
                                startActivity(new Intent(mContext, MainActivity.class));
                                dialog.close();
                                finish();
                            }else {
                                SimpleToast.makeText(mContext,"登陆失败",Toast.LENGTH_SHORT).show();
                                dialog.close();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        /* 日志 */
        Log.e("LOG","进入---LoginActivity---登陆页面");
    }

    /* 登陆 */
    @OnClick({R.id.mLoginPost})
    public void mLoginPost(View v){
        // TODO: 2018/7/3 启动dailog
        dialog = new SimpleDialogView(mContext,"玩命加载中...");
        dialog.show();
        //获取输入参数
        final String mName = mLoginName.getText().toString();
        final String mPass = mLoginPass.getText().toString();
        Log.e("LOG","进入---LoginActivity---登陆按钮---值为"+mName+"---"+mPass);
        if(!mName.equals("") && !mPass.equals("")){
            line1.setBackgroundColor(Color.WHITE);
            line2.setBackgroundColor(Color.WHITE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String mPath= Configfile.LOGINUSER_INPUT_POST;
                    RequestParams params=new RequestParams();
                    params.addBodyParameter("name",mName);
                    params.addBodyParameter("pass",mPass);
                    new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                            mPath,
                            params, new RequestCallBack<String>() {
                                @Override
                                public void onSuccess(ResponseInfo<String> responseInfo) {
                                    String result = responseInfo.result;
                                    if(result != null){
                                        Message msg=new Message();
                                        msg.what=1;
                                        msg.obj=result;
                                        mHandler.sendMessage(msg);
                                    }
                                }

                                @Override
                                public void onFailure(HttpException e, String s) {
                                    Configfile.Log(mContext,"网络连接失败");
                                    dialog.close();
                                }
                            });
                }
            }).start();



        }else {
            line1.setBackgroundColor(Color.RED);
            line2.setBackgroundColor(Color.RED);
            SimpleToast.makeText(this,"账号密码不能为空", Toast.LENGTH_SHORT).show();
            dialog.close();
        }
    }

    /* 注册 */
    @OnClick({R.id.mLoginRegiste})
    public void mLoginRegiste(View v){
        Intent intent = new Intent(this, ClientInputRegisteActivity.class);
        intent.putExtra("class",Configfile.PACKAGE+".registe.LoginActivity");
        startActivity(intent);
    }


    /* 短信登陆 */
    @OnClick({R.id.mLoginMsgPost})
    public void mLoginMsgPost(View v){
        Intent intent = new Intent(this, ClientRegisteActivity.class);
        intent.putExtra("class", Configfile.PACKAGE+".registe.LoginActivity");
        startActivity(intent);
    }

    /* [退出] 当用户打开APP 不想登陆 则直接退出 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
