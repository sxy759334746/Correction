package com.luckyxmobile.correction.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ChangHao
 * @date 2019年8月8日
 * 用于opencv图像处理工具类
 */
public class ImageUtil {
    /**
     * 转换图片为灰色
     *
     * @param bitmap
     * @return
     */
    public static Bitmap convertGray(Bitmap bitmap) {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, bitmap);
        return bitmap;
    }

    /**
     * @param imgPath 文件的绝对路径
     * @return Bitmap
     * @throws FileNotFoundException error
     */
    public static Bitmap file2Bitmap(String imgPath) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(imgPath);
        return BitmapFactory.decodeStream(fis);
    }

    /**
     * 将带有文字的图片二值化 对没有阴影的图片处理较好
     *
     * @param bitmap
     * @return
     */
    public static Bitmap convertThreshodOTSU(Bitmap bitmap) {
        Mat src = new Mat();
        Mat tmp = new Mat();
        Mat des = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.threshold(tmp, des, 220, 255, Imgproc.THRESH_OTSU);
        Utils.matToBitmap(des, bitmap);

        src.release();
        tmp.release();
        des.release();
        return bitmap;
    }

    /**
     * 将带有文字的图片二值化, 带有一定的阴影处理能力
     *
     * @param bitmap
     * @return
     */
    public static Bitmap convertThreshod(Bitmap bitmap) {
        Mat src = new Mat();
        Mat tmp = new Mat();
        Mat des = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGRA2GRAY);
        //直接转换的话 遇到阴影是黑的一片, 需要此函数处理
        Imgproc.adaptiveThreshold(tmp, des, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, 10);
        Utils.matToBitmap(des, bitmap);

        src.release();
        tmp.release();
        des.release();
        return bitmap;
    }

    /**
     * 将带有文字的图片二值化, 带有一定的阴影处理能力 并且中间加了一层模糊处理
     *
     * @param bitmap
     * @return
     */
    public static Bitmap convertBlurThreshod(Bitmap bitmap) {
//         创建一张新的bitmap
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat gray = new Mat();
        Mat GaussianBlur = new Mat();
        Mat out = new Mat();
        Utils.bitmapToMat(bitmap, origin);
        Imgproc.cvtColor(origin, gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(gray, GaussianBlur, new Size(5, 5), 0);
        // 二值化处理
        Imgproc.adaptiveThreshold(GaussianBlur, out, 255.0D, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 7.0D);
        Utils.matToBitmap(out, result);
        origin.release();
        gray.release();
        out.release();
        return result;
    }


    public static Bitmap setImageContrastRadioByPath(String contrastRadio,String imagePath){

        Bitmap bitmap = PhotoUtil.getBitmapByImagePath(imagePath);

        switch(contrastRadio){
            case ConstantsUtil.CONTRAST_RADIO_WEAK:
                return  ImageUtil.convertBlurThreshod(bitmap);

            case ConstantsUtil.CONTRAST_RADIO_COMMON:
                return ImageUtil.convertThreshod(bitmap);

            case ConstantsUtil.CONTRAST_RADIO_STRONG:
                return ImageUtil.convertThreshodOTSU(bitmap);

            default:
                break;
        }

        return bitmap;
    }

    /**
     * 计算传入图片内文字的大小
     * @param bitmap 题目图标
     * @return 文字大小
     */
    public static int calculateImageWordSize(Bitmap bitmap){
        int imageWordSize = 40;
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2),new Point(-1,-1));
        Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6,6),new Point(-1,-1));
        Imgproc.dilate(src, src, element1, new Point(-1, -1), 1);
        Imgproc.erode(src, src, element2, new Point(-1, -1), 1);
        Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(src,src,new Size(3,3),0);
//        Utils.matToBitmap(src,mBgBitmap);

        List<Integer> wordHeights = new ArrayList<>();
        List<MatOfPoint> contours=new ArrayList<>();

        Imgproc.findContours(src,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

//        Imgproc.drawContours(src, contours, -1,new Scalar(255, 0, 255));

        //2.筛选那些面积小的
        for (int i = 0; i < contours.size(); i++){

            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());

            //轮廓近似，作用较小，approxPolyDP函数有待研究
            double epsilon = 0.001*Imgproc.arcLength(matOfPoint2f, true);
            Imgproc.approxPolyDP(matOfPoint2f,new MatOfPoint2f(), epsilon, true);
            RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);

            int width = rect.boundingRect().width;
            int height = rect.boundingRect().height;

            if ( (250>height && height > 25) && (250 > width && width > 20) ){
                wordHeights.add(height);
            }
        }

        int max = 0;int Height = 40;
        for (Integer height1:wordHeights){
            int num = 0;
            for (Integer height2:wordHeights){
                if (Math.abs(height1-height2) < 5){
                    num++;
                }
            }
            if (num > max){
                max = num;
                Height = height1;
                Log.d("ImageUtil",max+" * 轮廓-->"+Height);
            }
        }

        imageWordSize = (int) (Height * 1.1);

        imageWordSize = imageWordSize<20?20:imageWordSize;

        Log.i("ImageUtil","轮廓 众数 "+Height);

        return imageWordSize;

    }

}
