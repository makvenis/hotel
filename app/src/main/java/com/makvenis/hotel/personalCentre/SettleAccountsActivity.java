package com.makvenis.hotel.personalCentre;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
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
import com.makvenis.hotel.tools.Books;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.xutils.DBHelp;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.ex.DbException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import json.makvenis.com.mylibrary.json.view.SimpleDialogView;

/* [结算中心] */
@ContentView(R.layout.activity_settle_accounts)
public class SettleAccountsActivity extends AppCompatActivity {

    /* 上下文 */
    public final Context mContext = SettleAccountsActivity.this;

    /* 传递的ClassName名称 */
    private String mClassName;

    /* package名称 */
    public String mPackage = Configfile.PACKAGE+".personalCentre.SettleAccountsActivity";
    /* 买单座位号 */
    private int mPathNum;
    /* recycle */
    @ViewInject(R.id.mSettleRecycle)
    RecyclerView mRecycle;

    /* 返回按钮 */
    @ViewInject(R.id.mSimpleSettleBack)
    ImageView mSimpleSettleBack;
    /* 总价赋值 */
    @ViewInject(R.id.mMenuAllSale)
    TextView mMenuAllSale;
    /* 开始付款按钮 */
    @ViewInject(R.id.mMenuPost)
    Button mMenuPost;
    /* 标题赋值控件 */
    @ViewInject(R.id.mSettleTitleName)
    TextView mSettleTitleName;
    /* 刷新组件 */
    @ViewInject(R.id.mSettleSwipe)
    PullRefreshLayout mSettleSwipe;

    /* 全局处理handler事件 */
    public Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 3012:
                    if(data.size() != 0){
                        data.clear();
                        /* 总价也需要清零 */
                        mAllSaleCsh=0f;
                    }
                    String obj = (String) msg.obj;
                    if(obj != null){
                        standard(obj);
                        /* 页面参数设置、回调事件等 */
                        setting();
                        adapter.notifyDataSetChanged();
                    }else {
                        Configfile.Log(mContext,"数据加载失败！");
                    }
                    dialogView.close();
                    break;

