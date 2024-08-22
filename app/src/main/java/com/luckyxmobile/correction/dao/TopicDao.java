package com.luckyxmobile.correction.dao;

import com.luckyxmobile.correction.bean.Topic;

import java.util.List;

public interface TopicDao {
    /**
     * 根据错题本，困难程度，重要程度筛选复习题
     *
     * @param filter_book  错题本
     * @param select_label 标签
     * @return 筛选出的复习题
     */
    List<Topic> selectTopic(List<String> filter_book, List<String> select_label);


    /**
     * 根据错题id列表查询错题
     *
     * @param topic_id 错题id
     * @return 查询错题id的错题
     */
    List<Topic> selectTopicById(List<Integer> topic_id);
}
