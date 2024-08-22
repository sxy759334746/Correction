package com.luckyxmobile.correction.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Book;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SelectBookAdapter extends RecyclerView.Adapter<SelectBookAdapter.ViewHolder> implements View.OnClickListener {
    private List<Book> bookList;
    private OnItemClickListener onItemClickListener = null;
    //选择的下标
    public int selectItem = 0;

    public SelectBookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.select_book_item,null);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bookName.setText(book.getBook_name());
        holder.bookImage.setImageResource(R.drawable.correction_book);
        if (position == selectItem) {
            holder.bookSelect.setVisibility(View.VISIBLE);
        }else {
            holder.bookSelect.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setTag(position);

    }

    @Override
    public int getItemCount() {
        return bookList.size() ;
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onItemClick(v,(int)v.getTag());
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView bookName;
        ImageView bookSelect;
        ImageView bookImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookName = itemView.findViewById(R.id.book_name);
            bookImage = itemView.findViewById(R.id.book_cover);
            bookSelect = itemView.findViewById(R.id.item_selected);
        }
    }

   public interface OnItemClickListener {
        void onItemClick(View view,int position);
   }

   public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
   }

    /**
     * 将bookid转为在适配器里booklist的下标
     * @param bookid 错题本的id
     * @return 若存在返回index，否则返回0
     */
   public int transferBookIdToIndex(int bookid){
        for(int i = 0; i < bookList.size(); i++){
            if(bookList.get(i).getId() == bookid){
                return i;
            }
        }
        return 0;
   }
}