                case 3014:
                    /* 说明远程数据库状态等更新完成 */
                    startActivity(new Intent(mContext, MainActivity.class));
                    break;
            }
        }
    };
    /* 全局dialog */
    private SimpleDialogView dialogView;

    /* 大集合 */
    List<SettleAccounstsEnrty> data=new ArrayList<>();

    /* 适配器 */
    private SettleAcconunsAdapter adapter;
    /* 清单列表大小 */
    private int mSize;
    /* 价格 */
    float mAllSaleCsh=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

        /* 父类参数 */
        getParmentData();
        /* 请求数据 */
        netDownData();
        /* 设置适配器 */
        settingAdapter();
        /* 刷新 */
        mSettleSwipe.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                netDownData();
                mSettleSwipe.setRefreshing(false);
            }
        });

    }

    /* [页面赋值] 页面参数设置 */
    private void setting() {
        //列表总数（也就是菜品总和）
        mSettleTitleName.setText(mPathNum+"桌清单列表("+mSize+")");
        BigDecimal bigDecimal=new BigDecimal(mAllSaleCsh);
        BigDecimal decimal = bigDecimal.setScale(2, BigDecimal.ROUND_UP);
        mMenuAllSale.setText("金额:"+decimal.floatValue()+" RMB");
    }

    /* [adapter] 设置适配器 */
    private void settingAdapter() {
        RecyclerView.LayoutManager manager=new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL,
                false);
        mRecycle.setLayoutManager(manager);
        adapter = new SettleAcconunsAdapter(data,mContext);
        mRecycle.setAdapter(adapter);

    }


    /* [获取参数] 获取父类传递参数 */
    public void getParmentData(){
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
        mPathNum = intent.getIntExtra("pathNum",0);
    }

    /* [数据格式] 处理下栽的数据格式 */
    public List<SettleAccounstsEnrty> standard(String net){
        try {

            JSONObject object=new JSONObject(net);
            JSONArray array = object.getJSONArray("data");
            mSize = array.length();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SettleAccounstsEnrty e=new SettleAccounstsEnrty();

                e.setAid(obj.optString("aid"));
                e.setAnum(obj.optString("anum"));
                e.setAsale(obj.optString("asale"));
                e.setsName(obj.optString("aclassName"));
                e.setPhotoUrl(obj.optString("photoUrl").replace("../..",Configfile.PATH));
                data.add(e);

                float anum = Float.parseFloat(obj.optString("anum"));
                float asale = Float.parseFloat(obj.optString("asale"));
                float mAllSale = asale * anum;
                mAllSaleCsh+=mAllSale;

            }
            return data;
        } catch (JSONException e) {
            new IllegalArgumentException(Configfile.jsonException(mPackage+"#standard()"));
        }
        return new ArrayList<>();
    }

    /* [请求数据] */
    public void netDownData(){
        dialogView = new SimpleDialogView(mContext,"获取账单中...");
        dialogView.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestParams params=new RequestParams();
                params.addBodyParameter("book", Books.PATH_PAY_BOOK);
                /* 座位号4，状态1（未结账）清单 */
                params.addBodyParameter("values",mPathNum+",1");
                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        Configfile.EXECUTE_SQL_SERVICE,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Message msg=new Message();
                                    msg.what=3012;
                                    msg.obj=result;
                                    mHandler.sendMessage(msg);
                                }
                            }
                            @Override
                            public void onFailure(HttpException e, String s) {
                                Configfile.Log(mContext,"网络链接失败！");
                                dialogView.close();
                            }
                        });
            }
        }).start();
    }

    /* [特定返回] 返回到个人中心页面 */
    @OnClick({R.id.mSimpleSettleBack})
    public void mSimpleSettleBack(View v){
        startActivity(new Intent(mContext,PersonalActivity.class));
    }

    /* [点击事件] 开始付款 */
    @OnClick({R.id.mMenuPost})
    public void mMenuPost(View v){
        // TODO: 2018/7/25 选择付款方式
        bottonWindow(v);
    }

    /* [结算成功] 客户已付款 */
    public void uploadCoffeeNetAndLocalData(){
        /* 跟新本地信息以及远程数据 */
        uploadHostState();
        uploadLocalState();
    }

    /* [更新本地数据库] 更新本地桌号为pathNum状态=0 */
    public void uploadLocalState(){
        try {
            String mSql = "update listCoffee set zhuangtai=0 where pathNum="+mPathNum;
            DbManager db = DBHelp.initDb();
            db.executeUpdateDelete(new SqlInfo(mSql));
            if(dialogView != null){
                dialogView.close();
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /* [更新远程数据库] 更新本地桌号为pathNum状态=0 */
    public void uploadHostState(){
        /* 更新付款信息和座位带清理信息 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* 更新远程数据库状态=0 */
                RequestParams params=new RequestParams();
                params.addBodyParameter("book",Books.PATH_UPLOAD_STATE_BOOK);
                params.addBodyParameter("values",mPathNum+"");
                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        Configfile.EXECUTE_SQL_SERVICE,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    try {
                                        JSONObject object=new JSONObject(result);
                                        String state = object.optString("state");
                                        if(state.equals("ok")){
                                            if(dialogView != null){
                                                dialogView.close();
                                            }
                                            Configfile.Log(mContext,"[付款]更新成功");
                                        }else {
                                            if(dialogView != null){
                                                dialogView.close();
                                            }
                                            Configfile.Log(mContext,"[付款]更新失败");
                                        }
                                    } catch (JSONException e) {
                                        new IllegalArgumentException(Configfile.jsonException(
                                                mPackage+"[#uploadHostlState()]"));
                                    }
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                if(dialogView != null){
                                    dialogView.close();
                                }
                                Configfile.Log(mContext,"网络链接失败！");
                            }
                        });

                /* 更新带清理座位 */
                RequestParams params1=new RequestParams();
                params1.addBodyParameter("book",Books.PATH_UPLOAD_PATHNUM_STATE_BOOK);
                params1.addBodyParameter("values",mPathNum+"");
                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        Configfile.EXECUTE_SQL_SERVICE,
                        params1,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    try {
                                        JSONObject object=new JSONObject(result);
                                        String state = object.optString("state");
                                        if(state.equals("ok")){
                                            if(dialogView != null){
                                                dialogView.close();
                                            }
                                            Configfile.Log(mContext,"[桌位]更新成功");
                                        }else {
                                            if(dialogView != null){
                                                dialogView.close();
                                            }
                                            Configfile.Log(mContext,"[桌位]更新失败");
                                        }
                                    } catch (JSONException e) {
                                        new IllegalArgumentException(Configfile.jsonException(
                                                mPackage+"[#uploadHostlState()]"));
                                    }
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                new IllegalArgumentException(Configfile.jsonException(
                                        mPackage+"[#uploadHostlState()]"));
                            }
                        });
                mHandler.sendEmptyMessage(3014);
            }

        }).start();
    }

    /* [广告窗口]  */
    private void advPopuwindow(final String cashType) {
        dialogView = new SimpleDialogView(mContext,"加载中...");
        dialogView.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = View.inflate(mContext, R.layout.include_adv_popuwindow, null);
                            ImageView mImg_first = (ImageView) view.findViewById(R.id.mImg_first);
                            if(cashType.equals("wx")){ //微信
                                Picasso.with(mContext).load(Configfile.PATH+"/upload/wx.png").into(mImg_first);
                                dialogView.close();
                            }else if(cashType.equals("zfb")){
                                Picasso.with(mContext).load(Configfile.PATH+"/upload/zfb.png").into(mImg_first);
                                dialogView.close();
                            }else if(cashType.equals("cash")){
                                Configfile.Log(mContext,"请移步到收银台");
                                dialogView.close();
                            }else {
                                return;
                            }


                            LinearLayout mLine = (LinearLayout) view.findViewById(R.id.mLine);
                            final PopupWindow mPopu = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT);
                            mPopu.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                            mLine.getBackground().setAlpha(100);
                            mLine.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mPopu.dismiss();
                                }
                            });
                            mImg_first.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mPopu.dismiss();
                                }
                            });
                            mPopu.setOutsideTouchable(true);//判断在外面点击是否有效
                            mPopu.setFocusable(true);
                            mPopu.showAsDropDown(view);

                            mPopu.setOnDismissListener(new PopupWindow.OnDismissListener() {

                                @Override
                                public void onDismiss() {
                                    /* 消失之后调用 */
                                    dialogView = new SimpleDialogView(mContext,"更新数据...");
                                    dialogView.show();
                                    uploadCoffeeNetAndLocalData();
                                }
                            });

                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /* [PopupWindow窗口] 选择付款方式 */
    /* PopWindows 的操作等（开始） */
    /**
     * @ 解释直接调用 bottomwindow(view)方法
     * @ 解释 详细的不揍操作在方法setButtonListeners()里面
     */
    private PopupWindow popupWindow;
    //使用PopWindows的时候需要给定当前的View
    public void bottonWindow(View view) {

        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.include_layout_popuwindow, null);
        popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.BOTTOM, 0, -location[1]);
        //添加按键事件监听
        setButtonListeners(layout);
        //添加pop窗口关闭事件，主要是实现关闭时改变背景的透明度
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        backgroundAlpha(0.5f);
    }
    //渐变通道
    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }
    //PopWindows的控件全局变量
    private LinearLayout callBank, wx,cash,zhb;
    //PopWindows的控件的点击事件
    public void setButtonListeners(LinearLayout view) { //
        callBank = (LinearLayout) view.findViewById(R.id.mHistory_pop_over); //退出
        wx = (LinearLayout) view.findViewById(R.id.mCrop_local_wx);  //微信
        zhb = (LinearLayout) view.findViewById(R.id.mCrop_local_zhb);  //支付宝
        cash = (LinearLayout) view.findViewById(R.id.mCrop_local_cash);  //支付宝

        /* 启动微信 */
        wx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advPopuwindow("wx");
                popupWindow.dismiss();
            }
        });

        /* 启动支付宝 */
        zhb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advPopuwindow("zfb");
                popupWindow.dismiss();
            }
        });

        /* 启动现金结算 */
        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            advPopuwindow("cash");
            popupWindow.dismiss();
            }
        });

        /* 取消操作 */
        callBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    /* [PopWindow] 的操作等 （结束） */

}
