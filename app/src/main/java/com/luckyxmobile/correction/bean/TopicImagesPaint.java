package com.luckyxmobile.correction.bean;
import android.content.Context;
import android.graphics.Point;

import com.luckyxmobile.correction.util.SDCardUtil;

import java.util.ArrayList;
import java.util.List;

/***
 * 记录每张涂抹操作，并转化为json存入数据库（多张图片的可能）
 * @author qujiajun
 */
public class TopicImagesPaint {

    //图片类型(题目，错解，正解，考点，错误原因)
    private String whichImage;

    //图片保存路径(原图s)
    private List<String> primitiveImagePathList = new ArrayList<>();

    //图片s文字大小
    private List<Integer> imageWordSizeList = new ArrayList<>();

    //图片s的对比度
    private List<String> imageContrastRadioList = new ArrayList<>();

    //图片s的涂抹s(每张图片的多次涂抹)
    private List<List<ImagePaint>> imagePaintsList = new ArrayList<>();

    public void removeAllImage(Context context){
        for (String imagePath: getPrimitiveImagePathList()){
            SDCardUtil.deleteFile(imagePath,context);
        }
    }

    public void removeWhichImage(int position, Context context){

        if (getPrimitiveImagePathList().size() > position){
            SDCardUtil.deleteFile(getPrimitiveImagePathList().get(position),context);
            getPrimitiveImagePathList().remove(position);
        }

        if (getImageWordSizeList().size() > position){
            getImageWordSizeList().remove(position);
        }

        if (getImageContrastRadioList().size() > position){
            getImageContrastRadioList().remove(position);
        }

        if (getImagePaintsList().size() > position){
            getImagePaintsList().remove(position);
        }

    }

    public String getWhichImage() {
        return whichImage;
    }

    public void setWhichImage(String whichImage) {
        this.whichImage = whichImage;
    }

    public int getPrimitiveImagesPathSize(){
        return getPrimitiveImagePathList().size();
    }

    public int getImagePaintsListSize(){
        return getImagePaintsList().size();
    }

    public List<String> getImageContrastRadioList() {
        return imageContrastRadioList;
    }

    public void setImageContrastRadioList(List<String> imageContrastRadioList) {
        this.imageContrastRadioList = imageContrastRadioList;
    }

    public List<String> getPrimitiveImagePathList() {
        return primitiveImagePathList;
    }

    public void setPrimitiveImagePathList(List<String> primitiveImagePathList) {
        this.primitiveImagePathList = primitiveImagePathList;
    }

    public List<Integer> getImageWordSizeList() {
        return imageWordSizeList;
    }

    public void setImageWordSizeList(List<Integer> imageWordSizeList) {
        this.imageWordSizeList = imageWordSizeList;
    }

    public List<List<ImagePaint>> getImagePaintsList() {
        return imagePaintsList;
    }

    public void setImagePaintsList(List<List<ImagePaint>> imagePaintsList) {
        this.imagePaintsList = imagePaintsList;
    }

    /**
     * 记录一次涂抹
     * 包括：涂抹类型、画笔宽度、涂抹点
     */
    public static class ImagePaint {

        //涂抹类型（错解，正解，考点，错误原因）
        private String whichPaint;

        //画笔宽度
        private Integer widthPaint;

        //涂抹多个点point
        private List<Point> pointsPaint = new ArrayList<>();

        public String getWhichPaint() {
            return whichPaint;
        }

        public void setWhichPaint(String whichPaint) {
            this.whichPaint = whichPaint;
        }

        public Integer getWidthPaint() {
            return widthPaint;
        }

        public void setWidthPaint(Integer widthPaint) {
            this.widthPaint = widthPaint;
        }

        public List<Point> getPointsPaint() {
            return pointsPaint;
        }

        public void setPointsPaint(List<Point> pointsPaint) {
            this.pointsPaint = pointsPaint;
        }

        @Override
        public String toString() {
            return "ImagePaint{" +
                    "whichPaint='" + whichPaint + '\'' +
                    ", widthPaint=" + widthPaint +
                    ", pointsPaint=" + pointsPaint +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "TopicImagesPaint{" +
                "whichImage='" + whichImage + '\'' +
                ", imageWordSizeList=" + imageWordSizeList +
                ", imageContrastRadioList=" + imageContrastRadioList +
                ", primitiveImagePathList=" + primitiveImagePathList +
                ", imagePaintsList=" + imagePaintsList +
                '}';
    }
}
