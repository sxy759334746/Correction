package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Book;
import java.util.List;

/**
 *
 */
public class HeadBookAdapter extends RecyclerView.Adapter<HeadBookAdapter.ViewHolder> {

    private List<Book> bookList;
    private Context context;

    private static final String TAG = "HeadBookAdapter";

    public HeadBookAdapter(Context context,List<Book> bookList){
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycle_item_book, parent, false);
//        view.setOnLongClickListener(this);
        return new HeadBookAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        //设置封面图片和删除效果
        if (position == 0){
            holder.bookImage.setImageResource(R.drawable.ic_favorite);
        }else{

            //Glide 加载封面图
            Glide.with(context)
                    .load(bookList.get(position).getBook_cover())
                    .placeholder(R.drawable.correction_book)
                    .fitCenter()
                    .centerCrop()
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .into(holder.bookImage);
        }


        holder.bookName.setText(bookList.get(position).getBook_name());


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnItemListener.onItemLongClickListener(holder,position,holder.bookLayout);
                return true;
            }
        });

       holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               mOnItemListener.onItemClickListener(holder,position);
           }
       });



    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void addBook(Book book){
        if (book != null){
            bookList.add(book);
            notifyDataSetChanged();
        }
    }

    public void setBook(int position,Book book){
        if (book != null){
            bookList.set(position,book);
            notifyDataSetChanged();
        }
    }


   public class ViewHolder extends RecyclerView.ViewHolder{


        ImageView bookImage;
        TextView bookName;
        ImageView deleteIcon;
        RelativeLayout bookLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bookLayout = itemView.findViewById(R.id.book_layout);
            bookImage = itemView.findViewById(R.id.book_image);
            bookName = itemView.findViewById(R.id.book_name);
            deleteIcon = itemView.findViewById(R.id.delete_book);

        }
    }


    //声明点击、长按接口供外部调用
    public interface onItemListener{
        void onItemClickListener(ViewHolder viewHolder,int position);
        void onItemLongClickListener(ViewHolder viewHolder,int position,View view);
    }

    private onItemListener mOnItemListener = null;

    public void setmOnItemListener(onItemListener listener) {
        this.mOnItemListener = listener;
    }

}
