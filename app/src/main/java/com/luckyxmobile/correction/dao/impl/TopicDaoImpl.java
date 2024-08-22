package com.luckyxmobile.correction.dao.impl;

import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.TopicDao;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class TopicDaoImpl implements TopicDao {
    @Override
    public List<Topic> selectTopic(List<String> filter_book, List<String> select_label) {
        List<Topic> topics = LitePal.findAll(Topic.class);
        List<Topic> topics2;
        List<Topic> topics3 = new ArrayList<>();
        if (filter_book == null || select_label == null) {
            return new ArrayList<>();
        }
        if (filter_book.size() == 0 && select_label.size() == 0) {
            return topics;
        }
        for (int i = 0; i < filter_book.size(); i++) {
            int book_id = Integer.parseInt(filter_book.get(i));
            if (book_id == 1) {
                topics2 = LitePal.where("topic_collection = ?", "1").find(Topic.class);
            } else {
                topics2 = LitePal.where("book_id = ?", filter_book.get(i)).find(Topic.class);
            }
            topics3.addAll(topics2);
        }
        if (!topics3.isEmpty() || !filter_book.isEmpty()) {
            topics.retainAll(topics3);
            topics3.clear();
        }
        Integer topic_tag = 0;

        for (int i = 0; i < select_label.size(); i++) {
            topic_tag += chaiFen(Integer.parseInt(select_label.get(i)));
            topics2 = TagDaoImpl.findTopicByTag(topic_tag);
            topics3.addAll(topics2);
        }
        if (!topics3.isEmpty() || !select_label.isEmpty()) {
            topics.retainAll(topics3);
            topics3.clear();
        }
        return topics;
    }

    /**
     * 把一个任意正整数拆解成几个是2的n次方的数
     *
     * @param input 传入的正整数
     * @return
     */
    private Integer chaiFen(int input) {
        int sum = 0;
        int m = 1;
        for (int i = 0; i < 32; i++) {
            if ((m & input) == m) {
                sum += m;
            }
            m = m << 1;
        }
        return sum;
    }

    @Override
    public List<Topic> selectTopicById(List<Integer> topic_id) {
        List<Topic> topicList = new ArrayList<>();
        if (topic_id == null) {
            return new ArrayList<>();
        }

        for (int i = 0; i < topic_id.size(); i++) {
            List<Topic> topics = LitePal.where("id = ?", String.valueOf(topic_id.get(i))).find(Topic.class);
            topicList.addAll(topics);
        }
        return topicList;
    }

    public static boolean isTopicsContainTopic(List<Topic> topics, Topic topic){

        for (Topic t :topics){
            if (t.getId() == topic.getId()){
                return true;
            }
        }
        return false;
    }
}
