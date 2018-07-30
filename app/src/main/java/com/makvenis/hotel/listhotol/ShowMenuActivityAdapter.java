package com.makvenis.hotel.listhotol;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.makvenis.hotel.R;
import com.makvenis.hotel.xutils.DBHelp;

import org.xutils.DbManager;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.table.DbModel;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ShowMenuActivity 类的适配器}
 */

public class ShowMenuActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /* 布局ID */
    private static int LAYOUT_TYPE_1 = 0; //用户点餐的列表
    private static int LAYOUT_TYPE_2 = 1; //备注布局

    /* 传递参数 */
    public Context mContext;
    private int pathNum; //座位编号

    public ShowMenuActivityAdapter(Context mContext, int pathNum) {
        this.mContext = mContext;
        this.pathNum = pathNum;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == LAYOUT_TYPE_1){
            View view1 = LayoutInflater.from(mContext).inflate(R.layout.include_layout_recycle, parent, false);
            return new MyViewHolderMenu(view1);
        }else if(viewType == LAYOUT_TYPE_2){
            View view = LayoutInflater.from(mContext).inflate(R.layout.include_item_food_show_2, parent, false);
            view.layout(0,10,0,0);
            return new MyViewHolderNote(view);
        }else {
            return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolderMenu){
            try {
                /* 数据库查询的总价格 */
                float mAllSale=0;
                RecyclerView mRecycle = ((MyViewHolderMenu) holder).mIncludeRecycle;
                // TODO: 2018/7/18 适配器
                /* 查询数据库数据 */
                String mSql = "select className,num,sale,type,id from listCoffee where pathNum="+pathNum+" and zhuangtai =1";
                DbManager db = DBHelp.initDb();
                List<DbModel> models = db.findDbModelAll(new SqlInfo(mSql));
                List<Map<String,Object>> mDataMap = new ArrayList<>();
                for (int i = 0; i < models.size(); i++) {
                    Map<String,Object> map=new HashMap<>();
                    DbModel dbModel = models.get(i);
                    String className = dbModel.getString("className");

                    map.put("className",className);
                    map.put("num",dbModel.getInt("num"));
                    map.put("type",dbModel.getString("type"));

                    /* 也就是第一次进入的时候数据库获取总价格 */
                    float sale = dbModel.getFloat("sale"); //单价
                    int num = dbModel.getInt("num");       //数量
                    float v = Float.parseFloat(num + "");  //同类型转换
                    float k = v * sale;
                    mAllSale+=k;
                    /* 总价格累加 */
                    map.put("sale",dbModel.getFloat("sale"));
                    mDataMap.add(map);
                }

                Log.e("LOGO","\nsqlite语句："+mSql+"\n 数据库查询集合大小："+models.size()+"\n 添加到集合的数据："+mDataMap.toString());

                RecyclerView.LayoutManager manager=new LinearLayoutManager(mContext,
                        LinearLayoutManager.VERTICAL,false);
                mRecycle.setLayoutManager(manager);
                MyMenuAdapter adapter = new MyMenuAdapter(mDataMap, mContext);
                mRecycle.setAdapter(adapter);

                /* 回调总价格 */
                listener.showCallDatabaseSale(mAllSale);

            } catch (DbException e) {
                String mSql = "select className,num,sale from listCoffee where pathNum="+pathNum+" and zhuangtai =1";
                new IllegalArgumentException("在查询数据库时候出现异常 "+mSql+"<---"+e.toString());
            }

        }else if(holder instanceof MyViewHolderNote){
            final EditText mNotePeopleNumber = ((MyViewHolderNote) holder).mNotePeopleNumber;
            final EditText mNoteRemark = ((MyViewHolderNote) holder).mNoteRemark;
            final EditText mNoteTax = ((MyViewHolderNote) holder).mNoteTax;
            //获取用户输入的备注、人数、单位名称
            mNotePeopleNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputData = mNotePeopleNumber.getText().toString();
                    listener.showInputEdit(inputData);
                }
            });

            mNoteRemark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputData = mNoteRemark.getText().toString();
                    listener.showInputEdit(inputData);
                }
            });

            mNoteTax.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputData = mNoteTax.getText().toString();
                    listener.showInputEdit(inputData);
                }
            });

            getInputClient(mNotePeopleNumber);
            getInputClient(mNoteRemark);
            getInputClient(mNoteTax);
        }


    }

    public void getInputClient(final EditText editText){
        //mRecycle是整个页面，mNotePeopleNumber是edittext
        mRecycle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                return false;
            }
        });
    }

    RecyclerView mRecycle;
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecycle = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if(mRecycle != null){
            mRecycle = null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return LAYOUT_TYPE_1;
        }else if(position == 1){
            return LAYOUT_TYPE_2;
        }else {
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    /* [内部类] 布局1 */
    public class MyViewHolderMenu extends RecyclerView.ViewHolder{

        @ViewInject(R.id.mIncludeRecycle)
        RecyclerView mIncludeRecycle;

        public MyViewHolderMenu(View itemView) {
            super(itemView);
            ViewUtils.inject(this,itemView);
        }
    }

    /* [内部类] 布局2 */
    public class MyViewHolderNote extends RecyclerView.ViewHolder{
        /* 就餐人数 */
        @ViewInject(R.id.mNotePeopleNumber)
        EditText mNotePeopleNumber;

        /* 备注 */
        @ViewInject(R.id.mNoteRemark)
        EditText mNoteRemark;

        /* 发票抬头名称 */
        @ViewInject(R.id.mNoteTax)
        EditText mNoteTax;
        public MyViewHolderNote(View itemView) {
            super(itemView);
            ViewUtils.inject(this,itemView);
        }
    }

    public class MyMenuAdapter extends RecyclerView.Adapter<MyMenuAdapter.MyMenuViewHolder>{

        List<Map<String,Object>> mDataMap;
        Context mContext;

        public MyMenuAdapter(List<Map<String, Object>> mDataMap, Context mContext) {
            this.mDataMap = mDataMap;
            this.mContext = mContext;
        }

        @Override
        public MyMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.include_item_food_show_1,
                    parent, false);
            return new MyMenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyMenuViewHolder holder, final int position) {
            final Map<String, Object> map = mDataMap.get(position);

            String className = (String) map.get("className");
            if(className.length() > 8){
                String substring = className.substring(0, 8);
                holder.mShowClassName.setText(substring+"..");
            }else {
                holder.mShowClassName.setText(className);
            }

            holder.mShowAllNum.setText(((int) map.get("num"))+"");
            holder.mShowSingleSale.setText(((float) map.get("sale"))+"");
            String type = (String) map.get("type");
            if(type.equals("max")){
                holder.mShowType.setText("大份");
            }else {
                holder.mShowType.setText("小份");
            }



            final View view = holder.itemView;
            holder.mShowOperation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.showOperation(map,position, view);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataMap.size();
        }

        public class MyMenuViewHolder extends RecyclerView.ViewHolder{
            //R.id.mShowClassName,R.id.mShowSingleSale,R.id.mShowAllNum
            //分别对应 商品名称 总价格 总数量 类型 操作
            @ViewInject(R.id.mShowClassName)
            TextView mShowClassName;

            @ViewInject(R.id.mShowSingleSale)
            TextView mShowSingleSale;

            @ViewInject(R.id.mShowAllNum)
            TextView mShowAllNum;

            @ViewInject(R.id.mShowOperation)
            Button mShowOperation;

            @ViewInject(R.id.mShowType)
            TextView mShowType;

            public MyMenuViewHolder(View itemView) {
                super(itemView);
                ViewUtils.inject(this,itemView);
            }
        }
    }

    /* 注册 */
    OnClinkShowMenuListener listener;
    public void OnClinkListener(OnClinkShowMenuListener listener) {
        this.listener = listener;
    }

    public interface OnClinkShowMenuListener{

        /**
         *{@link OnClinkShowMenuListener {@link #showOperation(Map, int, View)} } 此方法用于回调当前操作
         * 操作分为 删除和更改 则都需要知道点击的是Item的位置，以及主要的参数
         * @param map 当前集合（适配的数据信息）
         * @param position Item位置
         */
        void showOperation(Map<String, Object> map,int position,View view);

        /**
         * {@link OnClinkShowMenuListener {@link #showCallDatabaseSale(float)}} 此方法回调数据库查询的总价格
         * @param mAllSale 总价格
         */
        void showCallDatabaseSale(float mAllSale);

        /**
         * {@link #OnClinkListener(OnClinkShowMenuListener) {@link #showInputEdit(String, String, String)}}
         * 回调用户输入的参数值
         * @param var1
         */
        void showInputEdit(String var1);


    }

}
