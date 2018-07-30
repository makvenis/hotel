package com.makvenis.hotel.listhotol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bm.library.Info;
import com.bm.library.PhotoView;
import com.jude.rollviewpager.OnItemClickListener;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.StaticPagerAdapter;
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
import com.makvenis.hotel.utils.JSON;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import json.makvenis.com.mylibrary.json.view.SimpleDelPlusView;
import json.makvenis.com.mylibrary.json.view.SimpleDialogView;
/* 每一个商品的详细信息列表 */

@ContentView(R.layout.activity_information)
public class InformationActivity extends AppCompatActivity {

    /* 上下文 */
    public final Context mContext = InformationActivity.this;

    /* 全局包名 */
    public final String mPackage = Configfile.PACKAGE+".listhotol.InformationActivity";

    /* 返回事件控件 */
    @ViewInject(R.id.mSimpleBank)
    ImageView mSimpleBank;

    /* 标题 */
    @ViewInject(R.id.mSimpleBankNme)
    TextView mSimpleBankNme;

    /* 幻灯组件查找 */
    @ViewInject(R.id.mRollPagerView)
    RollPagerView mRollPagerView;

    /* recycle控件 */
    /**
     * {@parm 产品参数列表}
     */
    @ViewInject(R.id.mInformationRecycle)
    RecyclerView mInformationRecycle;


    /* 来源class名称 */
    private String mClassName;
    /* 数据库表jdb_net_information中cid */
    private String mCid;
    /* 全局dialog */
    private SimpleDialogView dialogView;
    /**
     * {@link SimpleFragmentAdapter #onBinderViewHolder()传递过来的intent参数}
     */
    /* 适配器[标题] */
    private String mTitle;

    /* 物品名称 */
    /**
     * {@parm 产品名称 #mTitle 名称一样}
     */
    @ViewInject(R.id.mInformationName)
    TextView mInformationName;

    /* 物品名称 */
    /**
     * {@parm 产品价格 # 传递一样}
     */
    @ViewInject(R.id.mInformationSale)
    TextView mInformationSale;

    /* 详情页面选择购买数量 */
    @ViewInject(R.id.mInformationDelPlusView)
    SimpleDelPlusView  mInformationDelPlusView;

    /* 加载更多评论 */
    @ViewInject(R.id.mLoadingEvaluation)
    TextView mLoadingEvaluation;

    /* 加载评论等待ProgressBar */
    @ViewInject(R.id.mInformationProgressBar)
    ProgressBar mInformationProgressBar;

    /* 分享区域 */
    @ViewInject(R.id.linearLayoutShare)
    LinearLayout linearLayoutShare;


    /* [remark] 全局数据 高清幻灯HD的数据默认 */
    List<String> mHDPathUrl = new ArrayList<>();
    /* [remark] 其余单参数放置到单集合 */
    Map<String,Object> mMapSingleValue = new HashMap<>();

