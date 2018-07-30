package com.makvenis.hotel.personalCentre;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.makvenis.hotel.activity.BaseActivity;
import com.makvenis.hotel.activity.MainActivity;
import com.makvenis.hotel.registe.LoginActivity;
import com.makvenis.hotel.tools.Books;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.utils.Bank;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import json.makvenis.com.mylibrary.json.tools.UploadPhotoActivity;
import json.makvenis.com.mylibrary.json.view.SimpleDialogView;
import json.makvenis.com.mylibrary.json.view.SimpleImageViewCircleBitmap;

/**
 * @解析 用户个人中心
 *
 *
 *
 */

@ContentView(R.layout.activity_personal)
public class PersonalActivity extends BaseActivity {

    /* 获取父类传递参数ClassName */
    private String mClassName;

    /* Context */
    public final Context mContext = PersonalActivity.this;
    /* package 地址 */
    public final String mPackage = Configfile.PACKAGE+".personalCentre.PersonalActivity";

    /* 获取include控件 start */
    @ViewInject(R.id.mSimpleBank)
    ImageView mSimpleBank;

    @ViewInject(R.id.mPublicTitle)
    TextView mPublicTitle;
    /* 获取include控件 end */

    /* 用户头像获取 */
    @ViewInject(R.id.mPersonalItemImage)
    SimpleImageViewCircleBitmap mPersonalItemImage;
    /* 用户姓名 */
    @ViewInject(R.id.mPersonalName)
    TextView mPersonalName;
    /* 用户等级 */
    @ViewInject(R.id.mPersonalLev)
    TextView mPersonalLev;
    /* 获取退出按钮 */
    @ViewInject(R.id.mPersonalItemOver)
    Button mPersonalItemOver;
    /* 获取买单按钮 */
    @ViewInject(R.id.ll2)
    LinearLayout ll2;        

