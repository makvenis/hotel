package com.makvenis.hotel.listhotol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.utils.Bank;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContentView(R.layout.activity_list_coffer)
public class ListCofferActivity extends AppCompatActivity {

    /* 左侧导航的按钮 */
    @ViewInject(R.id.mCoffee_Recycle)
    RecyclerView mCoffee_Recycle;

    /* 气温度数 */
    @ViewInject(R.id.mCoffee_weather_text)
    TextView mCoffee_weather_text; //天气的晴朗度

    /* 间日天气概况 */
    @ViewInject(R.id.mCoffee_time_text)
    TextView mCoffee_time_text;

    /* 获取头部返回文件 */
    @ViewInject(R.id.mSimpleBank)
    ImageView mSimpleBank;
    /* 获取头部标题 */
    @ViewInject(R.id.mPublicTitle)
    TextView mPublicTitle;
    /* 价格总数 */
    @ViewInject(R.id.mAllSale)
    TextView mAllSale;

    public FragmentManager manager;

    /* 全局上下文 */
    public final Context mContext=ListCofferActivity.this;
    private LeftRadioButtonAdapter mAdapter;

    /* 左侧栏目全局装载 */
    List<LeftEntry> mEntryData=new ArrayList<>();

    /* 接收类名称 */
    private String mClassName;

    /* 右侧FrameLayout */
    @ViewInject(R.id.fl)
    FrameLayout fl;

    /* 查看搜点餐清单 */
    @ViewInject(R.id.mAllSaleShow)
    Button mAllSaleShow;

    /* 用户选择的座位号 */
    private int mPathNum;

    /* [失败] 默认首先加载的第一个碎片数据的Cid */
    private String mDefaultCid = "1";

