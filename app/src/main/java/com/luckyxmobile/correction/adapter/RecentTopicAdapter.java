package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.ui.activity.MainActivity;
import com.luckyxmobile.correction.ui.activity.TopicViewPageActivity;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.FastJsonUtil;
import com.luckyxmobile.correction.util.PhotoUtil;


import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class RecentTopicAdapter extends RecyclerView.Adapter<RecentTopicAdapter.ViewHolder> {

    private static final int LAYOUT_TYPE_TOPIC = R.layout.recycle_item_recent_topic; //topic view
    private static final int LAYOUT_TYPE_DATA_ALL = R.layout.recent_topic_footer;//
    private SharedPreferences preferences;
    private List<Topic> topics;
    private List<String> whichShowPrint;
    private Context context;
    public int index;  //MainActivity 中滑动的记录
    private int groupCount;  //list分组索引数
    private View mFooterView;
    private static final String TAG = "RecentTopicAdapter";
    private int size;

    public RecentTopicAdapter(Context context, List<Topic> topics,int index){
        preferences = context.getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);
        this.context = context;
        this.topics = topics;
        this.index = index;
        preferences = context.getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);
        String s = preferences.getString(ConstantsUtil.TABLE_SHOW_SMEAR,"");
        if (TextUtils.isEmpty(s)){
            whichShowPrint = new ArrayList<>();
        }else{
            whichShowPrint = PhotoUtil.transformListOnSmear(context, s);
        }
    }

    public RecentTopicAdapter(Context context, List<Topic> topics){
        preferences = context.getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);
        this.context = context;
        this.topics = topics;
        if (topics.isEmpty()){
            MainActivity.recentTopicText.setVisibility(View.INVISIBLE);
        }else{
            MainActivity.recentTopicText.setVisibility(View.VISIBLE);
        }
        preferences = context.getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);
        String s = preferences.getString(ConstantsUtil.TABLE_SHOW_SMEAR,"");
        if (TextUtils.isEmpty(s)){
            whichShowPrint = new ArrayList<>();
        }else{
            whichShowPrint = PhotoUtil.transformListOnSmear(context, s);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==LAYOUT_TYPE_DATA_ALL) {
            mFooterView =  LayoutInflater.from(context).inflate(LAYOUT_TYPE_DATA_ALL, parent, false);
          return new RecentTopicAdapter.ViewHolder(mFooterView);
        }else {
            View view = LayoutInflater.from(context).inflate(LAYOUT_TYPE_TOPIC, parent, false);
            return new RecentTopicAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (position<size) {
            Topic topic = topics.get(position);
            Glide.with(context)
                    .load(PhotoUtil.convertTopicImageByWhichs(context,topic.getId(),whichShowPrint,0))
                    .into(holder.topicImage);

            holder.bookName.setText(LitePal.find(Book.class,topic.getBook_id()).getBook_name());
        }
        if (holder.itemView==mFooterView&&topics.size()>5&&position==topics.size()) {
            holder.noMoreData.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public int getItemCount() {
            size = (index+1)*5>=topics.size()?topics.size():(index+1)*5;
            //topic 个数小于5个 不显示footer
            if (topics.size()<5){
                return size;
            }else {
                return size+1;
            }
    }

    public void deleteTopic(int book_id){
        if (book_id != -1){
            for (int i = 0; i < topics.size(); i++) {
                if (topics.get(i).getBook_id()==book_id){
                    topics.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getGroupCount() {
        groupCount = topics.size()%5==0?topics.size()/5-1 :topics.size()/5;  //每页5个，得到总共的页数

        return groupCount;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private int MIN_CLICK_DELAY_TIME = 2500;//多少秒点击一次 默认2.5秒
        private long lastClickTime = 0L;

        ImageView topicImage;
        TextView bookName;
        TextView noMoreData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (itemView==mFooterView) {
                noMoreData = itemView.findViewById(R.id.no_more_data);
                return;
            }

            topicImage = itemView.findViewById(R.id.recent_topic_image);
            bookName = itemView.findViewById(R.id.recent_topic_book_name);

            topicImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //防止多次点击
                    if (System.currentTimeMillis() - lastClickTime < MIN_CLICK_DELAY_TIME){
                        return;
                    }
                    lastClickTime = System.currentTimeMillis();

                    Intent intent = new Intent(context, TopicViewPageActivity.class);
                    intent.putExtra(ConstantsUtil.BOOK_ID, -1);
                    intent.putExtra(ConstantsUtil.TOPIC_POSITION, getAdapterPosition());
                    context.startActivity(intent);
                }
            });

        }
    }


    public void setData(List<Topic> topicList) {
        this.topics = topicList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == size) {
            return LAYOUT_TYPE_DATA_ALL;
        }else {
           return LAYOUT_TYPE_TOPIC;
        }
    }




}
