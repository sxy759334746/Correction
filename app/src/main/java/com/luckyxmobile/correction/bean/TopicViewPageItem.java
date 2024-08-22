package com.luckyxmobile.correction.bean;

/**
 * 非数据库映射类
 * 临时保存viewPage数据
 */
public class TopicViewPageItem {

    //图片总数量
    private int imagesSum;
    //当前显示图片：默认从0开始 -1：无
    private int currentImage = 0;

    public int getImagesSum() {
        return imagesSum;
    }

    public void setImagesSum(int imagesSum) {
        this.imagesSum = imagesSum;
    }

    public int getCurrentImage() {
        if (currentImage > imagesSum){
            return 0;
        }else{
            return currentImage;
        }

    }

    public void setCurrentImage(int currentImage) {

        this.currentImage = currentImage;
    }
}
