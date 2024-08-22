package com.luckyxmobile.correction.dao.impl;

import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.dao.impl.CorrectionLab;

import org.junit.Assert;
import org.junit.Test;
import org.litepal.LitePal;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class CorrectionLabTest {

    /**
     * 测试添加标签方法
     * @author qjj
     */
    @Test
    public void addTag() {

    }

    /**
     * 测试删除标签方法
     * @author qjj
     */
    @Test
    public void deleteTag() {


    }

    /**
     * 测试添加错题本方法
     * @author qmn
     */
    @Test
    public void addBook() {
        //添加两条数据
      Book book1 = new Book("book_Test1","image.jpg");
      Book book2 = new Book("book_Test2","image.jpg");
      assertTrue(CorrectionLab.addBook(book1));
      assertTrue(CorrectionLab.addBook(book2));

      //测试后删除
        CorrectionLab.deleteBook(book1.getId());
        CorrectionLab.deleteBook(book2.getId());
    }


    /**
     * 测试删除错题本方法
     * @author qmn
     */
    @Test
    public void deleteBook() {
        //添加两条数据
        Book book1 = new Book("bookTest1","image.jpg");
        Book book2 = new Book("bookTest2","image.jpg");
        CorrectionLab.addBook(book1);
        CorrectionLab.addBook(book2);
        //执行删除方法
        CorrectionLab.deleteBook(book1.getId());
        CorrectionLab.deleteBook(book2.getId());
        //查询错题本
        List<Book> books1 = LitePal.where("id=?" ,String.valueOf(book1.getId())).find(Book.class);
        List<Book> books2 = LitePal.where("id=?",String.valueOf(book2.getId())).find(Book.class);
        Assert.assertEquals(0,books1.size());
        Assert.assertEquals(0,books2.size());

    }
}