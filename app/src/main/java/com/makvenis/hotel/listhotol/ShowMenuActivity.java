package com.makvenis.hotel.listhotol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.makvenis.hotel.tools.Books;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.utils.Bank;
import com.makvenis.hotel.xutils.DBHelp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.table.DbModel;
import org.xutils.ex.DbException;

import java.util.List;
import java.util.Map;

import json.makvenis.com.mylibrary.json.view.SimpleDialogView;

//查看当前的座位号点餐的详情有哪些？

@ContentView(R.layout.activity_show_menu)
public class ShowMenuActivity extends AppCompatActivity {

    /* 上下文 */
    public final Context mContext = ShowMenuActivity.this;

    @ViewInject(R.id.mPublicTitle)
    TextView mPublicTitle;
    /* include控件查找 END*/

    /* 桌号编号 */
    @ViewInject(R.id.mMenuPathNum)
    TextView mMenuPathNum;

    /* 继续点餐控件 */
    @ViewInject(R.id.mMenuContinue)
    LinearLayout mMenuContinue;

    /* Recycle控件 */
    @ViewInject(R.id.mRecycle_Show)
    RecyclerView mRecycle_Show;

    /* 底部总价格 和 提交订单按钮 */
    /* 总价格 */
    @ViewInject(R.id.mMenuAllSale)
    TextView mMenuAllSale;

    /* 订单提交按钮 */
    @ViewInject(R.id.mMenuPost)
    Button mMenuPost;
    /* 底部总价格 和 提交订单按钮 END */

    /* include start */
    @ViewInject(R.id.mSimpleBank)
    ImageView mSimpleBank;
    /* include end */


    private String mClassName;
    /* 当前客服的座位 */
    private int mPathNum;
    /* adapter */
    private ShowMenuActivityAdapter adapter;
    private PopupWindow mPopupWindow;
    /* dialog */
    private SimpleDialogView dialogView;


    /* 全局Handler */
    public Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 3002:
                    String obj = (String) msg.obj;
                    if(obj != null){
                        try {
                            JSONObject mJson = new JSONObject(obj);
                            String state = mJson.optString("state");
                            if(state.equals("ok")){
                                Configfile.Log(mContext,"提交成功");
                                /*  [为了防止重复提交] */
                                mMenuPost.setEnabled(false);
                            }else {
                                Configfile.Log(mContext,mJson.optString("msg"));
                            }
                        } catch (JSONException e) {
                            new IllegalArgumentException("不是一个合格的json");
                        }
                    }else {
                        Configfile.Log(mContext,"请求失败");
                    }
                    if(dialogView != null){
                        dialogView.close();
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
        /* 列表带有输入框的时候，不至于软键盘遮盖住输入框 */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ViewUtils.inject(this);

        /* 赋值以及获取父类参数 */
        getParentData();

        /* 赋值适配器 */
        settingAdapter();

        /* 回调数据 */
        adapter.OnClinkListener(new ShowMenuActivityAdapter.OnClinkShowMenuListener() {
            @Override
            public void showOperation(Map<String, Object> map, int position, View v) {
                String className = (String) map.get("className");
                String type = (String) map.get("type");
                deleteByConditions(className,type);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void showCallDatabaseSale(float mAllSale) {
                mMenuAllSale.setText("总价格:"+mAllSale+" RMB");
            }

            @Override
            public void showInputEdit(String var1) {
                Log.e("TAG","EditText 失去焦点的时候，回调结果"+var1);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    /* 设置适配器 */
    private void settingAdapter() {
        RecyclerView.LayoutManager manager=new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        mRecycle_Show.setLayoutManager(manager);
        adapter = new ShowMenuActivityAdapter(this,mPathNum);
        mRecycle_Show.setAdapter(adapter);
    }

    /* 获取父类参数 */
    public void getParentData() {
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
        mPathNum = intent.getIntExtra("pathNum",0);
        Bank.goTo(this,mClassName,"ShowMenuActivity");
        /* 赋值标题 */
        mPublicTitle.setText("核单");
        /* 座位号 */
        mMenuPathNum.setText("座位号:"+mPathNum+"号 VIP");
    }

    /* 继续点餐 */
    @OnClick({R.id.mMenuContinue})
    public void mMenuContinue(View v){
        /**
         * 继续点餐跳转到listCoffeeActivity页面
         */
        Intent intent=new Intent(this,ListCofferActivity.class);
        intent.putExtra("pathNum",mPathNum);
        intent.putExtra("class", Configfile.PACKAGE+".listhotol.ShowMenuActivity");
        startActivity(intent);
    }

    /* [删除] 删除数据中数据，代当前用户不需要的商品或者商品数目购选错误 */
    public void deleteByConditions(String mClassName, String type){
        try {
            String mSql = "delete from listCoffee where pathNum="+mPathNum+" and className='?'" +
                    " and type='?' and zhuangtai=1";
            DbManager db = DBHelp.initDb();
            String mSqlite = connationSqlite(mSql, new String[]{mClassName, type});
            Log.e("TAG","\n在执行删除"+mSqlite);
            db.executeUpdateDelete(new SqlInfo(mSqlite));
        } catch (DbException e) {
            new IllegalArgumentException("再执行删除 ShowMenuActivity #deleteByConditions()" +
                    "错误 --->["+mClassName+"]["+type+"]"+e.toString());
        }
    }


    /* [拼接] 拼接Sqlite语句 */
    public String connationSqlite(String mSql,String[] str) {
        if(str == null) { return mSql; }
        String news="";
        String[] by = mSql.split("");
        int h=0;
        for (int i = 0; i < by.length; i++) {
            if(by[i].equals("?")) {
                String k =by[i].replace("?", str[h]);
                news+=k;
                h++;
            }else {
                news+=by[i];
            }
        }
        System.out.print(news);
        return news;
    }


    /* [提交] 提交订单 */
    @OnClick({R.id.mMenuPost})
    public void postMenu(View v){
        /* 将这个座位的所有点餐内容全部上传到服务器 */
        dialogView = new SimpleDialogView(mContext,"提交订单中..");
        dialogView.show();

        /* [提交当前座位状态] */
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* 创建字符串json */
                RequestParams params = new RequestParams();
                params.addBodyParameter("book", Books.PATH_ZHUANGTAI_BOOK);
                params.addBodyParameter("values","1,"+mPathNum+"");
                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        Configfile.EXECUTE_SQL_SERVICE,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Configfile.Log(mContext,"状态改变成功！");
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Configfile.Log(mContext,"网络链接失败");
                                dialogView.close();
                            }
                        });

            }
        }).start();