    /* 全局Handler */
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 3018:
                    String obj = (String) msg.obj;
                    if(obj != null){
                        /* 分段解析数据 */
                        try {
                            JSONObject object=new JSONObject(obj);
                            JSONArray array = object.getJSONArray("data");
                            if(array.length() > 0){
                                /*
                                "photoUrl":"../../uploadfile/image/2018/2018072716112500.jpg,../../uploadfile/image/2018/2018072716112508.jpg",
                                "number":"SD2017",
                                "specs":"合格",
                                "addtime":"2018-07-27 16:11:44",
                                "weight":"23.89",
                                "id":"3",
                                "linkurl":"http://127.0.0.1/im/admin/Beginner/index.html",
                                "title":"十大阿萨",
                                "cid":"6"
                                **/
                                /* 无论这个商品有多少条数据 在页面有且只有显示一条数据的资格 */
                                JSONObject opt = array.getJSONObject(0);
                                String url = opt.optString("photoUrl");
                                String[] split = url.split(",");
                                for (int i = 0; i < split.length; i++) {
                                    /* 替换成全路径 */
                                    mHDPathUrl.add(split[i].replace("../..",Configfile.PATH));
                                }
                                /* 把剩余的参数放置到map集合里面 */
                                Map<String, Object> json = JSON.getObjectJson(opt.toString(), new String[]{"number", "specs",
                                        "weight"});
                                mMapSingleValue.putAll(json);
                                /* 参数名称 */
                                /**
                                 * @parm json 与 mKeyName 是一个共同体 获取的参数 == 标题名称
                                 */
                                String[] mKeyName = new String[]{"编号","规格","净重量"};
                                /* 设置适配器 */
                                settingNetData(json,mKeyName);

                            }else {
                                /* 这个商品没有更多的详细数据，提示并且返回上一级 */
                                dialogView.close();
                                Configfile.Log(mContext,"暂无数据");
                                String mClass = Bank.goBank(mContext, "InformationActivity");
                                try {
                                    Class<? extends Activity> forName = (Class<? extends Activity>) Class.forName(mClass);
                                    startActivity(new Intent(mContext,forName));
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }

                        } catch (JSONException e) {
                            new IllegalArgumentException(Configfile.jsonException(mPackage+"Handler case 3018"));
                        }
                    }else {
                        Configfile.Log(mContext,"请求数据失败！");
                    }
                    if(dialogView != null){
                        dialogView.close();
                    }
                    break;

                case 3020:
                    String mEvaluation = (String) msg.obj;
                    if(mEvaluation != null){

                        /* 设置进度不可见 */
                        mInformationProgressBar.setVisibility(View.GONE);
                    }else {
                        Log.e("TAG","暂无评论");
                        mLoadingEvaluation.setText("暂无评论");
                        /* 设置进度不可见 */
                        mInformationProgressBar.setVisibility(View.GONE);
                    }


                    break;
            }
        }


    };
    public MyPagerAdapter mHDPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

        /* 获取父类传递参数 */
        getParentData();
        /* 设置部分参数 */
        setting();
        /* 数据下载 */
        getNetData();

    }

    /* [预设参数] 设置部分参数 */
    private void setting() {
        /* 设置返回按钮旁边的标题 */
        if(mTitle != "" && mTitle.length() > 6){
            mSimpleBankNme.setText(mTitle.substring(0, 6)+"...");
        }else {
            mSimpleBankNme.setText(mTitle);
        }
        /* 赋值本页面的标题 */
        mInformationName.setText(mTitle);
        /* 赋值本页面的价格 */
        mInformationSale.setText("￥23.90");
        /* 选择数量 */
        mInformationDelPlusView.onClinkCheckNumber(new SimpleDelPlusView.setOnClinkCheckNumber() {
            @Override
            public void showNowNumbler(int i) {

            }
        });

        /* 加载更多评论 */
        mLoadingEvaluation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 设置进度开启可见 */
                mInformationProgressBar.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RequestParams params = new RequestParams();
                        params.addBodyParameter("book",Books.PATH_SELECT_EVALUATION_BOOK);
                        params.addBodyParameter("values",mCid+"");
                        new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                                Configfile.EXECUTE_SQL_SERVICE,
                                params,
                                new RequestCallBack<String>() {
                                    @Override
                                    public void onSuccess(ResponseInfo<String> responseInfo) {
                                        String result = responseInfo.result;
                                        if(result != null){
                                            Message msg=new Message();
                                            msg.what=3020;
                                            msg.obj=result;
                                            mHandler.sendMessage(msg);
                                        }
                                    }

                                    @Override
                                    public void onFailure(HttpException e, String s) {

                                    }
                                });


                    }
                }).start();
            }
        });




    }

    /* [获取父类] 获取父类传递参数 */
    public void getParentData() {
        Intent intent = getIntent();
        mClassName = intent.getStringExtra("class");
        mCid = intent.getStringExtra("id");
        mTitle = intent.getStringExtra("title");
        Bank.goTo(mContext,mClassName,"InformationActivity");
    }

    /* [特定返回] 返回的点击事件 */
    @OnClick({R.id.mSimpleBank})
    public void mSimpleBank(View v){
        String mClass = Bank.goBank(mContext, "InformationActivity");
        try {
            Class<? extends Activity> forName = (Class<? extends Activity>) Class.forName(mClass);
            startActivity(new Intent(mContext,forName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* [数据请求] 请求数据下载 */
    public void getNetData() {
        dialogView = new SimpleDialogView(mContext,"获取数据...");
        dialogView.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestParams params=new RequestParams();
                params.addBodyParameter("book", Books.PATH_SELECT_INFORMATION_BOOK);
                params.addBodyParameter("values",mCid+"");

                new HttpUtils(10000).send(HttpRequest.HttpMethod.POST,
                        Configfile.EXECUTE_SQL_SERVICE,
                        params,
                        new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                String result = responseInfo.result;
                                if(result != null){
                                    Message msg=new Message();
                                    msg.what=3018;
                                    msg.obj=result;
                                    mHandler.sendMessage(msg);
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
            }
        }).start();
    }

    /* [数据下载成功 适配数据] */
    private void settingNetData(Map<String,Object> mData,
            String[] mKeyClassName) {
        mHDPagerAdapter = new MyPagerAdapter(mHDPathUrl,mContext);
        mRollPagerView.setAdapter(mHDPagerAdapter);
        /* 启动自动翻页 */
        mRollPagerView.setPlayDelay(2000);
        mRollPagerView.pause();
        mRollPagerView.resume();
        mRollPagerView.isPlaying();
        /* 图片点击事件 */
        mRollPagerView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String s = mHDPathUrl.get(position);
                AdvImageView(s);
            }
        });


        /* [remark] 产品参数 设置适配器 */
        RecyclerView.LayoutManager manager=new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL,
                false);
        InformationAdapter adapter=new InformationAdapter(mData,mKeyClassName,mContext);
        mInformationRecycle.setLayoutManager(manager);
        mInformationRecycle.setAdapter(adapter);

    }

    /* [幻灯片adapter] 适配器 */
    public class MyPagerAdapter extends StaticPagerAdapter {

        private int[] image = {R.drawable.icon_test, R.drawable.icon_test, R.drawable.icon_test, R.drawable.icon_test};

        List<String> imgData;
        Context mContext;

        public MyPagerAdapter(List<String> imgData, Context mContext) {
            this.imgData = imgData;
            this.mContext = mContext;
        }

        @Override
        public View getView(ViewGroup container, int position) {
            ImageView imageView = new ImageView(container.getContext());

            String map = imgData.get(position);
            // 按比例扩大图片的size居中显示，使得图片长(宽)等于或大于View的长(宽)
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            Picasso.with(mContext).load(map)
                    .error(R.drawable.icon_net_error_120)
                    .into(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return imageView;
        }

        @Override
        public int getCount() {
            return imgData.size();
        }
    }

    /* [remark] 产品参数适配器 */
    public class InformationAdapter extends RecyclerView.Adapter<InformationAdapter.InformationViewHolder>{

        Map<String,Object> mData;
        String[] mKey;
        Context mContext;

        public InformationAdapter(Map<String, Object> mData, String[] mKey, Context mContext) {
            this.mData = mData;
            this.mKey = mKey;
            this.mContext = mContext;
        }

        @Override
        public InformationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.include_layout_information_item,
                    parent, false);
            return new InformationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(InformationViewHolder holder, int position) {
            /* 值 */
            String v = (String) mData.get(position);
            String k = mKey[position];
            if( v != null && k != null){
                Log.e("DATA",mPackage+"产品参数适配器 [InformationAdapter] \nk == "+k+" \n" +
                        "v== "+v);
                holder.mInformationTitle.setText(k);
                holder.mInformationValue.setText(v);
            }

        }

        @Override
        public int getItemCount() {
            return mData.size() == mKey.length ? mData.size() : 0;
        }

        public class InformationViewHolder extends RecyclerView.ViewHolder{

            /* 标题 栏目名称 */
            @ViewInject(R.id.mInformationTitle)
            TextView mInformationTitle;

            @ViewInject(R.id.mInformationValue)
            TextView mInformationValue;

            public InformationViewHolder(View itemView) {
                super(itemView);
                ViewUtils.inject(this,itemView);
            }
        }
    }

    /* [广告窗口] 弹出对话框 */
    /**
     * @parm 点击图片实现对图片的放大，缩放等功能
     */
    public void AdvImageView(String pic){
        View view = View.inflate(mContext, R.layout.include_adv_image, null);
        PhotoView mPhotoView = (PhotoView) view.findViewById(R.id.mImg_first);
        Picasso.with(mContext).load(pic).into(mPhotoView);
        setPhotoView(mPhotoView);
        LinearLayout mLine = (LinearLayout) view.findViewById(R.id.mLine);
        final PopupWindow mPopu = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPopu.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        /* 设置不透明 */
        //mLine.getBackground().setAlpha(100);
        mLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopu.dismiss();
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
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

            }
        });
    }

    /* [预览图图片设置] */
    public void setPhotoView(PhotoView photoView){
        // 启用图片缩放功能
        photoView.enable();
        // 禁用图片缩放功能 (默认为禁用，会跟普通的ImageView一样，缩放功能需手动调用enable()启用)
        //photoView.disenable();
        // 获取图片信息
        Info info = photoView.getInfo();
        // 从普通的ImageView中获取Info
        //Info info = PhotoView.getImageViewInfo(ImageView);
        // 从一张图片信息变化到现在的图片，用于图片点击后放大浏览，具体使用可以参照demo的使用
        photoView.animaFrom(info);
        // 从现在的图片变化到所给定的图片信息，用于图片放大后点击缩小到原来的位置，具体使用可以参照demo的使用
        photoView.animaTo(info,new Runnable() {
        @Override
        public void run() {
            //动画完成监听
        }
        });
        // 获取/设置 动画持续时间
        photoView.setAnimaDuring(1);
        int d = photoView.getAnimaDuring();
        // 获取/设置 最大缩放倍数
        photoView.setMaxScale(2);
        float maxScale = photoView.getMaxScale();
        // 设置动画的插入器
        //photoView.setInterpolator(Interpolator interpolator);
    }

    /* [分享模块] 分享当前信息的基本资料 */
    @OnClick({R.id.linearLayoutShare})
    public void linearLayoutShare(View v){
        
    }
}
