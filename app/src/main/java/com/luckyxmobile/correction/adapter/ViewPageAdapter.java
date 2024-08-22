package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.bean.TopicImagesPaint;
import com.luckyxmobile.correction.bean.TopicViewPageItem;
import com.luckyxmobile.correction.ui.view.SeePaintsByClickView;
import com.luckyxmobile.correction.util.FastJsonUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ViewPageAdapter extends PagerAdapter {

    private Context context;
    private List<Topic> topics;
    private SeePaintsByClickView seePaints;
    private List<String> whichShowPaint = new ArrayList<>();
    public  List<TopicViewPageItem> topicViewPageItemList;

    public ViewPageAdapter(Context context, List<Topic> topics){
        this.context = context;
        this.topics = topics;

        setTopicViewPageItemList();
    }

    public void setTopicViewPageItemList() {

        topicViewPageItemList = new ArrayList<>();
        TopicImagesPaint topicImagesPaint;
        TopicViewPageItem item;

        for (int i = 0; i < topics.size(); i++) {
            topicImagesPaint = FastJsonUtil.jsonToObject(topics.get(i).getTopic_original_picture(), TopicImagesPaint.class);
            if (topicImagesPaint != null){
                item = new TopicViewPageItem();
                item.setCurrentImage(0);
                item.setImagesSum(topicImagesPaint.getPrimitiveImagesPathSize());
                topicViewPageItemList.add(item);
                Log.d( "ViewPageAdapter","setTopicViewPageItemList: " +
                        "sum--"+topicViewPageItemList.get(i).getImagesSum()+
                        ";current--"+topicViewPageItemList.get(i).getCurrentImage());
            }
        }
    }

    public void setCurrentImage(int position,int currentImage){
        TopicViewPageItem item = topicViewPageItemList.get(position);
        item.setCurrentImage(currentImage);
        topicViewPageItemList.set(position,item);
    }

    public TopicViewPageItem getTopicViewPageItem(int position) {
        return topicViewPageItemList.get(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {

        return super.getItemPosition(object);
    }


    @Override
    public int getCount() {
        return topics.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view==object;
    }


    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        View view = View.inflate(context,R.layout.topic_view_page_item,null);

        seePaints = view.findViewById(R.id.see_paints_click);

        seePaints.setWhichShow(whichShowPaint);

        seePaints.setInit(topics.get(position).getId(),topicViewPageItemList.get(position).getCurrentImage());

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {

        container.removeView((View)object);

    }

    public void showPaperImage(List<String> whichs){
        this.whichShowPaint.clear();
        this.whichShowPaint.addAll(whichs);
    }


}