        /* [提交订单] */
        /* 生成json字符串 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                String s = createJsonByDataBase();
                Log.e("TAG","\n"+s);
                RequestParams params=new RequestParams();
                params.addBodyParameter("book","5");
                params.addBodyParameter("json",s);
                new HttpUtils()
                        .configCurrentHttpCacheExpiry(10000)
                        .send(HttpRequest.HttpMethod.POST,
                        Configfile.POST_INSERT_SERVICE,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Message msg=new Message();
                                    msg.obj=result;
                                    msg.what=3002;
                                    mHandler.sendMessage(msg);
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Configfile.Log(mContext,"网络链接失败");
                                dialogView.close();
                            }
                        });
            }
        }).start();
    }

    /* [创建JSON] 根据数据库的内容生成json字符串 */
    public String createJsonByDataBase(){
        //查询数据库
        //select className,num,sale,type from listCoffee where pathNum=? and zhuangtai =1
        String mSql = "select cid,className,type,num,sale,pathNum,zhuangtai,fid from listCoffee " +
                "where pathNum=? and zhuangtai=1";
        DbManager db = DBHelp.initDb();
        String sqlite = connationSqlite(mSql, new String[]{mPathNum+""});
        Log.e("TAG","\n"+sqlite);
        try {
            List<DbModel> models = db.findDbModelAll(new SqlInfo(sqlite));
            Log.e("TAG","\n"+models.size());
            JSONArray array=new JSONArray();
            for (int i = 0; i < models.size(); i++) {
                JSONObject object=new JSONObject();
                DbModel dbModel = models.get(i);
                //cid,className,type,num,sale,pathNum,zhuangtai
                //获取值
                int cid = dbModel.getInt("cid");
                String className = dbModel.getString("className");
                String type = dbModel.getString("type");
                int num = dbModel.getInt("num");
                float sale = dbModel.getFloat("sale");
                int pathNum = dbModel.getInt("pathNum");
                int zhuangtai = dbModel.getInt("zhuangtai");
                int fid = dbModel.getInt("fid");
                //赋值
                object.put("cid",cid);
                object.put("className",className);
                object.put("type",type);
                object.put("sale",sale);
                object.put("num",num);
                object.put("pathNum",pathNum);
                object.put("zhuangtai",zhuangtai);
                object.put("fid",fid);
                //添加
                array.put(object);
            }
            String data = array.toString();
            return data;
        } catch (DbException e) {
            new IllegalArgumentException("在执行查询 ShowMenuActivity #creatJsonByDataBase()" +
                    "发生错误"+e.toString()+"["+sqlite+"]");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* [特定返回] 返回上一级 */
    @OnClick({R.id.mSimpleBank})
    public void mSimpleBank(View v){
        /* 特定返回到某一个页面 */
        startActivity(new Intent(mContext,ListCofferActivity.class));
    }

}
