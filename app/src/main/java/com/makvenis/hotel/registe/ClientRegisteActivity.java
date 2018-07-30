package com.makvenis.hotel.registe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.makvenis.hotel.R;
import com.makvenis.hotel.tools.Configfile;


@ContentView(R.layout.activity_client_registe)
public class ClientRegisteActivity extends AppCompatActivity {

    @ViewInject(R.id.mRegistePhone)
    EditText mRegistePhone;

    /* 获取验证码按钮 */
    @ViewInject(R.id.mPostCode)
    Button mPostCode;

    /* 清除输入 */
    @ViewInject(R.id.mPostClean)
    ImageView mPostClean;

    /* 反射类名称 */
    private String mClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        Log.e("LOG","进入---LoginActivity---登陆页面");

        /* 获取父类传递的参数 */
        getParentData();
        Log.e("LOG","进入---LoginActivity---获取父类类名---"+mClassName);

        //清除按钮
        mPostClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configfile.Log(ClientRegisteActivity.this,"清除成功");
                //清除输入的电话
                mRegistePhone.setText("");
            }
        });
    }

    @OnClick({R.id.mPostCode})
    public void mRegisteValidation(View v){
        /* 跳转验证码输入框 */
        Intent intent=new Intent(this,ValidationCelintActivity.class);
        intent.putExtra("class", Configfile.PACKAGE+".registe.ClientRegisteActivity");
        intent.putExtra("phone",mRegistePhone.getText().toString());
        startActivity(intent);
        //
        ServiceUtils.getPostCodeToPhone(mRegistePhone.getText().toString(),this,new Handler());
    }

    //获取父类参数
    public void getParentData() {
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
    }

    //返回
    @OnClick({R.id.mSimpleBank})
    public void mSimpleBankClientRegisteActivity(View v){
        try {
            Class<? extends Activity> forName = (Class<? extends Activity>) Class.forName(mClassName);
            startActivity(new Intent(this,forName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
