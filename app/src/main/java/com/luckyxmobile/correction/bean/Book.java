package com.luckyxmobile.correction.bean;


import com.luckyxmobile.correction.dao.impl.CorrectionLab;

import org.litepal.crud.LitePalSupport;

import java.util.Date;


/**
 * 错题本的数据库表Book
 * @author qjj
 * @date 2019/7/24(up)
 */
public class Book extends LitePalSupport {

    /**
     *错题本id
     */
    private int id;

    /**
     * 错题本名称
     *book_name
     */
    private String book_name;

    /**
     * 错题本封面(存储图片路径)
     *book_cover
     */
    private String book_cover;

    /**
     *错题本新建时间
     * book_create_time
     */
    private Date book_create_time;


    public Book (String bookName,String cover){
        book_name = bookName;
        book_cover = cover;
        this.book_create_time = CorrectionLab.getNowDate();
    }

    public Book() {
        this.book_create_time = CorrectionLab.getNowDate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getBook_name() {
        return book_name;
    }

    public String getBook_cover() {
        return book_cover;
    }

    public void setBook_cover(String book_cover) {
        this.book_cover = book_cover;
    }

    public Date getBook_create_time() {
        return book_create_time;
    }

    public void setBook_create_time(Date book_create_time) {
        this.book_create_time = book_create_time;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", book_name='" + book_name + '\'' +
                ", book_cover='" + book_cover + '\'' +
                ", book_create_time=" + book_create_time +
                '}';
    }
}
