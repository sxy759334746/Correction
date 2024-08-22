package com.luckyxmobile.correction.dao;

import com.luckyxmobile.correction.bean.Topic;

import java.util.List;

/**
 * 复习卷和错题的数据库操作
 *
 * @author yh
 */
public interface PaperTopicDao {

    int ERROR = -1;
    int OK = 0;
    // 增
    Integer save(Integer paper_id, List<Integer> topic_ids);

    // 根据PaperId删除，删除paperId下的全部错题
    Integer deleteByPaperId(Integer paper_id);

    // 删除paperId下的某个topicId
    Integer deleteByTopicId(Integer paper_id,Integer topic_id);

    // 改
    Integer update(Integer paper_id, List<Integer> topic_ids);


    // 根据topic_id查paper_id列表
    List<String> selectByTopicId(Integer topic_id);

    // 根据一个paperid 查询这个复习卷里的复习题
    List<Topic> selectPaper(Integer paper_id);

}
