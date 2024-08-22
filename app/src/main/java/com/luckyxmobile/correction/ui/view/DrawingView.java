package com.luckyxmobile.correction.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.bean.TopicImagesPaint;
import com.luckyxmobile.correction.ui.activity.EditPhotoActivity;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.ImageUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cn.forward.androids.ScaleGestureDetectorApi27;
import cn.forward.androids.TouchGestureDetector;
import es.dmoral.toasty.Toasty;

/**
 * 编辑图片页面
 * @author qjj、
 * @date 2019/08/03
 */
public class DrawingView extends View {

    public final static String TAG = "DoodleView";
    private Context context = null;
    /**触摸手势监听*/
    private TouchGestureDetector mTouchGestureDetector;
    /**记录所有的涂抹*/
    private List<TopicImagesPaint.ImagePaint> imagePaintList = new ArrayList<>();
    /**（用于记录撤销的涂抹）*/
    private List<TopicImagesPaint.ImagePaint> redoImagePaintList = new ArrayList<>();
    /**当前的涂抹（涂抹类型，宽度，涂抹点）*/
    private TopicImagesPaint.ImagePaint imagePaint = new TopicImagesPaint.ImagePaint();
    /**当前涂抹类型*/
    private String nowWhichPaint = ConstantsUtil.PAINT_RIGHT;
    /**当前画笔宽度*/
    private int nowPaintWidth = 40;
    /**当前涂抹点*/
    private List<Point> nowPoints = new ArrayList<>();
    /**当前画笔*/
    private Paint nowPaint = new Paint();
    /**画布*/
    private Canvas mCanvas;
    /**背景图, 前景图*/
    private Bitmap mBgBitmap, mFgBitmap;
    /**图片的宽,高，缩放*/
    private int imageWidth, imageHeight;
    private float mBitmapTransX, mBitmapTransY, mBitmapScale = 1;

    public DrawingView(Context context){
        super(context);
        this.setInit(context);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.setInit(context);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setInit(context);
    }

    private void setInit(Context context) {
        this.context = context;
        //初始化触摸事件
        onTouchEvent();
        //初始化笔刷
        setNowPaintWidth(nowPaintWidth);
        setNowWhichPaint(nowWhichPaint);
    }

