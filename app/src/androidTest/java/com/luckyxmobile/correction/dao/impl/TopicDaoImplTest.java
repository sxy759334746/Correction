package com.luckyxmobile.correction.dao.impl;

import android.util.Log;

import androidx.test.runner.AndroidJUnit4;

import com.luckyxmobile.correction.LitePalTestCase;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Paper;
import com.luckyxmobile.correction.bean.Paper_Topic;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.TopicDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TopicDaoImplTest extends LitePalTestCase {
    private Paper paper;
    private Topic topic1;
    private Topic topic2;
    private Book book;
    private Book star;
    private Paper_Topic paper_topic1;
    private Paper_Topic paper_topic2;

    @Before
    public void insertFakeData() {
        paper = new Paper();
        paper.setPaper_name("paper_test");
        assertTrue(paper.save());

        star = new Book();
        star.setBook_cover("image.jpg");
        star.setBook_name("收藏");
        assertTrue(star.save());

        book = new Book();
        book.setBook_cover("image.jpg");
        book.setBook_name("book_test");
        assertTrue(book.save());

        topic1 = new Topic();
        topic1.setBook_id(book.getId());
        topic1.setTopic_original_picture("topic1.jpg");
        topic1.setTopic_difficulty(1);
        topic1.setTopic_importance(5);
        topic1.setTopic_tag(1);
        assertTrue(topic1.save());
        topic2 = new Topic();
        topic2.setBook_id(book.getId());
        topic2.setTopic_original_picture("topic2.jpg");
        topic2.setTopic_difficulty(5);
        topic2.setTopic_importance(1);
        topic2.setTopic_tag(7);
        assertTrue(topic2.save());

        paper_topic1 = new Paper_Topic();
        paper_topic1.setpaper_id(paper.getId());
        paper_topic1.setTopic_id(topic1.getId());
        assertTrue(paper_topic1.save());
        paper_topic2 = new Paper_Topic();
        paper_topic2.setpaper_id(paper.getId());
        paper_topic2.setTopic_id(topic2.getId());
        assertTrue(paper_topic2.save());

        Log.d(TAG, "insertFakeData: init fake data");

    }

    @After
    public void deleteFakeData() {
        assertEquals(1, LitePal.delete(Paper.class, paper.getId()));
        assertEquals(1, LitePal.delete(Topic.class, topic1.getId()));
        assertEquals(1, LitePal.delete(Topic.class, topic2.getId()));
        assertEquals(1, LitePal.delete(Book.class, book.getId()));
        LitePal.delete(Paper_Topic.class, paper_topic1.getId());
        LitePal.delete(Paper_Topic.class, paper_topic2.getId());
        Log.d(TAG, "deleteFakeData: delete fake data");
    }

    @Test
    public void selectTopicById() {
        TopicDao topicDao = new TopicDaoImpl();
        List<Integer> topics = new ArrayList<>();

        topics.add(topic1.getId());
        topics.add(topic2.getId());

        List<Topic> topicList = topicDao.selectTopicById(topics);
        for (Topic topic : topicList) {
            assertTrue(topics.contains(topic.getId()));
        }
        assertEquals(2, topicList.size());
        assertNotNull(topicDao.selectTopicById(null));
        assertNotNull(topicDao.selectTopicById(new ArrayList<Integer>()));
        assertNotNull(topicDao.selectTopicById(new ArrayList<Integer>() {{
            add(6553555);
        }}));
        assertNotNull(topicDao.selectTopicById(new ArrayList<Integer>() {{
            add(-251);
        }}));

        assertEquals(0, topicDao.selectTopicById(null).size());
        assertEquals(0, topicDao.selectTopicById(new ArrayList<Integer>()).size());
        assertEquals(0, topicDao.selectTopicById(new ArrayList<Integer>() {{
            add(6553555);
        }}).size());
        assertEquals(0, topicDao.selectTopicById(new ArrayList<Integer>() {{
            add(-251);
        }}).size());
        Log.d(TAG, "selectTopicById: select topic by id test succed");
    }


    /**
     * 测试根据错题本，标签筛选复习题
     * 测试数据：book topic1 topic2
     * 测试条件：
     * 1.错题本，标签都为null
     * 2.错题本，标签的size都为0
     * 3.错题本增加book的id，标签的size为0
     * 4.错题本为book的id，标签为1
     * 5.错题本为book的id，标签增加的标签7
     * 6.错题本为book的id，标签为0
     * 7.错题本为book的size为0，标签为0
     * 8.错题本为book的size为0，标签为1
     */
    @Test
    public void testSelectTopic() {
        TopicDao topicDao = new TopicDaoImpl();
        // 错题本，标签都为null
        List<String> book_list = null;
        List<String> label_list = null;
        List<Topic> topicList1;
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为空
        assertTrue(topicList1.isEmpty());

        // 错题本，标签的size都为0
        book_list = new ArrayList<>();
        label_list = new ArrayList<>();
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为全部
        assertTrue(topicList1.size() >= 2);
        // 筛选出的结果包含topic1
        assertTrue(topicList1.contains(topic1));
        // 筛选出的结果包含topic2
        assertTrue(topicList1.contains(topic2));

        // 错题本增加book的id，标签的size为0
        book_list.add(String.valueOf(book.getId()));
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为全部
        assertTrue(topicList1.size() >= 2);
        // 筛选出的结果包含topic1
        assertTrue(topicList1.contains(topic1));
        // 筛选出的结果包含topic2
        assertTrue(topicList1.contains(topic2));

        // 错题本为book的id，标签为1
        label_list.add("1");
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为全部
        assertTrue(topicList1.size() >= 2);
        // 筛选出的结果包含topic1
        assertTrue(topicList1.contains(topic1));
        // 筛选出的结果包含topic2
        assertTrue(topicList1.contains(topic2));

        // 错题本为book的id，标签增加的标签7
        label_list.add("7");
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为全部
        assertTrue(topicList1.size() >= 2);
        // 筛选出的结果包含topic1
        assertTrue(topicList1.contains(topic1));
        // 筛选出的结果包含topic2
        assertTrue(topicList1.contains(topic2));

        // 错题本为book的id，标签为0
        label_list.clear();
        label_list.add("0");
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为空
        assertTrue(topicList1.isEmpty());

        // 错题本为book的size为0，标签为0
        book_list.clear();
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为空
        assertTrue(topicList1.isEmpty());

        // 错题本为book的size为0，标签为1
        label_list.clear();
        label_list.add("1");
        topicList1 = topicDao.selectTopic(book_list, label_list);
        // 筛选出的结果为全部
        assertTrue(topicList1.size() >= 2);
        // 筛选出的结果包含topic1
        assertTrue(topicList1.contains(topic1));
        // 筛选出的结果包含topic2
        assertTrue(topicList1.contains(topic2));

    }
}