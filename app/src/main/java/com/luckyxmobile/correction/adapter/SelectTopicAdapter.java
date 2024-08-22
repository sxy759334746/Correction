package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.luckyxmobile.correction.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanghao
 */
public class SelectTopicAdapter extends RecyclerView.Adapter<SelectTopicAdapter.MyReviewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private int index;
    private Context rContext;
    private List<Map<String, Object>> rDatas;
    private SelectTopicAdapter.OnRecyclerViewItemClickListener mOnItemClickListener;
    private SelectTopicAdapter.OnRecyclerViewItemLongClickListener mOnItemLongClickListener;
    // 存储勾选框状态的map集合
    private Map<Integer, Boolean> map = new HashMap<>();

    public SelectTopicAdapter(List<Map<String, Object>> datas, Context context) {
        rDatas = datas;
        rContext = context;
        initMap();
    }

    /**
     * 初始化map集合,默认为不选中
     */
    private void initMap() {
        for (int i = 0; i < rDatas.size(); i++) {
            if ((Boolean) rDatas.get(i).get("topic_selected")) {
                map.put(i, true);
            } else {
                map.put(i, false);
            }
        }
    }

    /**
     * 点击item选中CheckBox
     *
     * @param position 选中某个位置的checkbox
     */
    public void setSelectItem(int position) {
        //对当前状态取反
        if (map.get(position)) {
            map.put(position, false);
        } else {
            map.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void setmOnItemClickListener(SelectTopicAdapter.OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setmOnItemLongClickListener(SelectTopicAdapter.OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public SelectTopicAdapter.MyReviewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        rContext = viewGroup.getContext();
        View view = LayoutInflater.from(rContext).inflate(R.layout.recycle_item_topic_select, viewGroup, false);
        view.setOnLongClickListener(this);
        view.setOnClickListener(this);
        SelectTopicAdapter.MyReviewHolder viewHolder = new SelectTopicAdapter.MyReviewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SelectTopicAdapter.MyReviewHolder myReviewHolder, final int position) {
        myReviewHolder.itemView.setTag(position);
        myReviewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(position);
            }
        });
        //设置checkBox改变监听
        myReviewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //用map集合保存
                map.put(position, isChecked);
            }
        });
        // 显示图片
        Glide.with(rContext).load(rDatas.get(position).get("topic_imgUri"))
                .thumbnail(0.1f)
                .into(myReviewHolder.imageView);
        // 设置CheckBox的状态
        if (map.get(position) == null) {
            map.put(position, false);
        }
        myReviewHolder.checkBox.setChecked(map.get(position));
        // 判断是否选中
        if (map.get(position)) {
            // 选中
//            myReviewHolder.is_checked.setText("√");
//            myReviewHolder.is_checked.setTextColor(ContextCompat.getColor(rContext, R.color.green_57));
            myReviewHolder.item_check.setImageDrawable(ContextCompat.getDrawable(rContext, R.drawable.item_checked));

            /**
             * @author lg
             * 更换了选词界面的错题边框
             * */
            myReviewHolder.relativeLayout.setBackgroundResource(R.drawable.topic_select_background);
        } else {
            // 未选中
//            myReviewHolder.is_checked.setText("×");
//            myReviewHolder.is_checked.setTextColor(ContextCompat.getColor(rContext, R.color.red_ee));
            myReviewHolder.item_check.setImageDrawable(ContextCompat.getDrawable(rContext, R.drawable.item_uncheck));
            myReviewHolder.relativeLayout.setBackgroundResource(R.drawable.grid_menu);
        }
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

    //返回集合
    public Map<Integer, Boolean> getMap() {
        return map;
    }

    /**
     * 清空选中的map
     */
    public void clearMap() {
        map.clear();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    class MyReviewHolder extends RecyclerView.ViewHolder {

        //        TextView is_checked;
        CheckBox checkBox;
        ImageView imageView;
        ImageView item_check;
        RelativeLayout relativeLayout;

        public MyReviewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.topic_select_layout);
//            is_checked = itemView.findViewById(R.id.is_checked);
            checkBox = itemView.findViewById(R.id.wrong_list_item_cb);
            imageView = itemView.findViewById(R.id.wrong_img);
            item_check = itemView.findViewById(R.id.item_Checked);
        }
    }
}