    /* [remark] 因为这个头像地址可能会多次使用 用户头像地址 */
    String photoUrl;
    /* [remark] 因为这个用户名称可能会多次使用 用户名称 */
    String username;
    /* [用户信息][remark] 老数据库查询的数据 在更新的时候可能用到 */
    String mUserData;
    /* [Handler] 线程通讯 */
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 3011:
                    String obj = (String) msg.obj;
                    Log.e("LOGO",mPackage+"在id=3001处数据为["+obj+"]");
                    if(obj != null){
                        try {
                            JSONObject object=new JSONObject(obj);
                            String state = object.optString("state");
                            if(state.equals("ok")){
                                Log.e("LOGO",mPackage+"在更新头像地址数据为["+
                                        Configfile.PATH+"/upload/"+username+".jpg"+"]");
                                Picasso.with(mContext)
                                        .load(Configfile.PATH+"/upload/"+username+".jpg")
                                        .into(mPersonalItemImage);
                                // TODO: 2018/7/24 修正不及时更新
                                Configfile.Log(mContext,"更新成功");
                            }else {
                                Configfile.Log(mContext,"更新失败"+object.opt("error"));
                            }
                            dialogView.close();
                        } catch (JSONException e) {
                            new IllegalArgumentException(Configfile.jsonException("[Handler 解析出]"
                                    +mPackage));
                        }
                    }else {
                        Configfile.Log(mContext,"无数据返回");
                        dialogView.close();
                    }
                    break;
                case 3013:
                    /* 获取等待结账桌位信息 */
                    String mPathNumData = (String) msg.obj;
                    if(mPathNumData != null){
                        try {
                            JSONObject object=new JSONObject(mPathNumData);
                            JSONArray data = object.getJSONArray("data");
                            if(data.length() > 0){
                                String[] mClass=new String[data.length()];
                                String[] mPathNumber=new String[data.length()];
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject mDataObj = data.getJSONObject(i);
                                    mClass[i]=mDataObj.optString("pathName");
                                    mPathNumber[i]=mDataObj.optString("pathNumber");
                                }
                                /* 显示dialog */
                                ShowChoise(mClass,mPathNumber);
                            }else {
                                Configfile.Log(mContext,"暂无需要买单桌位");
                            }

                        } catch (JSONException e) {
                            new IllegalArgumentException(Configfile.jsonException(mPackage+"Handler case 3013"));
                        }
                    }
                    dialogView.close();
                    break;

            }
        }
    };
    /* [Dialog] 全局dialog */
    private SimpleDialogView dialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ViewUtils.inject(this);

        /* 获取父类参数 */
        getParentData();

        /* 赋值预设参数 */
        setting();

        /* [remark] 可能null 返回修改的头像地址 */
        String data = getIntent().getStringExtra("data");
        if(data != null){
            /* 启动更新服务 */
            dialogView = new SimpleDialogView(mContext,"更新用户数据...");
            dialogView.show();
            Log.e("LOGO","上传头像返回的数据["+data+"]");
            /* 解析返回的数据 */
            try {
                JSONObject object=new JSONObject(data);
                final String bank = object.optString("bank");
                if(bank != "") {
                    Log.e("LOGO", "[开始执行上传]");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String mPath = Configfile.EXECUTE_SQL_SERVICE + "?book=" + Books.PATH_UPLOAD_USER_PHOTO_BOOK +
                                    "&values=" + bank + "," + username;
                            Log.e("LOGO", "GET 上传路劲为["+mPath+"]");
                            new HttpUtils(15000).send(HttpRequest.HttpMethod.GET,
                                    mPath,
                                    new RequestCallBack<String>() {
                                        @Override
                                        public void onSuccess(ResponseInfo<String> responseInfo) {
                                            String result = responseInfo.result;
                                            if (result != null) {
                                                Message msg = new Message();
                                                msg.what = 3011;
                                                msg.obj = result;
                                                mHandler.sendMessage(msg);
                                                Log.e("LOGO", mPackage + "在HttpUtils返回数据为[" + result + "]");
                                            }
                                        }

                                        @Override
                                        public void onFailure(HttpException e, String s) {
                                            Configfile.Log(mContext, "链接服务失败！");
                                            dialogView.close();
                                        }
                                    });
                        }
                    }).start();
                }
            } catch (JSONException e) {
                new IllegalArgumentException(Configfile.jsonException(mPackage));
            }

        }

    }



    /* [预设参数] 赋值预设参数（标题名称等） */
    private void setting() {
        /* 设置标题 */
        mPublicTitle.setText("个人中心");
        /* 设置头像、姓名、等级等信息 */
        List<Map<String, Object>> mUser = getUserData();
        if(mUser != null){
            try {
                mUserData = (String) mUser.get(0).get("data");
                JSONObject object=new JSONObject(mUserData);
                JSONArray array = object.getJSONArray("data");
                JSONObject obj = array.getJSONObject(0);
                /* 图片地址 */
                photoUrl = obj.optString("photoUrl").replace("../..", Configfile.PATH);
                Log.e("LOGO","用户头像地址["+photoUrl+"]");
                /* 名称 */
                username = obj.optString("username");
                /* 等级 */
                String userlevel = obj.optString("userlevel");

                /* 设置用户头像 */
                Picasso.with(mContext)
                        .load(photoUrl)
                        .placeholder(R.drawable.icon_default)
                        .error(R.drawable.icon_default)
                        .skipMemoryCache()
                        .resize(70,70)
                        .centerCrop()
                        .into(mPersonalItemImage);
                /* 设置用户名称 */
                mPersonalName.setText(username);
                /* 设置用户等级 */
                mPersonalLev.setText(userlevel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

    /* [父类] 获取父类参数 */
    public void getParentData() {
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
        Bank.goTo(mContext,mClassName,"PersonalActivity");
    }

    /* [特定返回] 返回到首页界面（MainActivity.Class） */
    @OnClick({R.id.mSimpleBank})
    public void mSimpleBank(View v){
        startActivity(new Intent(mContext, MainActivity.class));
    }

    /* [点击事件] 设置头像点击事件 */
    @OnClick({R.id.mPersonalItemImage})
    public void mPersonalItemImage(View v){
        /* 更改用户头像固定使用参数 */
        Intent intent=new Intent(this, UploadPhotoActivity.class);
        intent.putExtra("class",mPackage);     //提供返回的源头
        intent.putExtra("servicepath",Configfile.UPLOAD_FILE_PATH);//提供文件上传地址
        intent.putExtra("oldmgpath",photoUrl); //提供显示旧头像地址
        intent.putExtra("filename",username);  //上传文件的名称
        startActivity(intent);
    }
    
    /* [点击事件] 买单 */
    @OnClick({R.id.ll2})
    public void ll2(View v){
        dialogView = new SimpleDialogView(mContext,"");
        dialogView.show();
        getNetCashPatnNum();
    }

    /* [获取待结账的桌位] */
    public void getNetCashPatnNum(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestParams params=new RequestParams();
                params.addBodyParameter("book", Books.PATH_SELECT_WAITING_CASH_BOOK);
                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        Configfile.EXECUTE_SQL_SERVICE,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Message msg=new Message();
                                    msg.what=3013;
                                    msg.obj=result;
                                    mHandler.sendMessage(msg);
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                if(dialogView != null){
                                    dialogView.close();
                                }
                                Configfile.Log(mContext,"网络获取失败！");
                            }
                        });

            }
        }).start();
    }

    /* [点击事件][remark] dialog选择框 选择需要结账的桌位 */
    private void ShowChoise(String[] className,final String[] mPathNumber){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,android.R.style.Theme_Holo_Light_Dialog);
        builder.setTitle("请选择");
        //  指定下拉列表的显示数据
        //  final String[] cities = {"广州", "上海", "北京", "香港", "澳门"};
        //  设置一个下拉的列表选择项
        builder.setItems(className, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which){
                /* 获取桌位编号 */
                try {
                    int value = Integer.valueOf(mPathNumber[which]).intValue();
                    Intent intent=new Intent(mContext,SettleAccountsActivity.class);
                    intent.putExtra("class",mPackage);
                    // TODO: 2018/7/25 先给定固定的座位号
                    intent.putExtra("pathNum",value);
                    startActivity(intent);
                }catch (NumberFormatException e){
                    new IllegalArgumentException(mPackage+"[#ShowChoise()]在转化数字时候发生异常 " +
                            "Integer.valueOf(mPathNumber[which]).intValue()");
                }
            }
        });
        builder.show();
    }

    /* [退出按钮] */
    @OnClick({R.id.mPersonalItemOver})
    public void mPersonalItemOver(View v){
        /* 删除数据库信息 */
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }


}
