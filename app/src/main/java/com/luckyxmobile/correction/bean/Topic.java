package com.luckyxmobile.correction.bean;

import com.luckyxmobile.correction.dao.impl.CorrectionLab;

import org.litepal.crud.LitePalSupport;

import java.util.Date;
import java.util.Objects;

/**
 * 错题的数据库表Topic
 * @author qjj
 * @date 2019/7/24(up)
 */
public class Topic extends LitePalSupport {


    /**
     * 错题id
     */
    private int id;

    /**
     * 错题本id（用于关联错题本）
     * book_id
     */
    private int book_id;

    /**
     * 原题题干-文字
     * topic_original_text
     */
    private String topic_original_text;

    /**
     * 原题题干-图片
     * topic_original_picture
     */
    private String topic_original_picture;

    /**
     * 错解-文字
     * topic_error_solution_text
     */
    private String topic_error_solution_text;

    /**
     * 错解-图片
     * topic_error_solution_picture
     */
    private String topic_error_solution_picture;

    /**
     * 正解-文字
     * topic_right_solution_text
     */
    private String topic_right_solution_text;

    /**
     * 正解-图片
     * topic_right_solution_picture
     */
    private String topic_right_solution_picture;

    /**
     * 考点&知识点-文字
     * topic_knowledge_point_text
     */
    private String topic_knowledge_point_text;

    /**
     * 考点&知识点-图片
     * topic_knowledge_point_picture
     */
    private String topic_knowledge_point_picture;

    /**
     * 错误原因-文字
     * topic_error_cause_text
     */
    private String topic_error_cause_text;

    /**
     * 错误原因-图片
     * topic_error_cause_picture
     */
    private String topic_error_cause_picture;

    /**
     * 是否收藏(用于判断是否放在收藏夹中)
     * topic_collection
     */
    private int topic_collection;

    /**
     * 重要程度(待定保留)
     * topic_importance
     */
    private int topic_importance;

    /**
     * 困难程度(待定保留)
     * topic_difficulty
     */
    private int topic_difficulty;


    /**
     * 标签
     * topic_tag
     */
    private int topic_tag;

    /**
     * 添加时间
     * topic_creation_time
     */
    private Date topic_create_time;

    public Topic() {
        this.topic_create_time = CorrectionLab.getNowDate();
    }

