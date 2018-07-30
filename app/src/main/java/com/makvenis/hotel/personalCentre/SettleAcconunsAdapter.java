package com.makvenis.hotel.personalCentre;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.makvenis.hotel.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 结算适配器
 */

public class SettleAcconunsAdapter extends RecyclerView.Adapter<SettleAcconunsAdapter.SettleViewHolder> {

    List<SettleAccounstsEnrty> mData;
    Context mContext;


    public SettleAcconunsAdapter(List<SettleAccounstsEnrty> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public SettleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.include_layout_settleaccouns_item,
                parent, false);
        return new SettleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SettleViewHolder holder, int position) {

        SettleAccounstsEnrty e = mData.get(position);
        Picasso.with(mContext)
                .load(e.getPhotoUrl())
                .resize(100,100)
                .centerCrop()
                .error(R.drawable.icon_net_error_120)
                .into(holder.mSettleItemImage);
        holder.mSettleItemName.setText(e.getsName());
        holder.mSettleItemNum.setText("数量X"+e.getAnum());
        holder.mSettleItemSale.setText("单价:"+e.getAsale()+"RMB");


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class SettleViewHolder extends RecyclerView.ViewHolder{
        @ViewInject(R.id.mSettleItemImage)
        ImageView mSettleItemImage;

        @ViewInject(R.id.mSettleItemName)
        TextView mSettleItemName;

        @ViewInject(R.id.mSettleItemNum)
        TextView mSettleItemNum;

        @ViewInject(R.id.mSettleItemSale)
        TextView mSettleItemSale;

        public SettleViewHolder(View itemView) {
            super(itemView);
            ViewUtils.inject(this,itemView);
        }
    }

    /* 接口回调 */
    OnClinkCallBackAllSale listener;
    public void setOnClinkCallBackAllSale(OnClinkCallBackAllSale listener) {
        this.listener = listener;
    }
    public interface OnClinkCallBackAllSale{
        void showAllSale(float allSale);
    }
}
