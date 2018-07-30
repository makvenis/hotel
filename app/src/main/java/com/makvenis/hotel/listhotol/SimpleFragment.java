package com.makvenis.hotel.listhotol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baoyz.widget.PullRefreshLayout;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.makvenis.hotel.R;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.tools.NetworkTools;
import com.makvenis.hotel.xutils.DBHelp;
import com.makvenis.hotel.xutils.JavaXutilsCoffee;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.table.DbModel;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import json.makvenis.com.mylibrary.json.JSON;


/**
 * list界面
 */

public class SimpleFragment extends Fragment {

    /* 预备查询的Cid */
    private String mCid;

    /* 座位号 */
    private int pathNum;

    /* 适配商品列表的集合 */
    List<Map<String, String>> mData=new ArrayList<>();
    /* recycle控件 */
    @ViewInject(R.id.mRight_View)
    RecyclerView mRecycleView;

    /* 刷新组件 */
    @ViewInject(R.id.mSwipe_View)
    PullRefreshLayout mSwipe_View;

    /* 所有的总价格单元 */
    Map<String,String> mSaleDat = new HashMap<>();

    /* 回调子线程数据 */
    public Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 301: //适配数据
                    String footClass = (String) msg.obj;
                    if(footClass != null){
                        try {
                            if(mData.size() != 0){
                                mData.clear();
                            }
                            JSONObject object=new JSONObject(footClass);
                            JSONArray data = object.getJSONArray("data");
                            List<Map<String, String>> maps = JSON.GetJson(data.toString(),
                                    new String[]{"title","max","min","photoUrl","id","cid","context"});
                            mData.addAll(maps);
                            // 刷新适配器
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            new IllegalArgumentException(Configfile.jsonException("SimpleFragment"));
                        }
                    }
                    break;
            }
        }
    };
    private SimpleFragmentAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // here you can load whatever layout you want for your fragment
        View v = inflater.inflate(R.layout.public_fragment_instance, container, false);
        ViewUtils.inject(this,v);
        // for example set the address as a textview text, or do whatever you want with it
        //TextView tv = (TextView) v.findViewById(R.id.mTestTextFragment);
        //tv.setText();
        //传递的总数据--（json）
        String mParment = getArguments().getString("address");
        try {
            JSONObject object=new JSONObject(mParment);
            mCid = object.optString("cid");
            pathNum = object.optInt("pathNum");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /* 获取cid */
        return v;
    }

    public static SimpleFragment newInstance(String address) {
        SimpleFragment myFragment = new SimpleFragment();
        Bundle args = new Bundle();
        args.putString("address", address);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* 数据下载 */
        getNetData();

        /* 创建适配器 */
        createAdapter();

        /* 获取回调事件---> 价格 */
        adapter.setOnClinkSaleListener(new SimpleFragmentAdapter.OnClinkSale() {
            @Override //参数分别是商品个数 总价格 item位置 大小份
            public void showAllSale(int num,float sale,int position,String type) {
                mSaleDat.put(String.valueOf(position),String.valueOf(sale));
                float v = allSaleMath();
                /* 发送的参数包括当前fragment的总价格，以及cid编号（因为要获取很多cid的价格总和） */
                EventBus.getDefault().post(new SaleMessage(String.valueOf(v),mCid));
                /* 本地存储当前商品的名称、类别、数量、总价、桌号、 */
                boolean mBoolean = queryByConditionBoolean(position,type);
                if(mBoolean){
                    updateByColumn(position,num,type);
                }else {
                    addDataToDatabase(num,sale,position,type);
                }
            }
        });

        /* 刷新组件 */
        mSwipe_View.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNetData();
                mSwipe_View.setRefreshing(false);
            }
        });
    }

    private void createAdapter() {
        /* 设置方向 */
        RecyclerView.LayoutManager manager=new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL,false);
        mRecycleView.setLayoutManager(manager);
        adapter = new SimpleFragmentAdapter(mData,getActivity());
        mRecycleView.setAdapter(adapter);
    }

    private void getNetData() {
        /* 构建下载地址 */
        final String mPath = Configfile.EXECUTE_SQL_SERVICE+"?book=4&values="+mCid;
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] bytes = NetworkTools.NetShow(mPath);
                Message msg=new Message();
                msg.what=301;
                msg.obj=new String(bytes);
                mHandler.sendMessage(msg);
            }
        }).start();

    }

    private float allSaleMath(){
        float mAll=0;
        for (String saleStr:mSaleDat.values()) {
            float v = Float.valueOf(saleStr).floatValue();
            mAll+=v;
        }
        return mAll;
    }


    /* [添加] 添加商品 */
    private void addDataToDatabase(int num,float sale,int position,String type){
        /* 本地存储当前商品的cid、fid、名称、类别、数量、总价、桌号、状态（默认为1） */
        /* 通过position获取商品的基本信息 */
        Map<String, String> map = mData.get(position);
        if(map != null){
            try {
                Log.e("LOGO","执行.java插入数据["+map.toString()+"]");
                /* 商品名称 */
                String className = map.get("title");
                DbManager db = DBHelp.initDb();
                JavaXutilsCoffee e=new JavaXutilsCoffee();
                e.setCid(Integer.valueOf(mCid).intValue());
                e.setFid(Integer.valueOf(map.get("id")).intValue());
                e.setClassName(className);
                e.setNum(num);
                e.setType(type);
                e.setPathNum(pathNum); //座位编号
                e.setSale(sale);
                e.setZhuangtai(1);
                db.save(e);
            } catch (DbException e1) {
                e1.printStackTrace();
            }
        }
    }

    /* [判断] 商品是否存在 通过查询pathNum and cid and className and type and zhuangtai=1 */
    private boolean queryByConditionBoolean(int position,String type){
        try {
            //拿到某一个Item
            Map<String, String> map = mData.get(position);
            //创建sql语句
            String mSql = "select num from listCoffee where pathNum=? and cid=? and className='?' " +
                    "and type='?' and zhuangtai=1";
            //获取实质性的sql语句
            String sqlite = connationSqlite(mSql, new String[]{pathNum + "", mCid, map.get("title"),type});
            //执行查询
            DbManager db = DBHelp.initDb();
            List<DbModel> models = db.findDbModelAll(new SqlInfo(sqlite));

            if(models.size() > 0 && models != null){ //存在数据
                Log.e("LOGO","在执行判断时候 SimpleFragment #queryByConditionBoolean（）发现数据库中" +
                        "【存在】数据["+sqlite+"]");
                return true;
            }else { //不存在数据
                Log.e("LOGO","在执行判断时候 SimpleFragment #queryByConditionBoolean（）发现数据库中" +
                        "【不存在】数据["+sqlite+"]");
                return false;
            }
        } catch (DbException e) {
            new IllegalArgumentException("在SimpleFragment #queryByCondation() 中执行查询错误"+e.toString());
        }

        return false;
    }

    /* [更新] 更新商品的数目*/
    private void updateByColumn(int position,int i,String type){
        try {
            Map<String, String> map = mData.get(position);
            String mSql = "update listCoffee set num=? where pathNum=? and cid=? " +
                    "and className='?' and type='?' and zhuangtai=1";
            //获取实质性的sql语句
            String sqlite = connationSqlite(mSql, new String[]{i+"",
                    pathNum + "",
                    mCid,
                    map.get("title"),
                    type});
            //执行查询
            DbManager db = DBHelp.initDb();
            db.executeUpdateDelete(new SqlInfo(sqlite));
        } catch (DbException e) {
            new IllegalArgumentException("在SimpleFragment #queryByCondation() 中执行查询错误"+e.toString());
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
}
