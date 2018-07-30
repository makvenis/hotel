package com.makvenis.hotel.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.makvenis.hotel.listhotol.LocationSelectionActivity;
import com.makvenis.hotel.personalCentre.PersonalActivity;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.utils.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import json.makvenis.com.mylibrary.json.view.SimpleToast;
import json.makvenis.com.mylibrary.json.view.SimpleViewPage;
import json.makvenis.com.mylibrary.json.view.SimpleViewPageLikeAdapter;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    /* 全局上下文 */
    public final Context mContext = MainActivity.this;
    public final static String TAG="MainActivity";

    /* 在线预定 */
    @ViewInject(R.id.mMainActivity_yd)
    LinearLayout mMainActivity_yd;

    /* 消息 */
    @ViewInject(R.id.mMainActivity_message)
    LinearLayout mMainActivity_message;

    /* 外卖 */
    @ViewInject(R.id.mMainActivity_waimai)
    LinearLayout mMainActivity_waimai;

    /* 菜单 */
    @ViewInject(R.id.mMainActivity_caidan)
    LinearLayout mMainActivity_caidan;

    /* 更多 */
    @ViewInject(R.id.mMainActivity_gengduo)
    LinearLayout mMainActivity_gengduo;

    /* 喜欢 */
    @ViewInject(R.id.mMainActivity_like)
    LinearLayout mMainActivity_like;


    /* 幻灯片 */
    @ViewInject(R.id.mSimpleViewPage)
    SimpleViewPage mSimpleViewPage;


    /* 首页幻灯预备存储的值 */
    List<Map<String,String>> mHDData=new ArrayList<>();
    /* Handler */
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 101:
                    String mHd = (String) msg.obj;
                    if(mHd != null){
                        try {
                            JSONObject object=new JSONObject(mHd);
                            JSONArray data = object.getJSONArray("data");
                            List<Map<String, String>> maps = JSON.GetJson(data.toString(),
                                    new String[]{"id", "photoUrl", "like", "collection", "title"});

                            for (int i = 0; i < maps.size(); i++) {
                                Map<String, String> map = maps.get(i);
                                Map<String,String> mapAdd=new HashMap<>();
                                mapAdd.put("id",map.get("id"));
                                mapAdd.put("like",map.get("like"));
                                mapAdd.put("ok",map.get("collection"));
                                mapAdd.put("title",map.get("title"));
                                mapAdd.put("url",map.get("photoUrl").replace("../..",Configfile.PATH));
                                mHDData.add(mapAdd);
                            }
                            setHdPhoto(mHDData);
                        } catch (JSONException e) {
                            new IllegalArgumentException("在解析幻灯数据的时候，发现不是一个合格的json格式数据");
                        }
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
        /* 设置半透明 */
        mMainActivity_yd.getBackground().setAlpha(200);
        mMainActivity_message.getBackground().setAlpha(200);
        mMainActivity_caidan.getBackground().setAlpha(200);
        mMainActivity_gengduo.getBackground().setAlpha(200);
        mMainActivity_like.getBackground().setAlpha(200);
        mMainActivity_waimai.getBackground().setAlpha(200);



        setData();

    }

    /* [跳转] 设置点击事件--->菜单 */
    @OnClick({R.id.mMainActivity_caidan})
    public void caidan(View v){

        /* 获取当前类的class名称 */
        String mClassName = Configfile.PACKAGE+".activity.MainActivity";
        Intent intent=new Intent(mContext, LocationSelectionActivity.class);
        // TODO: 2018/7/18 模拟用户选择的座位号
        intent.putExtra("pathNum","100");
        intent.putExtra("class",mClassName);
        startActivity(intent);
    }

    /* [跳转] 设置点击事件--->个人中心 */
    @OnClick({R.id.mMainActivity_gengduo})
    public void gengduo(View v){
        /* 获取当前类的class名称 */
        String mClassName = Configfile.PACKAGE+".activity.MainActivity";
        Intent intent=new Intent(mContext, PersonalActivity.class);
        intent.putExtra("class",mClassName);
        startActivity(intent);
    }

    private void setData() {
        final String path = Configfile.HD_PATH+"?page=1&book=1";
        Log.e(TAG,path);
        new HttpUtils(10000).send(HttpRequest.HttpMethod.GET,
                path,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String result = responseInfo.result;
                        if(result != null){
                            Message msg=new Message();
                            msg.what=101;
                            msg.obj=result;
                            mHandler.sendMessage(msg);
                            Log.e(TAG,result);
                        }
                    }
                    @Override
                    public void onFailure(HttpException e, String s) {}
                });
    }

    /* 幻灯片（MAX） */
    private void setHdPhoto(List<Map<String,String>> mHDData) {

        SimpleViewPageLikeAdapter adapter = new SimpleViewPageLikeAdapter(mHDData, this);
        mSimpleViewPage.setAdapter(adapter);
        mSimpleViewPage.setShowTime(4000);
        mSimpleViewPage.setDirection(SimpleViewPage.Direction.LEFT);
        mSimpleViewPage.start();

        adapter.setOnclinkPageAdapterListener(new SimpleViewPageLikeAdapter.setOnClinkCheckListener() {
            @Override
            public void showLike(String mNum, int position, Map<String, String> map) {
                SimpleToast.makeText(mContext,"收藏成功", Toast.LENGTH_SHORT).show();
                String id = map.get("id");
                int i = Integer.valueOf(mNum).intValue();
                int k = i+1;
                final String mPath=Configfile.EXECUTE_SQL_SERVICE+"?book=2&values="+k+","+id;
                Log.e(TAG,mPath+"");
                /* 收藏 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new HttpUtils(10000).send(HttpRequest.HttpMethod.GET,
                                mPath,
                                new RequestCallBack<String>() {
                                    @Override
                                    public void onSuccess(ResponseInfo<String> responseInfo) {

                                    }

                                    @Override
                                    public void onFailure(HttpException e, String s) {

                                    }
                                });
                    }
                }).start();

            }

            @Override
            public void showCollection(String mNum, int position, Map<String, String> map) {
                SimpleToast.makeText(mContext,"点赞成功", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);

    }




}