    public Topic(String topic_picture) {
        this.topic_original_picture = topic_picture;
        this.topic_create_time = CorrectionLab.getNowDate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBook_id() {
        return book_id;
    }

    public void setBook_id(int book_id) {
        this.book_id = book_id;
    }

    public String getTopic_original_text() {
        return topic_original_text;
    }

    public void setTopic_original_text(String topic_original_text) {
        this.topic_original_text = topic_original_text;
    }

    public String getTopic_original_picture() {
        return topic_original_picture;
    }

    public void setTopic_original_picture(String topic_original_picture) {
        this.topic_original_picture = topic_original_picture;
    }

    public String getTopic_error_solution_text() {
        return topic_error_solution_text;
    }

    public void setTopic_error_solution_text(String topic_error_solution_text) {
        this.topic_error_solution_text = topic_error_solution_text;
    }

    public String getTopic_error_solution_picture() {
        return topic_error_solution_picture;
    }

    public void setTopic_error_solution_picture(String topic_error_solution_picture) {
        this.topic_error_solution_picture = topic_error_solution_picture;
    }

    public String getTopic_right_solution_text() {
        return topic_right_solution_text;
    }

    public void setTopic_right_solution_text(String topic_right_solution_text) {
        this.topic_right_solution_text = topic_right_solution_text;
    }

    public String getTopic_right_solution_picture() {
        return topic_right_solution_picture;
    }

    public void setTopic_right_solution_picture(String topic_right_solution_picture) {
        this.topic_right_solution_picture = topic_right_solution_picture;
    }

    public String getTopic_knowledge_point_text() {
        return topic_knowledge_point_text;
    }

    public void setTopic_knowledge_point_text(String topic_knowledge_point_text) {
        this.topic_knowledge_point_text = topic_knowledge_point_text;
    }

    public String getTopic_knowledge_point_picture() {
        return topic_knowledge_point_picture;
    }

    public void setTopic_knowledge_point_picture(String topic_knowledge_point_picture) {
        this.topic_knowledge_point_picture = topic_knowledge_point_picture;
    }

    public String getTopic_error_cause_text() {
        return topic_error_cause_text;
    }

    public void setTopic_error_cause_text(String topic_error_cause_text) {
        this.topic_error_cause_text = topic_error_cause_text;
    }

    public String getTopic_error_cause_picture() {
        return topic_error_cause_picture;
    }

    public void setTopic_error_cause_picture(String topic_error_cause_picture) {
        this.topic_error_cause_picture = topic_error_cause_picture;
    }


    public int getTopic_collection() {
        return topic_collection;
    }

    public int getTopic_importance() {
        return topic_importance;
    }

    public void setTopic_importance(int topic_importance) {
        this.topic_importance = topic_importance;
    }

    public int getTopic_difficulty() {
        return topic_difficulty;
    }

    public void setTopic_difficulty(int topic_difficulty) {
        this.topic_difficulty = topic_difficulty;
    }

    public int  isTopic_collection() {
        return topic_collection;
    }

    public void setTopic_collection(int topic_collection) {
        this.topic_collection = topic_collection;
    }

    public int getTopic_tag() {
        return topic_tag;
    }

    public void setTopic_tag(int topic_tag) {
        this.topic_tag = topic_tag;
    }

    public Date getTopic_create_time() {
        return topic_create_time;
    }

    public void setTopic_create_time(Date topic_create_time) {
        this.topic_create_time = topic_create_time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Topic topic = (Topic) o;
        return id == topic.id &&
                book_id == topic.book_id &&
                topic_importance == topic.topic_importance &&
                topic_difficulty == topic.topic_difficulty &&
                topic_collection == topic.topic_collection &&
                Objects.equals(topic_original_text, topic.topic_original_text) &&
                Objects.equals(topic_original_picture, topic.topic_original_picture) &&
                Objects.equals(topic_error_solution_text, topic.topic_error_solution_text) &&
                Objects.equals(topic_error_solution_picture, topic.topic_error_solution_picture) &&
                Objects.equals(topic_right_solution_text, topic.topic_right_solution_text) &&
                Objects.equals(topic_right_solution_picture, topic.topic_right_solution_picture) &&
                Objects.equals(topic_knowledge_point_text, topic.topic_knowledge_point_text) &&
                Objects.equals(topic_knowledge_point_picture, topic.topic_knowledge_point_picture) &&
                Objects.equals(topic_error_cause_text, topic.topic_error_cause_text) &&
                Objects.equals(topic_error_cause_picture, topic.topic_error_cause_picture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, book_id, topic_original_text, topic_original_picture, topic_error_solution_text, topic_error_solution_picture, topic_right_solution_text, topic_right_solution_picture, topic_knowledge_point_text, topic_knowledge_point_picture, topic_error_cause_text, topic_error_cause_picture, topic_importance, topic_difficulty, topic_collection);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", book_id=" + book_id +
                ", topic_original_text='" + topic_original_text + '\'' +
                ", topic_original_picture='" + topic_original_picture + '\'' +
                ", topic_error_solution_text='" + topic_error_solution_text + '\'' +
                ", topic_error_solution_picture='" + topic_error_solution_picture + '\'' +
                ", topic_right_solution_text='" + topic_right_solution_text + '\'' +
                ", topic_right_solution_picture='" + topic_right_solution_picture + '\'' +
                ", topic_knowledge_point_text='" + topic_knowledge_point_text + '\'' +
                ", topic_knowledge_point_picture='" + topic_knowledge_point_picture + '\'' +
                ", topic_error_cause_text='" + topic_error_cause_text + '\'' +
                ", topic_error_cause_picture='" + topic_error_cause_picture + '\'' +
                ", topic_collection=" + topic_collection +
                ", topic_importance=" + topic_importance +
                ", topic_difficulty=" + topic_difficulty +
                ", topic_tag=" + topic_tag +
                ", topic_create_time=" + topic_create_time +
                '}';
    }

}
