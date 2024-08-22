package com.luckyxmobile.correction.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Book;

import java.util.List;

public class DialogSelectAdapter extends BaseAdapter {

    private Context context;
    private List<Book> books;

    public DialogSelectAdapter(Context context, List<Book> books) {
        this.context = context;
        this.books = books;
        books.remove(0);
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Book book = books.get(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_view_item,parent,false);
        }else {
            view = convertView;
        }

        ImageView bookCover = view.findViewById(R.id.book_cover);
        TextView bookTitle = view.findViewById(R.id.book_title);
        bookTitle.setText(book.getBook_name());
        return view;
    }
}
