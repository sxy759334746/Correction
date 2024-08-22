package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
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
import com.luckyxmobile.correction.util.PhotoUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;

public class TopicOriginalAdapter extends RecyclerView.Adapter<TopicOriginalAdapter.ViewHolder> {

    private Context context;

    private Topic topic;

    private List<String> imagesPath;

    private TopicImagesPaint topicImagesPaint;

    private boolean isPrimitiveImage = false;

    private SharedPreferences preferences;

    private List<String> whichShowPrint;

    public TopicOriginalAdapter(Context context,Topic topic){

        this.context = context;
        this.topic = topic;
        imagesPath = getImagesPath();
        preferences = context.getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);
        String s = preferences.getString(ConstantsUtil.TABLE_SHOW_SMEAR,"");
        if (TextUtils.isEmpty(s)){
            whichShowPrint = new ArrayList<>();
        }else{
            whichShowPrint = PhotoUtil.transformListOnSmear(context, s);
        }

    }

    private List<String> getImagesPath(){

        topic = LitePal.find(Topic.class,topic.getId());
        topicImagesPaint = FastJsonUtil.jsonToObject(topic.getTopic_original_picture(), TopicImagesPaint.class);

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

        Bitmap bitmap = PhotoUtil.convertTopicImageByWhichs(context,topic.getId(),whichShowPrint,position);

        if (isPrimitiveImage){
            bitmap = PhotoUtil.getBitmapByImagePath(imagesPath.get(position));
        }

        Glide.with(context)
                .asBitmap()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .load(bitmap)
                .fitCenter()
                .thumbnail(0.5f)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
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
                    if (imagesPath.size()>1){
                        assert topicImagesPaint != null;
                        topicImagesPaint.removeWhichImage(getAdapterPosition(),context);
                        topic.setTopic_original_picture(FastJsonUtil.objectToJson(topicImagesPaint));
                        topic.save();
                        imagesPath = getImagesPath();
                        notifyItemRemoved(getAdapterPosition());

                    }else{
                        Toasty.warning(context, R.string.warning_picture, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }
}