    /**
     * 设置画笔
     * @param whichPaint 画笔类型
     * @param paintWidth 画笔宽度
     * @return 返回设置号的画笔
     */
    public Paint createPaint(String whichPaint, int paintWidth){

        int color = R.color.red_error;
        int alpha = 150;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        switch (whichPaint) {
            case ConstantsUtil.PAINT_RIGHT:
                color = R.color.blue_right;
                break;
            case ConstantsUtil.PAINT_ERROR:
                color = R.color.red_error;
                break;
            case ConstantsUtil.PAINT_POINT:
                color = R.color.green_point;
                break;
            case ConstantsUtil.PAINT_REASON:
                color = R.color.yellow_reason;
                break;
            case ConstantsUtil.PAINT_WHITE_OUT:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                color = R.color.colorWhite;
                alpha = 255;
                break;
            case ConstantsUtil.PAINT_ERASE:
                paint.setXfermode(new  PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                color = R.color.black;
                alpha = 255;
                break;
            default:
                break;
        }
        paint.setStrokeWidth(paintWidth);
        paint.setColor(getResources().getColor(color,null));
        paint.setAlpha(alpha);

        return paint;
    }

    public void setImagePaintList(List<TopicImagesPaint.ImagePaint> imagePaintList){
        this.imagePaintList = imagePaintList;
    }

    public List<TopicImagesPaint.ImagePaint> getImagePaintList(){
        return imagePaintList;
    }

    public void setNowPaintWidth(int paintWidth){
        this.nowPaintWidth = paintWidth;
        nowPaint = createPaint(nowWhichPaint,nowPaintWidth);
    }

    public void setNowWhichPaint(String whichPaint){
        this.nowWhichPaint = whichPaint;
        nowPaint = createPaint(nowWhichPaint,nowPaintWidth);
    }

    public Bitmap getImageBitmap(){
        return Bitmap.createBitmap(mBgBitmap,0,0,mBgBitmap.getWidth(),mBgBitmap.getHeight());
    }

    public void setImageBitmap(String contrastRadio, String imagePath){

        this.mBgBitmap = ImageUtil.setImageContrastRadioByPath(contrastRadio,imagePath);
        this.mFgBitmap = Bitmap.createBitmap(mBgBitmap.getWidth(),mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        imageWidth = mBgBitmap.getWidth();
        imageHeight = mBgBitmap.getHeight();

        mCanvas = new Canvas(mFgBitmap);
        invalidate();
    }

    public List<TopicImagesPaint.ImagePaint> getRedoImagePaintList() {
        return redoImagePaintList;
    }

    public void setRedoImagePaintList(List<TopicImagesPaint.ImagePaint> redoImagePaintList) {
        this.redoImagePaintList = redoImagePaintList;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系(toX toY)
        canvas.translate(mBitmapTransX, mBitmapTransY);
        canvas.scale(mBitmapScale, mBitmapScale);

        canvas.drawBitmap(mBgBitmap, 0, 0, null);
        canvas.drawBitmap(mFgBitmap, 0, 0, null);

        if (nowWhichPaint.equals(ConstantsUtil.PAINT_ERASE)){
            Paint paint = nowPaint;
            paint.setAlpha(150);
           canvas.drawPath(PhotoUtil.pointsToPath(nowPoints),paint);
        }

        //每次清屏
        mCanvas.save();

        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(p);
        for (int i = 0; i < imagePaintList.size(); i++) {
            TopicImagesPaint.ImagePaint imagePaint = imagePaintList.get(i);
            Paint paint = createPaint(imagePaint.getWhichPaint(), imagePaint.getWidthPaint());
            Path path = PhotoUtil.pointsToPath(imagePaint.getPointsPaint());
            mCanvas.drawPath(path,paint);
        }

        mCanvas.restore();
        setUndoOrRedo();
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float toX(float touchX) {
        return (touchX - mBitmapTransX) / mBitmapScale;
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float toY(float touchY) {
        return (touchY - mBitmapTransY) / mBitmapScale;
    }

    /**
     * 处理涂抹，缩放
     */
    private void onTouchEvent() {
        // 由手势识别器处理手势
        mTouchGestureDetector = new TouchGestureDetector(getContext(), new TouchGestureDetector.OnTouchGestureListener() {

            // 缩放手势操作相关
            Float mLastFocusX;
            Float mLastFocusY;
            float mTouchCentreX, mTouchCentreY;

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
                if (mBitmapScale < 0.1f) {
                    mBitmapScale = 0.1f;
                }
                invalidate();

                mLastFocusX = mTouchCentreX;
                mLastFocusY = mTouchCentreY;

                return true;
            }

            @Override
            public void onScrollBegin(MotionEvent e) { // 滑动开始
                float x = toX(e.getX()), y = toY(e.getY());
                Log.d(TAG, "滑动开始-->("+x+":"+y+")");
                if (x < 0 || x > mCanvas.getWidth() || y < 0 || y > mCanvas.getHeight()){
                    return ;
                }
                nowPoints = new ArrayList<>();
                nowPoints.add(new Point((int)x,(int)y));
                imagePaint = new TopicImagesPaint.ImagePaint();
                imagePaint.setWhichPaint(nowWhichPaint);
                imagePaint.setWidthPaint(nowPaintWidth);
                imagePaint.setPointsPaint(nowPoints);
                imagePaintList.add(imagePaint);
                invalidate(); // 刷新
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { // 滑动中
                float x = toX(e2.getX()), y = toY(e2.getY());
                if (x < 0 || x > mCanvas.getWidth() || y < 0 || y > mCanvas.getHeight()){
                    return false;
                }
                Log.d(TAG, "滑动中-->("+x+":"+y+")");
                nowPoints.add(new Point((int)x,(int)y));
                invalidate(); // 刷新
                return true;
            }

            @Override
            public void onScrollEnd(MotionEvent e) { // 滑动结束
                float x = toX(e.getX()), y = toY(e.getY());
                if (x < 0 || x > mCanvas.getWidth() || y < 0 || y > mCanvas.getHeight()){
                    return;
                }
                Log.d(TAG, "滑动结束-->("+x+":"+y+")");
                nowPoints.add(new Point((int)x,(int)y));
                changePoints();//计算涂抹点是否合理
                if (nowWhichPaint.equals(ConstantsUtil.PAINT_ERASE)){
                    nowPoints = new ArrayList<>();
                }
                invalidate(); // 刷新

            }
        });

        // 下面两行绘画场景下应该设置间距为大于等于1，否则设为0双指缩放后抬起其中一个手指仍然可以移动
        mTouchGestureDetector.setScaleSpanSlop(1); // 手势前识别为缩放手势的双指滑动最小距离值
        mTouchGestureDetector.setScaleMinSpan(1); // 缩放过程中识别为缩放手势的双指最小距离值
        mTouchGestureDetector.setIsLongpressEnabled(false);
        mTouchGestureDetector.setIsScrollAfterScaled(false);

    }

    /**
     * 计算手指滑动的区域，删去过小涂抹
     */
    private void changePoints() {

        List<Float> pointX = new ArrayList<>();
        List<Float> pointY = new ArrayList<>();

        for (Point point: nowPoints){
            pointX.add((float)point.x);
            pointY.add((float)point.y);
        }

        float minX = Collections.min(pointX)- nowPaintWidth /2;
        float minY = Collections.min(pointY)- nowPaintWidth /2;
        float maxX = Collections.max(pointX)+ nowPaintWidth /2;
        float maxY = Collections.max(pointY)+ nowPaintWidth /2;

        minX = (minX < 0)?0:minX;
        minY = (minY < 0)?0:minY;
        maxX = (maxX < 0)?0:maxX;
        maxY = (maxY < 0)?0:maxY;

        minX = (minX > imageWidth)? imageWidth :minX;
        minY = (minY > imageHeight)? imageHeight :minY;
        maxX = (maxX > imageWidth)? imageWidth :maxX;
        maxY = (maxY > imageHeight)? imageHeight :maxY;

        RectF rect = new RectF(minX, minY, maxX, maxY);

        float rectWidth = Math.abs(rect.left - rect.right);
        float rectHeight = Math.abs(rect.top - rect.bottom);

        if (rectWidth * rectHeight < 2000) {
            Log.d(TAG,"涂抹区域太小，删除以上涂抹点");
            Toasty.warning(context, R.string.smear_waring, Toasty.LENGTH_SHORT, true).show();
            imagePaintList.remove(imagePaintList.size()-1);
        }else{
           redoImagePaintList.clear();
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
        invalidate();
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

    /**
     * 返回上一步
     */
    public void undo(){

        if (!imagePaintList.isEmpty()){
            int lastPosition = imagePaintList.size()-1;
            if (imagePaintList.get(lastPosition).getWhichPaint().equals(ConstantsUtil.PAINT_ERASE)){
                Toasty.normal(context,context.getString(R.string.undo)+":"+context.getString(R.string.erase)+"×1").show();
            }
            redoImagePaintList.add(imagePaintList.get(lastPosition));
            imagePaintList.remove(lastPosition);
            invalidate();


        }
    }
    /**
     * 下一步
     */
    public void redo(){

        if (!redoImagePaintList.isEmpty()){
            int lastPosition = redoImagePaintList.size()-1;
            if (redoImagePaintList.get(lastPosition).getWhichPaint().equals(ConstantsUtil.PAINT_ERASE)){
                Toasty.normal(context,context.getString(R.string.redo)+":"+context.getString(R.string.erase)+"×1").show();
            }
            imagePaintList.add(redoImagePaintList.get(lastPosition));
            redoImagePaintList.remove(lastPosition);
            invalidate();
        }
    }


    private void setUndoOrRedo(){
        if (imagePaintList.isEmpty()){
            EditPhotoActivity.undoBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_paint_undo_no,0,0);
            EditPhotoActivity.undoBtn.setTextColor(context.getColor(R.color.gray_9c));
        }else{
            EditPhotoActivity.undoBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_paint_undo_yes,0,0);
            EditPhotoActivity.undoBtn.setTextColor(context.getColor(R.color.orange_f7));
        }

        if (redoImagePaintList.isEmpty()){
            EditPhotoActivity.redoBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_paint_redo_no,0,0);
            EditPhotoActivity.redoBtn.setTextColor(context.getColor(R.color.gray_9c));
        }else{
            EditPhotoActivity.redoBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_paint_redo_yes,0,0);
            EditPhotoActivity.redoBtn.setTextColor(context.getColor(R.color.orange_f7));
        }
    }

}
