package com.luckyxmobile.correction.bean;

import com.luckyxmobile.correction.dao.impl.CorrectionLab;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

/**
 * 复习卷的数据库表Paper
 * @author qjj
 * @date 2019/7/24(up)
 */
public class Paper extends LitePalSupport {

    /**
     * 复习卷id
     */
    private int id;

    /**
     * 复习卷名称
     * paper_name
     */
    private String paper_name;

    /**
     * 复习卷的创建时间
     * paper_creation_time
     */
    private Date paper_create_time;

    public Paper (){
        this.paper_create_time = CorrectionLab.getNowDate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaper_name() {
        return paper_name;
    }

    public void setPaper_name(String paper_name) {
        this.paper_name = paper_name;
    }

    public Date getPaper_create_time() {
        return paper_create_time;
    }

    public void setPaper_create_time(Date paper_create_time) {
        this.paper_create_time = paper_create_time;
    }

    @Override
    public String toString() {
        return "Paper{" +
                "id=" + id +
                ", paper_name='" + paper_name + '\'' +
                ", paper_create_time=" + paper_create_time +
                '}';
    }
}
