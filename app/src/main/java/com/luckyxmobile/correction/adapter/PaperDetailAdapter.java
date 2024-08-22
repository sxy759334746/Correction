package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.ui.activity.BookDetailActivity;
import com.luckyxmobile.correction.ui.activity.PaperDetailActivity;
import com.luckyxmobile.correction.ui.activity.TopicActivity;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.like.LikeButton;
import org.litepal.LitePal;
import java.util.Collections;
import java.util.List;

public class PaperDetailAdapter extends RecyclerView.Adapter<PaperDetailAdapter.ViewHolder> implements ItemTouchAdapter {

    private Context mContext;
    /**
     * 该错题本所有的题（数据库内）
     */
    private List<Topic> topics;
    private List<Book> books;
    private String paperName;

    public PaperDetailAdapter(Context context, List<Topic> topics, String name) {
        this.mContext = context;
        this.paperName = name;
        this.topics = topics;
        books = LitePal.findAll(Book.class);
    }

    @NonNull
    @Override
    public PaperDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /**
         * @author LiuGen
         * @description 这里使用LayoutInflater.from方法，
         * 因为LayoutInflater.from
         * 是将布局的子控件加载完后，又添加到parent布局中的，
         * 关于CaraView的Margin等与需要父布局参照的属性才能得到实现；
         * 而View.inflate()方法只加载了子控件，没有添加到父布局中*/
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.recycle_item_paper_topic, parent, false);
        return new PaperDetailAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PaperDetailAdapter.ViewHolder holder, final int position) {

        // 获取错题对象
        final Topic topic = topics.get(position);

        // 单独设置第一个和最后一个CardView的Margin，达到整体所有CardView的Margin都是8dp
        if (position == 0) {
            // 设置宽和高，顺序为宽、高
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // 4个参数按顺序分别是左上右下，默认是px
            layoutParams.setMargins(dipToPx(mContext, 8), dipToPx(mContext, 8), dipToPx(mContext, 8), dipToPx(mContext, 0));
            holder.cardView.setLayoutParams(layoutParams);
        }
        if (position + 1 == getItemCount()) {
            // 设置宽和高，顺序为宽、高
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // 4个参数按顺序分别是左上右下，默认是px
            layoutParams.setMargins(dipToPx(mContext, 8), dipToPx(mContext, 8), dipToPx(mContext, 8), dipToPx(mContext, 8));
            holder.cardView.setLayoutParams(layoutParams);
        }
        // 加载原题图片
        Glide.with(mContext).load(PhotoUtil.convertTopicImageByWhichs(mContext,topic.getId(),null,0))
                .thumbnail(0.1f)
                .into(holder.image_correction);

        holder.order_correction.setText(position + 1 + "");
        holder.setTopicInfo(IndexPlusBookName(position + 1 + "", getBookName(holder.getAdapterPosition(), books, topics)));

        /**
         * @author ChangHao
         * 滑动过后改变错题的标号
         * 如果原holderpos == frompos则设置题号为topos+1
         * 如果from < to 则把两者中间的所有题号-1 反之+1
         */
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                int holderposition = holder.getAdapterPosition();
                int index = 0;
                if (holderposition == fromPosition) {
                    index = toPosition + 1;
                } else {
                    if (holderposition <= toPosition && holderposition > fromPosition) {
                        index = holderposition;
                    } else if (holderposition < fromPosition && holderposition >= toPosition) {
                        index = holderposition + 2;
                    }
                }
                if (index > 0)
                    holder.setTopicInfo(IndexPlusBookName(index + "", getBookName(index - 1, books, topics)));
            }
        });

        /**@solved 解决在BookDetailActivity页面点击GridView也会触发CheckBox的选中事件*/
        // 设置CheckBox不可点击，同时修改selector_correction_collection中的样式
        holder.likeButton_collection.setEnabled(false);
        holder.likeButton_collection.setClickable(false);

        if (topic.isTopic_collection() == 1) {
            holder.likeButton_collection.setLiked(true);
        } else {
            holder.likeButton_collection.setLiked(false);
        }


    }

    public static String IndexPlusBookName(String index, String bookname) {
        return index + " • " + bookname;
    }

    /**
     * 如果已经有books和topics可以用这个方法
     *
     * @param AdapterPosition topicForTopicActivity 在topics的位置
     * @param books           所有的book
     * @param topics          需要的topic
     * @return book name
     */
    public static String getBookName(int AdapterPosition, List<Book> books, List<Topic> topics) {
        Topic topic = topics.get(AdapterPosition);
        for (Book book1 : books) {
            if (book1.getId() == topic.getBook_id()) {
                return book1.getBook_name();
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(topics, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        topics.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 加载布局的Holder
     */
    class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private CardView cardView;
        private TextView order_correction;
        private LikeButton likeButton_collection;
        private ImageView image_correction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view_collection);
            likeButton_collection = (LikeButton) itemView.findViewById(R.id.likebutton_correction);
            image_correction = (ImageView) itemView.findViewById(R.id.image_correction);
            order_correction = itemView.findViewById(R.id.paper_detail_topic_index);
            //item 点击事件，进入错题详情页面
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到题目详情页面 并传递该题id
                    Intent intent = new Intent(mContext, TopicActivity.class);
                    intent.putExtra(ConstantsUtil.TOPIC_ID, topics.get(getAdapterPosition()).getId());
                    intent.putExtra(ConstantsUtil.TOOLBAR_NAME, paperName);
                    intent.putExtra(ConstantsUtil.WHICH_ACTIVITY, PaperDetailActivity.TAG);
                    mContext.startActivity(intent);
                }
            });
        }

        public void setTopicInfo(String info) {
            this.order_correction.setText(info);
        }

        public String getTopicBook() {
            return this.order_correction.getText().toString();
        }

        @Override
        public void onItemSelected() {
            itemView.setTranslationZ(10);
            itemView.setAlpha(0.9f);
        }

        @Override
        public void onItemClear() {
            itemView.setTranslationZ(0);
            itemView.setAlpha(1);
        }
    }

    /**
     * @editor LiuGen
     * @date 2019/07/25
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * @editor LiuGen
     * @date 2019/07/25
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int pxToDp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
