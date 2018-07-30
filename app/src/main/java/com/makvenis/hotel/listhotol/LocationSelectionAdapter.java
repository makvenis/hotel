package com.makvenis.hotel.listhotol;


import android.content.Context;
import android.content.Intent;
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
import com.makvenis.hotel.tools.Configfile;

import java.util.List;

public class LocationSelectionAdapter extends RecyclerView.Adapter<LocationSelectionAdapter.MyLocationSelectViewHolder> {

    List<LocationSelectEntry> mData;
    Context mContext;

    public LocationSelectionAdapter(List<LocationSelectEntry> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public MyLocationSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.include_location_selection_item,
                parent, false);
        return new MyLocationSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyLocationSelectViewHolder holder, int position) {
        final LocationSelectEntry e = mData.get(position);
        holder.mLocationSelect_number.setText("编号-"+e.getPathNumber()+"");
        /* 获取当前的状态 */
        final int type = e.getZhuangtai();
        Log.e("DATA","刷新结果 type=["+type+"] position=["+position+"]");
        /* 获取布局ll */
        LinearLayout linearLayout = holder.ll;
        if(type == 1){       // 就餐中 颜色橘红 9b58b5
            linearLayout.setBackgroundColor(Color.parseColor("#9b58b5")); //紫色
        }else if(type == 2){ // 待清理 颜色橘红 f37835
            linearLayout.setBackgroundColor(Color.parseColor("#f37835")); //橘红
        }else if(type == 3){ // 预定 颜色绿色 4ecdc4
            linearLayout.setBackgroundColor(Color.parseColor("#4ecdc4")); //绿色
        }else if(type == 0){ // 空闲状态 颜色绿色 5274d1
            linearLayout.setBackgroundColor(Color.parseColor("#5274d1")); //蓝色
        }
        holder.mLocationSelect_name.setText(e.getPathName());
        holder.mLocationSelect_peopleNumber.setText("("+e.getPeopleNumber()+")人桌");

        /* 点击事件 */
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type == 0){ //表示空闲桌面才能点餐
                    /* 获取当前类的class名称 */
                    String mClassName = Configfile.PACKAGE+".listhotol.LocationSelectionActivity";
                    Intent intent=new Intent(mContext, ListCofferActivity.class);
                    intent.putExtra("pathNum",e.getPathNumber());
                    intent.putExtra("class",mClassName);
                    mContext.startActivity(intent);
                }else if(type == 1){
                    Configfile.Log(mContext,"该桌客人正在使用");
                }else if(type == 2){
                    Configfile.Log(mContext,"桌面等待清理");
                }else if(type == 3){
                    Configfile.Log(mContext,"此位已被预定");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyLocationSelectViewHolder extends RecyclerView.ViewHolder{

        @ViewInject(R.id.mLocationSelect_number)
        TextView mLocationSelect_number;

        @ViewInject(R.id.mLocationSelect_name)
        TextView mLocationSelect_name;

        @ViewInject(R.id.mLocationSelect_peopleNumber)
        TextView mLocationSelect_peopleNumber;

        @ViewInject(R.id.mLocationSelect_ll)
        LinearLayout ll;
        public MyLocationSelectViewHolder(View itemView) {
            super(itemView);
            ViewUtils.inject(this,itemView);
        }
    }

    OnClinkCallBankItem listener;
    public void setOnClinkCallBankItem(OnClinkCallBankItem listener) {
        this.listener = listener;
    }
    public interface OnClinkCallBankItem{
        /**
         * 回调当前Item的信息，供外不适用
         * @param position 当前Item的位置
         * @param e 实体类
         */
        void showItemOnClink(int position,LocationSelectEntry e);
    }
}
