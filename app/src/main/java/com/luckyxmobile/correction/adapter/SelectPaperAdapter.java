package com.luckyxmobile.correction.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Paper;
import java.util.List;
/**
 * @author deh
 * */
public class SelectPaperAdapter extends RecyclerView.Adapter<SelectPaperAdapter.ViewHolder>
        implements View.OnClickListener,View.OnLongClickListener{
    private List<Paper> rDatas;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener;

    public void setDatas(List<Paper> rDatas){
        this.rDatas = rDatas;
    }

    public void setmOnItemClickListener(OnRecyclerViewItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    public void setmOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener){
        this.mOnItemLongClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycle_item_paper,viewGroup,false);
        view.setOnLongClickListener(this);
        view.setOnClickListener(this);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.tv.setText(rDatas.get(i).getPaper_name());
        viewHolder.itemView.setTag(i);
    }

    @Override
    public int getItemCount() {
        return rDatas.size();
    }

    @Override
    public void onClick(View view) {
        if(mOnItemClickListener !=null){
            mOnItemClickListener.onItemClick(view,(int)view.getTag());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (mOnItemLongClickListener != null) {
            mOnItemLongClickListener.onItemLongClick(view,(int)view.getTag());
        }
        return true;
    }

    public interface OnRecyclerViewItemClickListener{
        void onItemClick(View view, int position);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view , int position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.review_recyclerview_tv);
        }
    }
}
