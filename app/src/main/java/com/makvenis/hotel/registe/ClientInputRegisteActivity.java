package com.makvenis.hotel.registe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.makvenis.hotel.utils.Bank;
import com.makvenis.hotel.xutils.AppHelpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import json.makvenis.com.mylibrary.json.tools.UploadPhotoActivity;
import json.makvenis.com.mylibrary.json.view.SimpleDialogView;
import json.makvenis.com.mylibrary.json.view.SimpleImageViewCircleBitmap;
import json.makvenis.com.mylibrary.json.view.SimpleToast;

/* 客服端手动输入注册界面 */
@ContentView(R.layout.activity_client_input_registe)
public class ClientInputRegisteActivity extends AppCompatActivity {

    public final Context mContext=ClientInputRegisteActivity.this;

    /* 获取用户图标 */
    @ViewInject(R.id.mLoginInputPhoto)
    SimpleImageViewCircleBitmap mSimpleImageViewCircleBitmap;

    /* 用户名 */
    @ViewInject(R.id.mLoginInputName)
    EditText mLoginInputName;

    /* 密码 */
    @ViewInject(R.id.mLoginInputPass)
    EditText mLoginInputPass;

    /* 用户电话 */
    @ViewInject(R.id.mLoginInputPhone)
    EditText mLoginInputPhone;

    /* 获取验证码按钮 */
    @ViewInject(R.id.mLoginInputGetCode)
    Button mLoginInputGetCode;

    /* 输入验证码 */
    @ViewInject(R.id.mLoginInputCode)
    EditText mLoginInputCode;

    /* 提交注册 */
    @ViewInject(R.id.mLoginInputRegiste)
    Button mLoginInputRegiste;

    /* 全局dialog */
    SimpleDialogView dialog;


    /* 公共返回 */
    @ViewInject(R.id.mSimpleBank)
    ImageView mSimpleBank;
    private String mClassName;

    public Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 2:
                    String obj = (String) msg.obj;
                    try {
                        JSONObject object=new JSONObject(obj);
                        String state = object.optString("state");
                        if(state.equals("ok")){
                            //存储用户数据到本地数据库
                            String bank = object.optString("bank");
                            Log.e("LOGO",bank);
                            boolean existBool = AppHelpMethod.isExistBool(
                                    "select data from user where key = 'localuser'"
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
                            SimpleToast.makeText(mContext,"注册成功",Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            dialog.close();
                            SimpleToast.makeText(mContext,"注册失败",Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        ViewUtils.inject(this);




        /* 获取父类传递的参数 */
        getparentData();

        /* 获取验证码 */
        mLoginInputGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 获取输入的手机号码 */
                String phone = mLoginInputPhone.getText().toString();
                ServiceUtils.getPostCodeToPhone(phone,mContext,mHandler);

                CountDownTimer countDownTimer=new CountDownTimer(60*1000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int i = (int) (millisUntilFinished / 1000);
                        mLoginInputGetCode.setText(i+"S后重新获取");
                        mLoginInputGetCode.setTextColor(Color.WHITE);
                        mLoginInputGetCode.setEnabled(false);
                    }

                    @Override
                    public void onFinish() {
                        mLoginInputGetCode.setEnabled(true);
                        mLoginInputGetCode.setText("没有收到?重新获取");
                        mLoginInputGetCode.setTextColor(Color.BLACK);
                    }
                }.start();
            }
        });
    }

    @OnClick({R.id.mLoginInputRegiste})
    public void mLoginInputRegiste(View view){

        dialog=new SimpleDialogView(this,"玩命加载中...");
        dialog.show();

        /* 获取用户输入 */
        final String mPhotoUrl = getIntent().getStringExtra("data");
        final String mName = mLoginInputName.getText().toString();
        final String mPass = mLoginInputPass.getText().toString();
        final String mPhone = mLoginInputPhone.getText().toString();
        final String mCode = mLoginInputCode.getText().toString();

        if(mName == null || mPass == null || mPhone == null || mCode == null){
            dialog.close();
            SimpleToast.makeText(this,"必填项不能为空", Toast.LENGTH_SHORT).show();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String mPath= Configfile.REGISTE_INPUT_POST;
                RequestParams params=new RequestParams();
                params.addBodyParameter("name",mName);
                params.addBodyParameter("phone",mPhone);
                params.addBodyParameter("pass",mPass);
                params.addBodyParameter("code",mCode);
                if(mPhotoUrl != null){
                    params.addBodyParameter("photoUrl",mPhotoUrl);
                }else {
                    params.addBodyParameter("photoUrl","../../upload/default.jpg");
                }
                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        mPath,
                        params, new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Message msg=new Message();
                                    msg.what=2;
                                    msg.obj=result;
                                    mHandler.sendMessage(msg);
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Configfile.Log(mContext,"网络连接失败");
                            }
                        });
            }
        }).start();

    }


    //返回
    @OnClick({R.id.mSimpleBank})
    public void mSimpleBank(View v){
        String className = Bank.goBank(this, "ClientInputRegisteActivity");
        try {
            Class<? extends Activity> forName = (Class<? extends Activity>) Class.forName(className);
            startActivity(new Intent(this,forName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /* 用户头像更改 */
    @OnClick({R.id.mLoginInputPhoto})
    public void mSelctPhoto(View v){
        Intent intent=new Intent(this, UploadPhotoActivity.class);
        intent.putExtra("class",Configfile.PACKAGE+".registe.ClientInputRegisteActivity");
        intent.putExtra("servicepath","http://192.168.1.3/im/MyUploadFile");
        intent.putExtra("oldmgpath","http://b1.hucdn.com/upload/item/1806/13/05238622351985_800x800.jpg");
        intent.putExtra("filename","makvenis");
        startActivity(intent);

    }


    public void getparentData() {
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
        //储存返回的计划事件
        Bank.goTo(this,mClassName,"ClientInputRegisteActivity");
    }
}
