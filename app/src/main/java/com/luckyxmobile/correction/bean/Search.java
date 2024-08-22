package com.luckyxmobile.correction.bean;
/**
 * @created by Android Studio
 * @author DongErHeng
 * @date 2019/7/27
 * */

public class Search {
    private int id;
    private String name;
    private int imageId;

    public Search(String name, int imageId){
        this.name = name;
        this.imageId = imageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
