package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Paper;

import java.util.List;

public class PaperAdapter extends RecyclerView.Adapter<PaperAdapter.MyReviewHolder>
        implements View.OnClickListener, View.OnLongClickListener {
    private int index;
    private Context rContext;
    private List<Paper> rDatas;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener;
    private OnPaperMenuClickListener mPaperMenuClickListener;


    public void setDatas(List<Paper> rDatas) {
        this.rDatas = rDatas;
    }

    public void setmOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setmOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public interface OnPaperMenuClickListener {
        void paperMenuClick(View v,int position);
    }

    public void setPaperMenuClickListener(OnPaperMenuClickListener onPaperMenuClickListener) {
        this.mPaperMenuClickListener = onPaperMenuClickListener;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    ;

    public int getIndex() {
        return this.index;
    }


    @Override
    public MyReviewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        rContext = viewGroup.getContext();
        final View view = LayoutInflater.from(rContext).inflate(R.layout.recycle_item_paper, viewGroup, false);
        view.setOnLongClickListener(this);
        view.setOnClickListener(this);

        final MyReviewHolder viewHolder = new MyReviewHolder(view);
        //更多按钮添加点击事件
        viewHolder.review_recyclerview_ibt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaperMenuClickListener.paperMenuClick(viewHolder.review_recyclerview_ibt,viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(final MyReviewHolder myReviewHolder, final int position) {
        myReviewHolder.review_recyclerview_tv.setText(rDatas.get(position).getPaper_name());
        myReviewHolder.itemView.setTag(position);

    }

    @Override
    public int getItemCount() {
        return rDatas.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, (int) view.getTag());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (mOnItemLongClickListener != null) {
            mOnItemLongClickListener.onItemLongClick(view, (int) view.getTag());
        }
        return true;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    class MyReviewHolder extends RecyclerView.ViewHolder {
        TextView review_recyclerview_tv;
        ImageButton review_recyclerview_ibt;

         MyReviewHolder(View itemView) {
            super(itemView);
            review_recyclerview_tv = itemView.findViewById(R.id.review_recyclerview_tv);
            review_recyclerview_ibt = itemView.findViewById(R.id.paper_item_bt);
        }
    }
    //添加复习卷   lyw
    public void addPaper(Paper paper){
        rDatas.add(paper);
        notifyDataSetChanged();
    }
}
