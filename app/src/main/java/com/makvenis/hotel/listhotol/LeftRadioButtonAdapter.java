package com.makvenis.hotel.listhotol;

/* 左侧导航栏目的适配器 */

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.makvenis.hotel.R;

import java.util.List;

public class LeftRadioButtonAdapter extends RecyclerView.Adapter<LeftRadioButtonAdapter.LeftViewHolder>{

    List<LeftEntry> mData;
    Context mContext;
    RecyclerView mRecyclerView;


    //先声明一个int成员变量
    private int thisPosition;
    //再定义一个int类型的返回值方法
    public int getthisPosition() {
        return thisPosition;
    }

    //其次定义一个方法用来绑定当前参数值的方法
    //此方法是在调用此适配器的地方调用的，此适配器内不会被调用到
    public void setThisPosition(int thisPosition) {
        this.thisPosition = thisPosition;
    }


    public LeftRadioButtonAdapter(List<LeftEntry> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }



    @Override
    public LeftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_leftbutton, parent, false);
        /* 创建View的点击事件 （普通） */
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecyclerView !=null && mCallBankItemCheck !=null) {
                    int position = mRecyclerView.getChildAdapterPosition(v);
                    LeftEntry entry = mData.get(position);
                    int num = entry.getNum();
                    mCallBankItemCheck.show(num,position);
                }else {
                    Log.e("DATA","mRecyclerView + mCallBankItemCheck");
                }
            }
        });

        return new LeftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LeftViewHolder holder, final int position) {
        if(holder instanceof LeftViewHolder){
            LeftEntry e = mData.get(position);
            holder.mList_coffee_eng.setText(e.getNameEng());
            holder.mList_coffee_cn.setText(e.getNameCn());
            Log.e("DATA","适配器赋值标题[LeftRadioButtonAdapter]--test--null"+e.getNameCn());
            if (position == getthisPosition()) {
                holder.mll.setBackgroundColor(Color.WHITE);
                holder.mList_coffee_cn.setTextColor(Color.parseColor("#dd932a"));
                holder.mList_coffee_eng.setTextColor(Color.parseColor("#dd932a"));
            } else {
                holder.mll.setBackgroundColor(Color.parseColor("#f6f6f6"));
                holder.mList_coffee_cn.setTextColor(Color.parseColor("#3d3d3d"));
                holder.mList_coffee_eng.setTextColor(Color.parseColor("#3d3d3d"));
            }

        }
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder{
        @ViewInject(R.id.mList_coffee_eng)
        TextView mList_coffee_eng;

        @ViewInject(R.id.mList_coffee_cn)
        TextView mList_coffee_cn;

        @ViewInject(R.id.mll)
        LinearLayout mll;

        public LeftViewHolder(View itemView) {
            super(itemView);
            ViewUtils.inject(this,itemView);
        }
    }

    /*他为哪一个recycler提供数据 当为一个recycle提供数据的时候就会调用这个方法*/
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }
    /*解绑*/
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.mRecyclerView = null;
    }
    CallBankItemCheck mCallBankItemCheck;

    public void setCallBankItemCheck(CallBankItemCheck mCallBankItemCheck) {
        this.mCallBankItemCheck = mCallBankItemCheck;
    }

    public interface CallBankItemCheck{
        void show(int cid,int position);
    }



}
