package com.luckyxmobile.correction.bean;

import org.litepal.crud.LitePalSupport;

/**
 * 错题和复习卷的关联表
 * @author yh&qjj(up)
 * @date 2019/7/24
 */
public class Paper_Topic extends LitePalSupport {

    /**
     * 复习卷与错题关联表的id
     */
    private int id;

    /**
     * 复习卷id
     * paper_id
     */
    private int paper_id;

    /**
     * 错题id
     * topic_id
     */
    private int topic_id;


    public int getpaper_id() {
        return paper_id;
    }

    public void setpaper_id(int paper_id) {
        this.paper_id = paper_id;
    }

    public int getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(int topic_id) {
        this.topic_id = topic_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPaper_id() {
        return paper_id;
    }

    public void setPaper_id(int paper_id) {
        this.paper_id = paper_id;
    }


    @Override
    public String toString() {
        return "Paper_Topic{" +
                "id=" + id +
                ", paper_id=" + paper_id +
                ", topic_id=" + topic_id +
                '}';
    }
}
