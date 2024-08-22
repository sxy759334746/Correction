package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.Target;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.bean.TopicImagesPaint;
import com.luckyxmobile.correction.ui.activity.TopicActivity;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.FastJsonUtil;
import com.luckyxmobile.correction.util.ImageUtil;
import com.luckyxmobile.correction.util.PhotoUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class TopicPointAdapter extends RecyclerView.Adapter<TopicPointAdapter.ViewHolder> {

    private Context context;

    private Topic topic;

    private List<String> imagesPath;

    private TopicImagesPaint topicImagesPaint;

    private boolean isPrimitiveImage = false;

    public TopicPointAdapter(Context context, Topic topic, LinearLayout linearLayout){

        this.context = context;
        this.topic = topic;

        imagesPath = getImagesPath();

        if (imagesPath.size()>0){
            linearLayout.setVisibility(View.VISIBLE);
        }

        String text = topic.getTopic_knowledge_point_text();

        if (text != null){
            text = text.trim();
            if (!text.isEmpty()){
                linearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private List<String> getImagesPath(){
        topic = LitePal.find(Topic.class,topic.getId());
        topicImagesPaint = FastJsonUtil.jsonToObject(topic.getTopic_knowledge_point_picture(), TopicImagesPaint.class);

        if (topicImagesPaint == null){
            return new ArrayList<>();
        }else{
            return topicImagesPaint.getPrimitiveImagePathList();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item_topic_images, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (!TopicActivity.isEditing){
            holder.deleteImage.setVisibility(View.GONE);
        }else{
            holder.deleteImage.setVisibility(View.VISIBLE);
        }

        String contrastRadio;
        try {
            contrastRadio = topicImagesPaint.getImageContrastRadioList().get(position);
        }catch (Exception e){
            contrastRadio = ConstantsUtil.CONTRAST_RADIO_COMMON;
        }

        Bitmap bitmap = ImageUtil.setImageContrastRadioByPath(contrastRadio,imagesPath.get(position));

        if (isPrimitiveImage){
            bitmap = PhotoUtil.getBitmapByImagePath(imagesPath.get(position));
        }

        holder.imageView.setPadding(16,8,16,8);
        Glide.with(context)
                .asBitmap()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .load(bitmap)
                .fitCenter()
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .thumbnail(0.5f)
                .into(holder.imageView);

    }


    @Override
    public int getItemCount() {
        return imagesPath.size();
    }

    public void flashImagePath(){
        imagesPath = getImagesPath();
        notifyDataSetChanged();
    }

    public void showPrimitiveImages(boolean isPrimitiveImage){
        this.isPrimitiveImage = isPrimitiveImage;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout frameLayout;
        private ImageView imageView;
        private ImageView deleteImage;


        public ViewHolder(View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.item_topic_image_layout);
            imageView = itemView.findViewById(R.id.item_topic_image);
            deleteImage = itemView.findViewById(R.id.item_topic_image_delete);


            frameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TopicActivity.showImageBiggerDialog(imagesPath,context,getAdapterPosition());
                }
            });

            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    assert topicImagesPaint != null;
                    topicImagesPaint.removeWhichImage(getAdapterPosition(),context);
                    topic.setTopic_knowledge_point_picture(FastJsonUtil.objectToJson(topicImagesPaint));
                    topic.save();
                    imagesPath = getImagesPath();
                    notifyItemRemoved(getAdapterPosition());

                }
            });
        }
    }
}
