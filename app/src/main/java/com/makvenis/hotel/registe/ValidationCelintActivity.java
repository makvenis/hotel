package com.makvenis.hotel.registe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dalimao.corelibrary.VerificationCodeInput;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.makvenis.hotel.R;
import com.makvenis.hotel.activity.MainActivity;
import com.makvenis.hotel.tools.Configfile;

import org.json.JSONException;
import org.json.JSONObject;


/* 验证码验证输入，以及电话的验证 */
@ContentView(R.layout.activity_validation_celint)
public class ValidationCelintActivity extends AppCompatActivity {


    public final Context mContext = ValidationCelintActivity.this;

    @ViewInject(R.id.mValidationShow)
    TextView mValidationShow;

    /* 验证码输入框 */
    @ViewInject(R.id.mVerificationCodeInput)
    VerificationCodeInput mVerificationCodeInput;

    @ViewInject(R.id.mll)
    LinearLayout mll;

    /* 倒计时 */
    @ViewInject(R.id.mDownTime)
    TextView mDownTime;

    /* 检测验证码的回调结果（验证码过期或者输入错误） */
    @ViewInject(R.id.mValidationError)
    TextView mValidationError;

    //传递的父类
    private String mClassName;

    //电话（参与验证的电话）
    private String mPhone;

    //返回参数
    @ViewInject(R.id.mSimpleBank)
    private ImageView mSimpleBank;

    //倒计时
    CountDownTimer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

        Log.e("LOG","进入---ValidationCelintActivity---进入验证码输入页面");

        /* 接收上层父类传递的值 */
        getParentData();

        /* 倒计时 */
        mTimer = new CountDownTimer(15*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int i = (int) (millisUntilFinished / 1000);
                // TODO: 2018/7/2 改变颜色
                mDownTime.setTextColor(0xffe8035f);
                mDownTime.setEnabled(false);
                mDownTime.setText(i + "S");
            }

            @Override
            public void onFinish() {
                mDownTime.setText("没有收到验证码？重新获取");
                mDownTime.setTextColor(Color.BLUE);
                mDownTime.setEnabled(true);
            }
        }.start();


        String html = "已向<font color='#383838'>"+mPhone+"</font>用户发送注册验证码，请查看短信并且输入验证码";
        mValidationShow.setText(Html.fromHtml(html));

        mVerificationCodeInput.setOnCompleteListener(new VerificationCodeInput.Listener() {
            @Override
            public void onComplete(String s) {
                /* 输入完成 */
                if(mll.getVisibility() == View.GONE){
                    mll.setVisibility(View.VISIBLE);
                }
                /* 获取验证码 去验证 */
                validationCode(s);
            }
        });
    }

    public void getParentData() {
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
        mPhone = intent.getStringExtra("phone");
    }


    @OnClick({R.id.mDownTime})
    public void mDownTime(View v){
        ServiceUtils.getPostCodeToPhone(mPhone,mContext,mHandler);
        Configfile.Log(mContext,"验证码已发送");
        mTimer.start();

    }

    //返回上一级界面（1）
    @OnClick({R.id.mSimpleBank})
    public void mSimpleBank(View v){
        Class<? extends Activity> forName = null;
        try {
            forName = (Class<? extends Activity>) Class.forName(mClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this,forName));
    }

    public Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            String obj = (String) msg.obj;
            switch (what){
                case 0:
                    try {
                        Log.e("LOG","case 0 --- mHandler接收"+obj);
                        JSONObject jsonObject = new JSONObject(obj);
                        String state = jsonObject.optString("state");
                        String message = jsonObject.optString("msg");
                        if(state.equals("ok")){
                            // TODO: 2018/7/2 验证成功
                            mValidationError.setText(message);
                            startActivity(new Intent(mContext, MainActivity.class));
                            finish();
                        }else {
                            if(mValidationError.getVisibility() == View.GONE){
                                mValidationError.setVisibility(View.VISIBLE);
                                mValidationError.setText(message);
                                mll.setVisibility(View.GONE);
                            }

                        }
                    } catch (JSONException e) {
                        new IllegalArgumentException("解析失败 不是一个合格json格式--- "+obj);
                    }
                    break;
                case 1:
                    try {
                        Log.e("LOG","case 1 --- mHandler接收"+obj);
                        JSONObject jsonObject = new JSONObject(obj);
                        String state = jsonObject.optString("state");
                        String message = jsonObject.optString("msg");
                        if(state.equals("ok")){
                            // TODO: 2018/7/2 验证成功
                            mValidationError.setText(message);
                            startActivity(new Intent(mContext, MainActivity.class));
                        }else {
                            if(mValidationError.getVisibility() == View.GONE){
                                mValidationError.setVisibility(View.VISIBLE);
                                mValidationError.setText(message);
                                mll.setVisibility(View.GONE);
                            }

                        }
                    } catch (JSONException e) {
                        new IllegalArgumentException("解析失败 不是一个合格json格式--- "+obj);
                    }
                    break;
            }
        }
    };

    //验证短信
    public void validationCode(String code){
        String path = Configfile.VALIDATION + "?code=" + code + "&phone=" + mPhone;

        new Thread().start();

        new HttpUtils(1000).send(HttpRequest.HttpMethod.GET,
                path,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String result = responseInfo.result;
                        if(result != null){
                            Message msg=new Message();
                            msg.what=0;
                            msg.obj=result;
                            mHandler.sendMessage(msg);
                            Log.e("LOG",result);
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        Configfile.Log(mContext,"连接失败");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        super.onLoading(total, current, isUploading);
                        //Log.e(this,"")
                    }
                });
    }
}
