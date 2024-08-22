package com.luckyxmobile.correction.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.bean.TopicImagesPaint;
import com.luckyxmobile.correction.ui.activity.TopicViewPageActivity;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.FastJsonUtil;
import com.luckyxmobile.correction.util.ImageUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.List;
import cn.forward.androids.ScaleGestureDetectorApi27;
import cn.forward.androids.TouchGestureDetector;

public class SeePaintsByClickView extends View {

    /**背景图, 前景图*/
    private Bitmap mBgBitmap, mFgBitmap;
    private float mBitmapTransX, oldX, mBitmapTransY, oldY, mBitmapScale = 1;
    private List<PaintPoints> imagePaints = new ArrayList<>();
    /**触摸手势监听*/
    private TouchGestureDetector mTouchGestureDetector;
    private List<String> whichShow;

    public SeePaintsByClickView(Context context){
        super(context);
        //初始化点击事件
        onTouchEvent();
    }

    public SeePaintsByClickView(Context context, AttributeSet attrs) {
        super(context,attrs);
        //初始化点击事件
        onTouchEvent();
    }

    public SeePaintsByClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化点击事件
        onTouchEvent();
    }

    public void setWhichShow(List<String> whichShow) {
        this.whichShow = whichShow;
        if(this.whichShow == null){
            this.whichShow = new ArrayList<>();
        }
    }

    public void setInit(int topicID, int currentImage){
        Topic topic = LitePal.find(Topic.class,topicID);
        TopicImagesPaint topicImagesPaint = FastJsonUtil.jsonToObject(topic.getTopic_original_picture(),TopicImagesPaint.class);
        if (topicImagesPaint == null){
            Log.d("SeePaintsByClick", "未找到该题目图片相关信息");
            return;
        }

        Log.d("SeePaintsByClick", "setInit: --"+currentImage);

        if (topicImagesPaint.getPrimitiveImagesPathSize() > currentImage){
            //加载bitmap
            setImageBitmap(topicImagesPaint.getImageContrastRadioList().get(currentImage)
                    ,topicImagesPaint.getPrimitiveImagePathList().get(currentImage));
        }

        if (topicImagesPaint.getImagePaintsList().size() > currentImage){
            //加载数据
            for (TopicImagesPaint.ImagePaint imagePaint:topicImagesPaint.getImagePaintsList().get(currentImage)){
                PaintPoints paintPoints = new PaintPoints();
                //包含则显示涂抹
                if (whichShow.contains(imagePaint.getWhichPaint())){
                    paintPoints.setData(imagePaint,false);
                }else{
                    paintPoints.setData(imagePaint,true);
                }
                imagePaints.add(paintPoints);
            }
        }
    }

    /**
     * 初始化图片
     * @param contrastRadio 对比度
     * @param imagePath 图片路径
     */
    private void setImageBitmap(String contrastRadio, String imagePath){
        this.mBgBitmap = ImageUtil.setImageContrastRadioByPath(contrastRadio,imagePath);
        this.mFgBitmap = Bitmap.createBitmap(mBgBitmap.getWidth(),mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mFgBitmap.eraseColor(getContext().getColor(R.color.black));
        invalidate();
    }

    /**
     * 点击操作
     */
    private void onTouchEvent() {
        // 由手势识别器处理手势
        mTouchGestureDetector = new TouchGestureDetector(getContext(),
                new TouchGestureDetector.OnTouchGestureListener() {

            // 缩放手势操作相关
            Float mLastFocusX;
            Float mLastFocusY;
            float mTouchCentreX, mTouchCentreY;

            @Override
            public boolean onSingleTapUp(MotionEvent e) { // 单击选中

                if (TopicViewPageActivity.IS_CLICK_SMEAR_BY){

                    float x = toX(e.getX()), y = toY(e.getY());
                    for (int i = 0; i < imagePaints.size(); i++) {
                        Point point = new Point((int)x,(int)y);
                        if (imagePaints.get(i).judgePointInPoints(point)){
                            if (imagePaints.get(i).isShow){
                                imagePaints.get(i).isShow = false;
                            }else{
                                imagePaints.get(i).isShow = true;
                            }
                        }
                    }
                    invalidate();
                    return true;
                }

                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetectorApi27 detector) {
                mLastFocusX = null;
                mLastFocusY = null;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetectorApi27 detector) {
            }

            @Override
            public boolean onScale(ScaleGestureDetectorApi27 detector) { // 双指缩放中

                // 屏幕上的焦点
                mTouchCentreX = detector.getFocusX();
                mTouchCentreY = detector.getFocusY();

                if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
                    float dx = mTouchCentreX - mLastFocusX;
                    float dy = mTouchCentreY - mLastFocusY;
                    // 移动图片
                    mBitmapTransX = mBitmapTransX + dx;
                    mBitmapTransY = mBitmapTransY + dy;
                }

                // 缩放图片
                mBitmapScale = mBitmapScale * detector.getScaleFactor();
                if (mBitmapScale < 0.5f) {
                    mBitmapScale = 0.5f;
                }

                if (mBitmapScale >2.5f){
                    mBitmapScale = 2.5f;
                }

                invalidate();

                mLastFocusX = mTouchCentreX;
                mLastFocusY = mTouchCentreY;

                return true;
            }
        });

        // 下面两行绘画场景下应该设置间距为大于等于1，否则设为0双指缩放后抬起其中一个手指仍然可以移动
        mTouchGestureDetector.setScaleSpanSlop(1); // 手势前识别为缩放手势的双指滑动最小距离值
        mTouchGestureDetector.setScaleMinSpan(1); // 缩放过程中识别为缩放手势的双指最小距离值
        mTouchGestureDetector.setIsLongpressEnabled(false);
        mTouchGestureDetector.setIsScrollAfterScaled(false);
    }

    private float toX(float touchX) {
        return (touchX - mBitmapTransX) / mBitmapScale;
    }

    private float toY(float touchY) {
        return (touchY - mBitmapTransY) / mBitmapScale;
    }


    @Override
    public boolean dispatchTouchEvent (MotionEvent event){
        // 由手势识别器处理手势
        boolean consumed = mTouchGestureDetector.onTouchEvent(event);
        if (!consumed) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系(toX toY)
        canvas.translate(mBitmapTransX, mBitmapTransY);
        canvas.scale(mBitmapScale, mBitmapScale);

        canvas.drawBitmap(mFgBitmap, 0, 0, null);
        canvas.drawBitmap(mBgBitmap, 0, 0, null);

        for (PaintPoints paintPoints:imagePaints){
            canvas.save();
            Path path = PhotoUtil.pointsToPath(paintPoints.imagePaint.getPointsPaint());
            String whichPaint = paintPoints.imagePaint.getWhichPaint();
            int widthPaint = paintPoints.imagePaint.getWidthPaint();
            boolean isShow = paintPoints.isShow;
            Paint paint = PhotoUtil.createPaint(getContext(),whichPaint,widthPaint,!isShow);
            canvas.drawPath(path,paint);
            canvas.restore();
        }

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) { //view绘制完成时 大小确定
        super.onSizeChanged(width, height, oldw, oldh);
        int w = mBgBitmap.getWidth();
        int h = mBgBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        float centerWidth, centerHeight;
        // 1.计算使图片居中的缩放值
        if (nw > nh) {
            mBitmapScale = 1 / nw;
            centerWidth = getWidth();
            centerHeight = (int) (h * mBitmapScale);
        } else {
            mBitmapScale = 1 / nh;
            centerWidth = (int) (w * mBitmapScale);
            centerHeight = getHeight();
        }
        // 2.计算使图片居中的偏移值
        mBitmapTransX = (getWidth() - centerWidth) / 2f;
        mBitmapTransY = (getHeight() - centerHeight) / 2f;
        oldX = mBitmapTransX;
        oldY = mBitmapTransY;
        invalidate();
    }

    static class PaintPoints{

        private boolean isShow;
        private TopicImagesPaint.ImagePaint imagePaint;

        public void setData(TopicImagesPaint.ImagePaint imagePaint,boolean isShow){
            this.isShow = isShow;//显示涂抹
            this.imagePaint = imagePaint;
        }


        private boolean judgePointInPoints(Point point){
            for (Point p:imagePaint.getPointsPaint()){
                if (Math.abs(p.x-point.x)<imagePaint.getWidthPaint()
                        && Math.abs(p.y-point.y)<imagePaint.getWidthPaint()){
                    String[] which = {ConstantsUtil.PAINT_WHITE_OUT,ConstantsUtil.PAINT_ERASE};
                    //排除橡皮擦/涂改液
                    if (!imagePaint.getWhichPaint().equals(which[0]) && !imagePaint.getWhichPaint().equals(which[1])){
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
