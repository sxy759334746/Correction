package com.luckyxmobile.correction.ui.view;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.luckyxmobile.correction.util.SDCardUtil;

import org.opencv.photo.Photo;

import es.dmoral.toasty.Toasty;

public class AddBookDialog extends AlertDialog.Builder {

    private ImageButton alterBookCoverBtn;
    private ImageButton deleteBookCoverBtn;
    private ImageView bookCover;
    private EditText bookNameEt;
    private TextView bookNameNum;
    private Book book;
    private boolean isNewAdd = true;


    public AddBookDialog(@NonNull Context context, Book book,boolean isNewAdd) {
        super(context);
        this.book = book;
        this.isNewAdd = isNewAdd;
        View view =  LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_book,null);
        setView(view);

        alterBookCoverBtn = view.findViewById(R.id.alter_cover_image);
        bookCover = view.findViewById(R.id.coverImg);
        bookNameEt = view.findViewById(R.id.bookNameEdt);
        bookNameNum = view.findViewById(R.id.bookNameEdtNum);
        deleteBookCoverBtn = view.findViewById(R.id.delete_book_cover);

        if (!isNewAdd){
            bookNameEt.setText(book.getBook_name());
            bookNameNum.setText(bookNameEt.length()+"/10");

            Glide.with(getContext()).load(book.getBook_cover())
                    .placeholder(R.drawable.correction_book)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .into(bookCover);

            if (book.getBook_cover().equals("default")){
                deleteBookCoverBtn.setVisibility(View.GONE);
            }else{
                deleteBookCoverBtn.setVisibility(View.VISIBLE);
            }
        }



        deleteBookCoverBtn.setOnClickListener(view1 -> {
            SDCardUtil.deleteFile(book.getBook_cover(),context);
            PhotoUtil.resetResultPath();
            setBookCover();
        });

        //输入框字数提示和限制
        bookNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                bookNameNum.setText(s.length()+"/10");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                bookNameNum.setText(s.length()+"/10");
            }
        });
    }

    public void setBookCover() {
        Glide.with(getContext()).load(PhotoUtil.resultPath)
                .placeholder(R.drawable.correction_book)
                .skipMemoryCache(true) // 不使用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .into(bookCover);

        if (PhotoUtil.resultPath.equals("default")){
            deleteBookCoverBtn.setVisibility(View.GONE);
        }else{
            deleteBookCoverBtn.setVisibility(View.VISIBLE);
        }
    }

    public ImageButton getAlterBookCoverBtn() {
        return alterBookCoverBtn;
    }

    public Book getSaveBook(){

        if (bookNameEt.getText().length() < 1){
            Toasty.warning(getContext(),R.string.empty_input, Toast.LENGTH_SHORT, true).show();
            return null;
        }else{
            Toasty.success(getContext(), R.string.successful, Toast.LENGTH_SHORT, true).show();
            book.setBook_name(bookNameEt.getText().toString());
            book.setBook_cover(PhotoUtil.getResultPath());
            book.save();

            String oldName = SDCardUtil.getBookDIR()  + "/id-0-0-0.jpeg";
            String newName = SDCardUtil.getBookDIR() + "/id-" + book.getId()+".jpeg";
            if (PhotoUtil.resultPath.equals(oldName)){
                SDCardUtil.deleteFile(newName,getContext());
                SDCardUtil.renameFile(oldName,newName);
                book.setBook_cover(newName);
                book.save();
            }
            return book;
        }

    }
}
