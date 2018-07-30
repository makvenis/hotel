package com.makvenis.hotel.listhotol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.makvenis.hotel.R;
import com.makvenis.hotel.tools.Configfile;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import json.makvenis.com.mylibrary.json.view.SimpleDelPlusView;

/**
 * SimpleFragment 适配器部署
 */

public class SimpleFragmentAdapter extends RecyclerView.Adapter<SimpleFragmentAdapter.SimpleFragmentViewHolder>{

    List<Map<String, String>> mData;
    Context mContext;
    int checkBox;

    public SimpleFragmentAdapter(List<Map<String, String>> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public SimpleFragmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_food, parent, false);
        return new SimpleFragmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleFragmentViewHolder holder,final int position) {
        if(holder instanceof SimpleFragmentViewHolder){
            /* 获取item参数 */
            final Map<String, String> map = mData.get(position);
            /* 赋值参数及其控件 */
            final SimpleDelPlusView mView = holder.mSimpleDelPlusView;

            // 0.商品图片
            String photoUrl = map.get("photoUrl");
            if(!photoUrl.equals("")){
                String replace = photoUrl.replace("../..", Configfile.PATH);
                Picasso.with(mContext).load(replace).error(R.drawable.icon_photo_no).into(
                        holder.mLookImage
                );
            }else {
                holder.mLookImage.setImageResource(R.drawable.icon_photo_no);
            }

            // 1.商品标题
            final String max = map.get("title");
            if(!max.equals("") ){
                if(max.length() > 7){
                    String subStringMax = max.substring(0, 7);
                    holder.mLookClassName.setText(subStringMax+"..");
                }else {
                    holder.mLookClassName.setText(max);
                }

            }else {
                holder.mLookClassName.setText("暂无数据");
                holder.mLookClassName.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG );
            }
            // 2.商品大份价格
            String maxSale = map.get("max");
            if(!maxSale.equals("")){
                holder.mLookSaleMax.setText(maxSale+"RMB");
            }else {
                holder.mLookSaleMax.setText("暂无数据");
                holder.mLookSaleMax.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG );
                holder.mLookSaleMin.setTextColor(Color.parseColor("#d4d4d4"));
                /* 价格不存在 那么也就不会被选中 */
                holder.mCheckMax.setEnabled(false);
                holder.mSimpleDelPlusView.setEnabled(false);
            }

            // 3.商品小份价格
            String minSale = map.get("min");
            if(!minSale.equals("")){
                Log.e("LOGO","--->"+minSale+"<---");
                holder.mLookSaleMin.setText(minSale+"RMB");
            }else {
                holder.mLookSaleMin.setText("暂无数据");
                holder.mLookSaleMin.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG );
                holder.mLookSaleMin.setTextColor(Color.parseColor("#d4d4d4"));
                /* 价格不存在 那么也就不会被选中 */
                holder.mCheckMin.setEnabled(false);
                holder.mSimpleDelPlusView.setEnabled(false);
            }

            /* 获取事件 */
            // 1.大份选中
            holder.mCheckMax.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        //小份设置为 false
                        holder.mCheckMin.setChecked(false);
                    }
                }
            });

            // 1.小份份选中
            holder.mCheckMin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        //大份设置为 false
                        holder.mCheckMax.setChecked(false);
                    }
                }
            });

            // 3.选择数量
            mView.onClinkCheckNumber(new SimpleDelPlusView.setOnClinkCheckNumber() {
                @Override
                public void showNowNumbler(int i) {
                    /* 查看是否被选中 */
                    boolean max = holder.mCheckMax.isChecked();
                    boolean min = holder.mCheckMin.isChecked();
                    if(max){
                        String maxSale = holder.mLookSaleMax.getText().toString();
                        if(maxSale != ""){
                            Float valueOf = Float.valueOf(maxSale.replace("RMB","")); //价格
                            /* 总价格 */
                            float mAllSale = valueOf * i;
                            listener.showAllSale(i,mAllSale,position,"max");
                        }else {
                            Configfile.Log(mContext,"当前商品已售罄");
                        }
                    }else if(min){
                        String minSale = holder.mLookSaleMin.getText().toString();
                        if(minSale != ""){
                            Float valueOf = Float.valueOf(minSale.replace("RMB","")); //价格
                            /* 总价格 */
                            float mAllSale = valueOf * i;
                            listener.showAllSale(i,mAllSale,position,"min");
                        }else {
                            Configfile.Log(mContext,"当前商品已售罄");
                        }
                    }else {
                        Configfile.Log(mContext,"请选择商品类型");
                    }
                }
            });

            /* item的点击事件 */
            holder.mLookImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* 查看详细信息 */
                    Intent intent=new Intent(mContext,InformationActivity.class);
                    intent.putExtra("class",Configfile.PACKAGE+".listhotol.ListCofferActivity");
                    intent.putExtra("id",map.get("id"));
                    intent.putExtra("title",map.get("title"));
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class SimpleFragmentViewHolder extends RecyclerView.ViewHolder{

        /* 样式左边图片 */
        @ViewInject(R.id.mLookImage)
        ImageView mLookImage;

        /* 商品名称 */
        @ViewInject(R.id.mLookClassName)
        TextView mLookClassName;

        /* 大份价格 */
        @ViewInject(R.id.mLookSaleMax)
        TextView mLookSaleMax;

        /* 小份价格 */
        @ViewInject(R.id.mLookSaleMin)
        TextView mLookSaleMin;

        /* 选择checkbox---> 大份 */
        @ViewInject(R.id.mCheckMax)
        CheckBox mCheckMax;

        /* 选择checkbox---> 小份 */
        @ViewInject(R.id.mCheckMin)
        CheckBox mCheckMin;

        /* 选择数量左右加减 */
        @ViewInject(R.id.mSimpleDelPlusView)
        SimpleDelPlusView mSimpleDelPlusView;

        /* ll */
        @ViewInject(R.id.ll)
        LinearLayout ll;

        public SimpleFragmentViewHolder(View itemView) {
            super(itemView);
            ViewUtils.inject(this,itemView);
        }
    }

    OnClinkSale listener;

    public void setOnClinkSaleListener(OnClinkSale listener) {
        this.listener = listener;
    }

    public interface OnClinkSale{
        /**
         * 回调的数据结构
         * @param sale 当前价格
         * @param position 价格处在item位置
         * @param type 类别（大份，小份）
         */
        void showAllSale(int num,float sale,int position,String type);
    }

}