    public Handler mHandler=new Handler(){


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 2: //天气信息
                    String weather = (String) msg.obj;
                    if(weather != null){
                        /* 解析天气信息 */
                        try {
                            JSONObject object=new JSONObject(weather);
                            JSONObject data = object.getJSONObject("data");
                            JSONObject yesterday = data.getJSONObject("yesterday");
                            String date = yesterday.optString("date");
                            String high = yesterday.optString("high").replace("高温 ", "");;
                            String low = yesterday.optString("low").replace("低温 ", "");
                            String type = yesterday.optString("type");
                            //String notice = yesterday.optString("notice");

                            mCoffee_weather_text.setText(type+" "+low+"~"+high);
                            SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月");
                            String format = sdf.format(new Date());
                            mCoffee_time_text.setText(format+date+"");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 201: //栏目名称获取
                    String mClassName = (String) msg.obj;
                    if(mClassName != null){
                        try {
                            JSONObject object=new JSONObject(mClassName);
                            JSONArray data = object.getJSONArray("data");
                            //默认获取数组第一个对象CID
                            mDefaultCid = data.getJSONObject(0).optString("cid");
                            //LeftEntry
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject opt = data.getJSONObject(i);
                                LeftEntry e=new LeftEntry();
                                e.setNameEng(opt.optString("keywords"));
                                Log.e("DATA","赋值实体类[LeftEntry]");
                                e.setNameCn(opt.optString("classname"));
                                e.setNum(Integer.valueOf(opt.optString("cid")).intValue());
                                mEntryData.add(e);
                            }
                            mAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            new IllegalArgumentException("在解析栏目名称的时候，发现不是一个合格的json格式数据"+e.toString());
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
        /* 获取父类传递的参数 */
        getParentData();
        /* 获取数据 */
        getClassName();
        /* 绑定左侧栏目适配器 */
        settingAdapter();
        /* 天气设置 */
        setWeather();
        /* 价格的广播事件 */


        /* 默认加载第一个碎片 */
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl, SimpleFragment.newInstance(mDefaultCid));
        transaction.commit();


    }

    @Override
    protected void onStart() {
        super.onStart();
        /* 广播注册 */
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* 广播销毁 */
        EventBus.getDefault().unregister(this);
    }

    /* 将几个碎片的价格总和置于map集合中 */
    Map<String,String> mClassSale = new HashMap<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saleCheck(SaleMessage msg){
        String msgCid = msg.getCid();
        String msgSale = msg.getSale();
        mClassSale.put(msgCid,msgSale);
        //计算总合
        float v = allSaleMath();
        //共计:0RMB
        mAllSale.setText("共计:"+v+"RMB");
    }

    private void setWeather() {
        /* 地理位置获取 */
        // TODO: 2018/7/8  地理位置获取
        final String mPath= Configfile.WEATHER_DATA+"广元";
        /* 下载数据 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                new HttpUtils(10000).send(HttpRequest.HttpMethod.GET,
                        mPath,
                        new RequestCallBack<String>() {
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
                                Configfile.Log(mContext,"获取天气信息失败");
                            }
                        });
            }
        }).start();

    }

    /* [adapter] 适配器设置以及回调事件 */
    private void settingAdapter() {
        RecyclerView.LayoutManager manager=new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        mCoffee_Recycle.setLayoutManager(manager);
        mAdapter = new LeftRadioButtonAdapter(mEntryData,mContext);
        mCoffee_Recycle.setAdapter(mAdapter);

        mAdapter.setCallBankItemCheck(new LeftRadioButtonAdapter.CallBankItemCheck() {
            @Override
            public void show(int cid,int position) {
                /* 当前栏目CID */
                mAdapter.setThisPosition(position);
                mAdapter.notifyDataSetChanged();

                try {
                    /* 默认加载第position个碎片 */
                    FragmentManager manager=getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    /* 传递基础数据到工厂类Fragment */
                    // TODO: 2018/7/22 当前传递是cid 和 座位号 （后期可能会更多）
                    JSONObject object=new JSONObject();
                    /* Fragment下载数据的Cid String */
                    object.put("cid",cid+"");
                    /* 当前客户点餐的座位号 int */
                    object.put("pathNum",mPathNum);
                    transaction.replace(R.id.fl, SimpleFragment.newInstance(object.toString()));
                    transaction.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* [获取父类参数] 获取父类传递的参数 */
    private void getParentData() {
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
        /* 获取用户选择的座位号 */
        mPathNum = intent.getIntExtra("pathNum",0);
        //调用存储
        Bank.goTo(mContext,mClassName,"ListCofferActivity");
    }

    /* [求和] 求和价格 */
    private float allSaleMath(){
        float mAll=0;
        for (String saleStr:mClassSale.values()) {
            /* 替换 */
            String replace = saleStr.replace("共计:", "").replace("RMB", "");
            float v = Float.valueOf(replace).floatValue();
            mAll+=v;
        }
        /*  */
        BigDecimal big=new BigDecimal(mAll);
        return big.setScale(2,BigDecimal.ROUND_HALF_EVEN).floatValue();
    }

    /* [返回] 返回上一级 */
    @OnClick({R.id.mSimpleBank})
    public void bankAcyivity(View v){
        try {
            /* 获取返回的类 */
            String bankClassName = Bank.goBank(mContext, "ListCofferActivity");
            Class<? extends Activity> forName = (Class<? extends Activity>) Class.forName(bankClassName);
            /* 这是特定返回 意思就是这个页面只允许返回到某一个页面 */
            Intent intent=new Intent(mContext,LocationSelectionActivity.class);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* [点击事件] 查看所订购的商品价格 */
    @OnClick({R.id.mAllSale})
    public void showMenu(View v){}

    /* [点击事件] 查看所订购的商品 */
    @OnClick({R.id.mAllSaleShow})
    public void mAllSaleShow(View v){
        Intent intent=new Intent(mContext, ShowMenuActivity.class);
        intent.putExtra("class",Configfile.PACKAGE+".listhotol.ListCofferActivity");
        //座位编号
        intent.putExtra("pathNum",mPathNum);
        startActivity(intent);
    }

    /* [数据下载] 获取物品种类 */
    public void getClassName() {

        final RequestParams params=new RequestParams();
        params.addBodyParameter("book","3");
        new Thread(new Runnable() {
            @Override
            public void run() {
                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        Configfile.EXECUTE_SQL_SERVICE,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Message msg=new Message();
                                    msg.what=201;
                                    msg.obj=result;
                                    mHandler.sendMessage(msg);
                                    Log.e("DATA","[book=3]下载数据为-->\n"+result);
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {

                            }
                        });
            }
        }).start();
    }
}
