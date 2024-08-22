package com.luckyxmobile.correction.dao.impl;

import androidx.test.runner.AndroidJUnit4;

import com.luckyxmobile.correction.LitePalTestCase;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Paper;
import com.luckyxmobile.correction.bean.Paper_Topic;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.PaperTopicDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PaperTopicDaoImplTest extends LitePalTestCase {
    private Paper paper;
    private Topic topic1;
    private Topic topic2;
    private Book book;
    private Paper_Topic paper_topic1;
    private Paper_Topic paper_topic2;


    @Before
    public void insertFakeData() {
        paper = new Paper();
        paper.setPaper_name("paper_test");
        assertTrue(paper.save());

        book = new Book();
        book.setBook_cover("image.jpg");
        book.setBook_name("book_test");
        assertTrue(book.save());

        topic1 = new Topic();
        topic1.setBook_id(book.getId());
        topic1.setTopic_original_picture("topic1.jpg");
        assertTrue(topic1.save());
        topic2 = new Topic();
        topic2.setBook_id(book.getId());
        topic2.setTopic_original_picture("topic2.jpg");
        assertTrue(topic2.save());

        paper_topic1 = new Paper_Topic();
        paper_topic1.setpaper_id(paper.getId());
        paper_topic1.setTopic_id(topic1.getId());
        assertTrue(paper_topic1.save());
        paper_topic2 = new Paper_Topic();
        paper_topic2.setpaper_id(paper.getId());
        paper_topic2.setTopic_id(topic2.getId());
        assertTrue(paper_topic2.save());

    }

    @After
    public void deleteFakeData() {
        assertEquals(1, LitePal.delete(Paper.class, paper.getId()));
        assertEquals(1, LitePal.delete(Topic.class, topic1.getId()));
        assertEquals(1, LitePal.delete(Topic.class, topic2.getId()));
        assertEquals(1, LitePal.delete(Book.class, book.getId()));
        LitePal.delete(Paper_Topic.class, paper_topic1.getId());
        LitePal.delete(Paper_Topic.class, paper_topic2.getId());
    }
    @Test
    public void save() {
        assertEquals((Integer) PaperTopicDao.OK, PaperTopic.save(1, new ArrayList<Integer>() {
            {
                for (int i = 0; i < 10; ++i) {
                    add(i);
                }
            }
        }));
    }


    @Test
    public void selectPaper() {
        assertEquals(2, PaperTopic.selectPaper(paper.getId()).size());
        assertEquals(0, PaperTopic.selectPaper(0).size());
        assertEquals(0, PaperTopic.selectPaper(null).size());

    }

    @Test
    public void selectByTopicId() {
        assertEquals(1, PaperTopic.selectByTopicId(topic1.getId()).size());
        assertEquals(0, PaperTopic.selectByTopicId(-1).size());
        assertEquals(0, PaperTopic.selectByTopicId(Integer.MAX_VALUE).size());
        assertEquals(0, PaperTopic.selectByTopicId(0).size());
        assertEquals(0, PaperTopic.selectByTopicId(null).size());


    }

    @Test
    public void update() {
        assertEquals((Integer) PaperTopicDao.OK, PaperTopic.update(paper.getId(), new ArrayList<Integer>() {{
            add(topic1.getId());
        }}));
    }


    @Test
    public void deleteByTopicId() {
        paper_topic1.save();
        paper_topic2.save();

        assertEquals(0, (int) PaperTopic.deleteByTopicId(paper.getId(), null));
        assertEquals(0, (int) PaperTopic.deleteByTopicId(paper.getId(), 0));
        assertEquals(1, (int) PaperTopic.deleteByTopicId(paper.getId(), topic1.getId()));
    }

    @Test
    public void deleteByPaperId() {
        paper_topic1.save();
        paper_topic2.save();

        assertEquals(2, (int) PaperTopic.deleteByPaperId(paper.getId()));
    }
}