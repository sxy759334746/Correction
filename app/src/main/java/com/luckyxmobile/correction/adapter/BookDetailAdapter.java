package com.luckyxmobile.correction.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.like.LikeButton;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.CorrectionLab;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;
import com.luckyxmobile.correction.dao.impl.TopicDaoImpl;
import com.luckyxmobile.correction.ui.activity.TopicViewPageActivity;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.FastJsonUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.luckyxmobile.correction.util.SDCardUtil;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import org.litepal.LitePal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author LiuGen
 * @date 2019/7/23
 *
 *@author 修改 qjj
 * */
public class BookDetailAdapter extends RecyclerView.Adapter<BookDetailAdapter.ViewHolder> implements Filterable {

    private String TAG = "BookDetailAdapter";
    private Context mContext;
    /**是否显示删除*/
    public static boolean mShowDelete = false;
    /**该错题本所有的题（数据库内）*/
    private List<Topic> topics;
    /**该错题本的题（过滤）*/
    private List<Topic> topicsFilter;
    /**该错题本要删除的题*/
    private List<Topic> topicsDelete;
    private int book_id;
    private List<String> whichShowPrint;
    private SharedPreferences preferences;


    public BookDetailAdapter(Context context, List<Topic> topics, int book_id){
        this.mContext = context;
        this.topics = topics;
        this.topicsFilter = topics;
        this.book_id = book_id;
        preferences = context.getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);
        String s = preferences.getString(ConstantsUtil.TABLE_SHOW_SMEAR,"");
        if (TextUtils.isEmpty(s)){
            whichShowPrint = new ArrayList<>();
        }else{
            whichShowPrint = PhotoUtil.transformListOnSmear(context, s);
        }
    }

    public void upTopics(List<Topic> topics){
        this.topics = topics;
        this.topicsFilter = topics;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(mContext).inflate( R.layout.recycle_item_book_topic, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        // 获取错题对象
        Topic topic = topicsFilter.get(position);

        holder.topicDate.setText(getTopicDate(topic.getTopic_create_time()));

        if (topic.isTopic_collection() == 1){
            holder.likeButton_collection.setLiked(true);
        }else{
            holder.likeButton_collection.setLiked(false);
        }

        //@solved 解决在BookDetailActivity页面点击GridView也会触发CheckBox的选中事件的BUG
        // 设置CheckBox不可点击，同时修改selector_correction_collection中的样式
        holder.likeButton_collection.setEnabled(false);
        holder.likeButton_collection.setClickable(false);

        // 加载原题图片
        Glide.with(mContext)
                .load(PhotoUtil.convertTopicImageByWhichs(mContext,topic.getId(),whichShowPrint,0))
                .thumbnail(0.5f)
                .into(holder.image_correction);

        //设置CardView的点击波纹效果
        Drawable foreGroundAnimation = holder.cardView.getForeground();
        holder.itemChecked.setImageResource(R.drawable.item_uncheck);
        holder.frameLayout.setBackgroundResource(R.color.white);

        if (mShowDelete){
            // 若显示了垃圾箱，则不显示点击波纹效果
            holder.cardView.setForeground(null);
            holder.itemChecked.setVisibility(View.VISIBLE);
        }else{
            holder.cardView.setForeground(foreGroundAnimation);
            holder.itemChecked.setVisibility(View.GONE);
        }

        // 在错题本详情界面显示标签布局
        holder.tagLayout.setAdapter(new TagAdapter<Tag>(TagDaoImpl.findTagByTopic(LitePal.find(Topic.class, topic.getId()).getTopic_tag())) {
            @Override
            public View getView(FlowLayout parent, int position, Tag tag) {

                TextView tagText = (TextView) LayoutInflater.from(mContext).inflate
                        (R.layout.flow_item_tag_on_book, holder.tagLayout, false);
                tagText.setText(tag.getTag_name());
                tagText.setClickable(false);
                return tagText;
            }
        });

    }

    @Override
    public int getItemCount() {
        return topicsFilter.size();
    }


    //重写getFilter()方法
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {


                String tagsum = String.valueOf(constraint);

                if (tagsum.equals("")){
                    topicsFilter = topics;
                }else{
                    topicsFilter = TagDaoImpl.findTopicsByTags(topics, tagsum);
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = topicsFilter;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                topicsFilter = (List<Topic>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    /**
     * 加载布局的Holder
     * */
    public class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;
        private LikeButton likeButton_collection;
        private ImageView image_correction;
        private TagFlowLayout tagLayout;
        private ImageView itemChecked;
        private FrameLayout frameLayout;
        private TextView topicDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_collection);
            likeButton_collection = itemView.findViewById(R.id.likebutton_correction);
            image_correction = itemView.findViewById(R.id.image_correction);
            tagLayout = itemView.findViewById(R.id.tags_showed_on_book);
            itemChecked = itemView.findViewById(R.id.item_Checked);
            frameLayout = itemView.findViewById(R.id.frame_layout);
            topicDate = itemView.findViewById(R.id.topic_date);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mShowDelete){
                        Intent intent = new Intent(mContext, TopicViewPageActivity.class);
                        intent.putExtra(ConstantsUtil.BOOK_ID, book_id);
                        intent.putExtra(ConstantsUtil.TOPIC_POSITION, findPositionByFilter(getAdapterPosition()));
                        mContext.startActivity(intent);
                    }else{
                        if (TopicDaoImpl.isTopicsContainTopic(topicsDelete,topicsFilter.get(getAdapterPosition()))){
                            topicsDelete.remove(topicsFilter.get(getAdapterPosition()));
                            itemChecked.setImageResource(R.drawable.item_uncheck);
                            frameLayout.setBackgroundResource(R.color.white);
                        }else{
                            topicsDelete.add(topicsFilter.get(getAdapterPosition()));
                            itemChecked.setImageResource(R.drawable.topic_delete_check);
                            frameLayout.setBackgroundResource(R.drawable.check_delete_background);
                        }
                    }
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mShowDelete = true;
                    topicsDelete = new ArrayList<>();
                    onItemListener.onItemLongClickListener(getAdapterPosition());
                    notifyDataSetChanged();
                    return false;
                }
            });
        }
    }



    private int findPositionByFilter(int positionFilter){

        int positionSource = -1;

        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).getId() == topicsFilter.get(positionFilter).getId()){
                positionSource = i;
                return positionSource;
            }
        }

        return positionSource;
    }

    /**
     * 点击事件监听接口
     */
    public interface onItemListener {
        void onItemLongClickListener(int position);
    }

    private onItemListener onItemListener = null;

    public void setOnItemListener (onItemListener mListener) {
        this.onItemListener = mListener;
    }

    /**
     * 错题删除
     * @author qmn
     */
    public void deleteTopics() {

        for (Topic topic : topicsDelete){
            SDCardUtil.cascadeDeleteTopic(topic.getId(), mContext);
            CorrectionLab.deleteTopic(topic.getId());
        }
    }

    private String getTopicDate(Date topicDate) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd");
        return format0.format(topicDate);
    }
}
