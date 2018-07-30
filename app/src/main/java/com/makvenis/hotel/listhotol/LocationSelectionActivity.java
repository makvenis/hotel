package com.makvenis.hotel.listhotol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.makvenis.hotel.R;
import com.makvenis.hotel.activity.MainActivity;
import com.makvenis.hotel.tools.Books;
import com.makvenis.hotel.tools.Configfile;
import com.makvenis.hotel.tools.NetworkTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import json.makvenis.com.mylibrary.json.view.SimpleDialogView;

/**
 * @解析  服务员为客服选择就餐座位
 * @解析  通过本界面，为服务员体现当前的座位的状态（空闲，就餐中，待清理，已预定）
 * @通过  空闲桌面，然后点餐
 */


@ContentView(R.layout.activity_location_selection)
public class LocationSelectionActivity extends AppCompatActivity {

    /* this */
    public Context mContext = LocationSelectionActivity.this;

    /* 适配器大集合 */
    List<LocationSelectEntry> mData = new ArrayList<>();

    /* Handler组件 */
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 300:
                    String mNetData = (String) msg.obj;
                    Log.e("LOGO","LocationSelectionActivity handler-300["+mNetData+"]");
                    if(mNetData != null){
                        try {
                            if(mData.size() != 0){
                                mData.clear();
                            }
                            JSONObject object=new JSONObject(mNetData);
                            JSONArray array = object.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                LocationSelectEntry e=new LocationSelectEntry();
                                e.setPathNumber(obj.optInt("pathNumber"));
                                e.setPathName(obj.optString("pathName"));
                                e.setZhuangtai(obj.optInt("zhuangtai"));
                                e.setPeopleNumber(obj.optInt("peopleNumber"));
                                mData.add(e);
                            }

                            adapter.notifyDataSetChanged();
                            if(dialogView != null){
                                dialogView.close();
                            }
                        } catch (JSONException e) {
                            new IllegalArgumentException(Configfile.jsonException("LocationSelectionActivity" +
                                    "#netDownPathNum()"));
                        }
                    }else {
                        Configfile.Log(mContext,"网络链接失败,暂未获取到数据");
                        if(dialogView != null){
                            dialogView.close();
                        }
                    }

                    break;
            }
        }
    };

    /* recycle组件 */
    @ViewInject(R.id.mSelectionPathRecycle)
    RecyclerView mRecyclePath;
    /* adapter */
    private LocationSelectionAdapter adapter;

    /* include start */
    @ViewInject(R.id.mSimpleBank)
    ImageView mSimpleBank;

    @ViewInject(R.id.mSimpleBankNme)
    TextView mSimpleBankNme;
    private SimpleDialogView dialogView;
    /* include end */

    /* 刷新组件 */
    @ViewInject(R.id.mSelectionPathSwipe)
    PullRefreshLayout mSelectionPathSwipe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

        /* 数据下载 */
        netDownPathNum();

        /* 设置适配器 */
        settingAdapter();

        /* adapter的回调事件等状态 */
        adapter.setOnClinkCallBankItem(new LocationSelectionAdapter.OnClinkCallBankItem() {
            @Override
            public void showItemOnClink(int position, LocationSelectEntry e) {

            }
        });

        /* 设置标题 */
        mSimpleBankNme.setText("选择席位");

        /* 刷新组件 */
        mSelectionPathSwipe.setColor(Color.GREEN);
        mSelectionPathSwipe.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final String mPath = Configfile.EXECUTE_SQL_SERVICE +"?"+ Books.PATH_NUM_BOOK;
                Log.e("LOGO","LocationSelectionActivity #netDownPathNum["+mPath+"]");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String mNetData = new String(NetworkTools.NetShow(mPath));
                        Message msg=new Message();
                        msg.what=300;
                        msg.obj=mNetData;
                        mHandler.sendMessage(msg);
                    }
                }).start();
                mSelectionPathSwipe.setRefreshing(false);
            }
        });
    }

    /* 设置适配器 */
    private void settingAdapter() {
        //RecyclerView.LayoutManager manager=new StaggeredGridLayoutManager()
        StaggeredGridLayoutManager manager=new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclePath.setLayoutManager(manager);
        adapter = new LocationSelectionAdapter(mData,mContext);
        mRecyclePath.setAdapter(adapter);
    }


    /* 下载数据 */
    public void netDownPathNum(){
        dialogView = new SimpleDialogView(mContext,"加载客桌信息...");
        dialogView.show();
        final String mPath = Configfile.EXECUTE_SQL_SERVICE +"?"+ Books.PATH_NUM_BOOK;
        Log.e("LOGO","LocationSelectionActivity #netDownPathNum["+mPath+"]");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String mNetData = new String(NetworkTools.NetShow(mPath));
                Message msg=new Message();
                msg.what=300;
                msg.obj=mNetData;
                mHandler.sendMessage(msg);
            }
        }).start();
    }


    /* [固定返回] 返回点击事件 */
    @OnClick({R.id.mSimpleBank})
    public void mSimpleBank(View v){
        /* 这是特定返回 意思就是这个页面只允许返回到某一个页面 */
        startActivity(new Intent(mContext, MainActivity.class));
    }

    /* [remark] 因为这个页面是为了实时获取最新数据 所以未使用HttpUtils() 故所以需要改进dialog的效果 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            dialogView.close();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
