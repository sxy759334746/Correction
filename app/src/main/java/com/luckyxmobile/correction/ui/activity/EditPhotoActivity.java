package com.luckyxmobile.correction.ui.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.gcssloop.widget.PagerGridLayoutManager;
import com.gcssloop.widget.PagerGridSnapHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.SelectBookAdapter;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.bean.TopicImagesPaint;
import com.luckyxmobile.correction.dao.impl.CorrectionLab;
import com.luckyxmobile.correction.ui.view.DrawingView;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.DestroyActivityUtil;
import com.luckyxmobile.correction.util.FastJsonUtil;
import com.luckyxmobile.correction.util.ImageUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.luckyxmobile.correction.util.SDCardUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

/**
 * 编辑图片页面
 * @author qjj、
 * @date 2019/08/03
 */
public class EditPhotoActivity extends AppCompatActivity implements View.OnClickListener, PagerGridLayoutManager.PageListener {

    public static final String TAG = "EditPhotoActivity";
    /**错题对象*/
    private Topic topic;
    /**临时变量,用于旋转屏幕恢复数据*/
    private static String whichContrastRadioS;
    private static int INITIAL_PAINT_WIDTH_S;
    private static List<TopicImagesPaint.ImagePaint> imagePaintListS = new ArrayList<>();
    private static List<TopicImagesPaint.ImagePaint> redoImagePaintListS = new ArrayList<>();
    /**用于判断是否旋转*/
    public static boolean isSensor = false;
    /**错题图片（用于记录操作）*/
    private TopicImagesPaint topicImagesPaint;
    /**判断是否进行触摸涂抹*/
    public static boolean ISTOUCH = false;
    /**自定义涂抹布局*/
    private DrawingView drawingView;
    /**返回按钮*/
    private TextView returnBtn;
    /**undo/redo按钮*/
    @SuppressLint("StaticFieldLeak")
    public static TextView undoBtn, redoBtn;
    /**画板功能按钮>涂改液、橡皮擦、对比度*/
    private TextView whiteOutBtn, eraseBtn, contrastRadioBtn;
    /**选择画笔按钮>正解、错解、考点、错误原因笔刷*/
    private Button paintRight,paintError,paintPoint,paintErrorReason;
    private int positionImage;
    /**图片路径地址*/
    private String imagePath;
    private TextView whichToolLayoutText;
    /**画笔宽度布局*/
    private HorizontalScrollView paintWidthLayout;
    private final String PAINT_WIDTH_LAYOUT = "PAINT_WIDTH_LAYOUT";
    private Button widthThinBtn, widthMediumBtn, widthThickBtn, initialWidthBtn;
    private int nowPaintWidth = ConstantsUtil.PAINT_THIN;
    private int INITIAL_PAINT_WIDTH = ConstantsUtil.PAINT_THIN;
    /**橡皮擦布局*/
    private HorizontalScrollView eraseLayout;
    private final String ERASE_WIDTH_LAYOUT = "ERASE_WIDTH_LAYOUT";
    private Button eraseThinBtn, eraseMediumBtn, eraseThickBtn;
    private int nowEraseWidth = ConstantsUtil.ERASE_MEDIUM;
    /**对比度布局*/
    private HorizontalScrollView contrastRatioLayout;
    private final String CONTRAST_RATION_LAYOUT = "CONTRAST_RADIO_LAYOUT";
    private String whichContrastRadio = ConstantsUtil.CONTRAST_RADIO_COMMON;
    private Button contrastWeakBtn, contrastCommonBtn, contrastStrongBtn;
    /**选择错题本*/
    private List<Book> bookList;
    private SelectBookAdapter adapter;
    private BottomSheetDialog selectBookDialog = null;
    private String whichActivity;
    /**判断是否是新创建的涂抹*/
    private boolean isNewTopicPaints = false;
    private CheckBox srceenRotationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 将activity设置为全屏显示（必须放在setContentView()前）
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_edit_photo);

        //初始化布局
        initView();

        //初始化页面数据
        initViewDate();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!isSensor){
            isSensor = true;
            whichContrastRadioS = whichContrastRadio;
            INITIAL_PAINT_WIDTH_S = INITIAL_PAINT_WIDTH;
            imagePaintListS = drawingView.getImagePaintList();
            redoImagePaintListS = drawingView.getRedoImagePaintList();

            Log.i(TAG, "临时数据"+whichContrastRadioS+";"+INITIAL_PAINT_WIDTH_S+";"+imagePaintListS.size()+";"+redoImagePaintListS.size());
        }

    }

    /**
     * 初始化布局，并设置点击事件
     * 初始化自定义view，并传入参数
     */
    private void initView() {
        drawingView = findViewById(R.id.doodle_view_photo);
        returnBtn = findViewById(R.id.edit_photo_return_btn);
        ImageButton finishBtn = findViewById(R.id.doodle_btn_next);
        undoBtn = findViewById(R.id.edit_photo_undo_btn);
        redoBtn = findViewById(R.id.edit_photo_redo_btn);
        eraseBtn = findViewById(R.id.edit_photo_erase);
        contrastRadioBtn = findViewById(R.id.edit_photo_contrast_ratio_btn);
        whiteOutBtn = findViewById(R.id.edit_photo_white_out);
        paintRight = findViewById(R.id.doodle_paint_right);
        paintError = findViewById(R.id.doodle_paint_error);
        paintPoint = findViewById(R.id.doodle_paint_point);
        paintErrorReason = findViewById(R.id.doodle_paint_error_reason);
        paintWidthLayout = findViewById(R.id.paint_width_layout);
        initialWidthBtn = findViewById(R.id.doodle_paint_initial_width);
        widthThinBtn = findViewById(R.id.doodle_paint_width_thin);
        widthMediumBtn = findViewById(R.id.doodle_paint_width_medium);
        widthThickBtn = findViewById(R.id.doodle_paint_width_thick);
        contrastRatioLayout = findViewById(R.id.contrast_ratio_layout);
        contrastWeakBtn = findViewById(R.id.contrast_weak_ratio_btn);
        contrastCommonBtn = findViewById(R.id.contrast_common_ratio_btn);
        contrastStrongBtn = findViewById(R.id.contrast_strong_ratio_btn);
        eraseLayout = findViewById(R.id.erase_layout);
        eraseThinBtn = findViewById(R.id.doodle_erase_width_thin);
        eraseMediumBtn = findViewById(R.id.doodle_erase_width_medium);
        eraseThickBtn = findViewById(R.id.doodle_erase_width_thick);
        whichToolLayoutText = findViewById(R.id.which_tools_text);
        srceenRotationBtn = findViewById(R.id.screen_rotation);

        undoBtn.setOnClickListener(this);
        redoBtn.setOnClickListener(this);
        eraseBtn.setOnClickListener(this);
        contrastRadioBtn.setOnClickListener(this);
        whiteOutBtn.setOnClickListener(this);
        paintRight.setOnClickListener(this);
        paintError.setOnClickListener(this);
        paintPoint.setOnClickListener(this);
        paintErrorReason.setOnClickListener(this);
        returnBtn.setOnClickListener(this);
        finishBtn.setOnClickListener(this);
        widthThinBtn.setOnClickListener(this);
        widthMediumBtn.setOnClickListener(this);
        widthThickBtn.setOnClickListener(this);
        initialWidthBtn.setOnClickListener(this);
        contrastWeakBtn.setOnClickListener(this);
        contrastCommonBtn.setOnClickListener(this);
        contrastStrongBtn.setOnClickListener(this);
        eraseThinBtn.setOnClickListener(this);
        eraseMediumBtn.setOnClickListener(this);
        eraseThickBtn.setOnClickListener(this);
        srceenRotationBtn.setOnClickListener(this);
    }

    /**
     * 获取错题topic对象
     * 获取题干图片(待编辑图片)路径
     * 根据路径转换成bitmap格式
     */
    private void initViewDate() {

        DestroyActivityUtil.addDestroyActivityToMap(EditPhotoActivity.this,TAG);
        positionImage = getIntent().getIntExtra(ConstantsUtil.IMAGE_POSITION,0);
        whichActivity = getIntent().getStringExtra(ConstantsUtil.WHICH_ACTIVITY);
        topic = LitePal.find(Topic.class,getIntent().getIntExtra(ConstantsUtil.TOPIC_ID,0));

        topicImagesPaint = FastJsonUtil.jsonToObject(topic.getTopic_original_picture(), TopicImagesPaint.class);
        assert topicImagesPaint != null;
        //获取指定图片路径
        imagePath = topicImagesPaint.getPrimitiveImagePathList().get(positionImage);
        Log.d(TAG, "primitive--imagePath: "+imagePath);

        if (!whichActivity.equals(MainActivity.TAG)){
            returnBtn.setText(getString(R.string.exit));
        }

        //判断是否是新的涂抹
        if (topic.getBook_id() == 0 || topicImagesPaint.getPrimitiveImagesPathSize() > topicImagesPaint.getImagePaintsList().size()){
            isNewTopicPaints = true;
        }else{
            isNewTopicPaints = false;
        }

        //判断是否旋转
        if (isSensor){
            Log.i(TAG, "旋转:true");
            isSensor = false;
            INITIAL_PAINT_WIDTH = INITIAL_PAINT_WIDTH_S;
            whichContrastRadio = whichContrastRadioS;
            drawingView.setImagePaintList(imagePaintListS);
            drawingView.setRedoImagePaintList(redoImagePaintListS);
        }else{
            Log.i(TAG, "旋转:false");
            if (isNewTopicPaints){
                drawingView.setImageBitmap(whichContrastRadio,imagePath);
                INITIAL_PAINT_WIDTH = ImageUtil.calculateImageWordSize(drawingView.getImageBitmap());
                drawingView.setImagePaintList(new ArrayList<>());
            }else {
                INITIAL_PAINT_WIDTH = topicImagesPaint.getImageWordSizeList().get(positionImage);
                whichContrastRadio = topicImagesPaint.getImageContrastRadioList().get(positionImage);
                drawingView.setImagePaintList(topicImagesPaint.getImagePaintsList().get(positionImage));
            }
        }

        setWhichPaint(ConstantsUtil.PAINT_RIGHT,true);
        setContrastRadio(whichContrastRadio,true);
        setPrintWidth(INITIAL_PAINT_WIDTH,true);

        nowPaintWidth = INITIAL_PAINT_WIDTH;
        initialWidthBtn.setText(String.valueOf(INITIAL_PAINT_WIDTH));

        int widthInitialBtn = (INITIAL_PAINT_WIDTH + 80)/4;
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) initialWidthBtn.getLayoutParams();
        linearParams.width = PhotoUtil.dip2px(this,(float)widthInitialBtn);
        linearParams.height = PhotoUtil.dip2px(this,(float)widthInitialBtn);
        initialWidthBtn.setLayoutParams(linearParams); //使设置好的布局参数应用到控件

    }

    @Override
    protected void onResume() {
        super.onResume();

        //加载所有错题本（删去收藏）
        bookList = LitePal.findAll(Book.class);
        bookList.remove(0);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ISTOUCH = false;
        isSensor = false;
        whichContrastRadioS = "";
        INITIAL_PAINT_WIDTH_S = 0;
        imagePaintListS = null;
        redoImagePaintListS = null;

        //此活动返回时 将错题是否是从收藏夹添加的 标记为false
        SharedPreferences sp = getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION, MODE_PRIVATE);
        Editor editor =sp.edit();
        editor.putBoolean(ConstantsUtil.IF_FROM_FAVORITE, false);
        editor.apply();

        switch (whichActivity){
            case MainActivity.TAG:
                SDCardUtil.cascadeDeleteTopic(topic.getId(),this);
                CorrectionLab.deleteTopic(topic.getId());
                DestroyActivityUtil.destroyActivity(TAG);
                break;

            case TopicActivity.TAG:
                SDCardUtil.deleteFile(imagePath,this);
                topicImagesPaint.getPrimitiveImagePathList().remove(positionImage);
                topic.setTopic_original_picture(FastJsonUtil.objectToJson(topicImagesPaint));
                topic.save();
                DestroyActivityUtil.destroyActivityALL();
                break;
                default:
                    break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //返回操作
            case R.id.edit_photo_return_btn:
                onBackPressed();
                break;
            //撤销操作
            case R.id.edit_photo_undo_btn:
                drawingView.undo();
                break;
            //恢复操作
            case R.id.edit_photo_redo_btn:
                drawingView.redo();
                break;
            //对比度
            case R.id.edit_photo_contrast_ratio_btn:
                changeLayout(CONTRAST_RATION_LAYOUT,true);
                break;
            //切换 橡皮擦
            case R.id.edit_photo_erase:
                changeLayout(ERASE_WIDTH_LAYOUT,true);
                break;
            //切换 涂改液
            case R.id.edit_photo_white_out:
                setWhichPaint(ConstantsUtil.PAINT_WHITE_OUT,true);
                break;
            //切换 正解 笔刷
            case R.id.doodle_paint_right:
                setWhichPaint(ConstantsUtil.PAINT_RIGHT,true);
                break;
            //切换 错解 笔刷
            case R.id.doodle_paint_error:
                setWhichPaint(ConstantsUtil.PAINT_ERROR,true);
                break;
            //切换 考点 笔刷
            case R.id.doodle_paint_point:
                setWhichPaint(ConstantsUtil.PAINT_POINT,true);
                break;
            //切换 错误原因 笔刷
            case R.id.doodle_paint_error_reason:
                setWhichPaint(ConstantsUtil.PAINT_REASON,true);
                break;
            //涂抹完成-下一步
            case R.id.doodle_btn_next:
                if (whichActivity.equals(MainActivity.TAG)){
                    //如果是新题，选择错题本
                    selectBook();
                }else{
                    finishNext();
                    //销毁活动
                    DestroyActivityUtil.destroyActivityALL();
                }

                break;
            //画笔宽度 细
            case R.id.doodle_paint_width_thin:
                setPrintWidth(ConstantsUtil.PAINT_THIN,true);
                break;
            //画笔宽度 中
            case R.id.doodle_paint_width_medium:
                setPrintWidth(ConstantsUtil.PAINT_MEDIUM,true);
                break;
            //画笔宽度 粗
            case R.id.doodle_paint_width_thick:
                setPrintWidth(ConstantsUtil.PAINT_THICK,true);
                break;
            //画笔宽度 初始设置
            case R.id.doodle_paint_initial_width:
                setPrintWidth(INITIAL_PAINT_WIDTH,true);
                break;
            //橡皮擦宽度 细
            case R.id.doodle_erase_width_thin:
                setEraseWidth(ConstantsUtil.ERASE_THIN,true);
                break;
            //橡皮擦宽度 中
            case R.id.doodle_erase_width_medium:
                setEraseWidth(ConstantsUtil.ERASE_MEDIUM,true);
                break;
            //橡皮擦宽度 粗
            case R.id.doodle_erase_width_thick:
                setEraseWidth(ConstantsUtil.ERASE_THICK,true);
                break;
            //对比度 弱
            case R.id.contrast_weak_ratio_btn:
                setContrastRadio(ConstantsUtil.CONTRAST_RADIO_WEAK,true);
                break;
            //对比度 一般
            case R.id.contrast_common_ratio_btn:
                setContrastRadio(ConstantsUtil.CONTRAST_RADIO_COMMON,true);
                break;
            //对比度 强
            case R.id.contrast_strong_ratio_btn:
                setContrastRadio(ConstantsUtil.CONTRAST_RADIO_STRONG,true);
                break;
            case R.id.screen_rotation:
                if (srceenRotationBtn.isChecked()){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    srceenRotationBtn.setBackground(getResources().getDrawable(R.drawable.ic_screen_rotation_24dp,null));
                }else{
                    srceenRotationBtn.setBackground(getResources().getDrawable(R.drawable.ic_screen_lock_rotation_24dp,null));
                    switch (this.getResources().getConfiguration().orientation){
                        case Configuration.ORIENTATION_PORTRAIT:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            break;

                        case Configuration.ORIENTATION_LANDSCAPE:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            break;
                            default:
                                break;
                    }
                }
                break;

                default:
                    break;
        }
    }

    /**
     * 处理按钮对应的布局
     * @param whichLayout 布局参数
     * @param isVisible 是否显示可见
     */
    private void changeLayout(String whichLayout,boolean isVisible){

        switch (whichLayout){
            case ERASE_WIDTH_LAYOUT:
                if (isVisible){
                    whichToolLayoutText.setText(getString(R.string.width));
                    Log.v(TAG,"setWhichPaint-->"+ConstantsUtil.PAINT_ERASE);
                    drawingView.setNowPaintWidth(nowEraseWidth);
                    drawingView.setNowWhichPaint(ConstantsUtil.PAINT_ERASE);
                    eraseLayout.setVisibility(View.VISIBLE);
                    eraseBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_erase_checked,0,0);
                    eraseBtn.setTextColor(getColor(R.color.orange_f7));

                    changeLayout(PAINT_WIDTH_LAYOUT,false);
                    changeLayout(CONTRAST_RATION_LAYOUT,false);

                    setWhichPaint(ConstantsUtil.PAINT_RIGHT,false);
                    setWhichPaint(ConstantsUtil.PAINT_ERROR,false);
                    setWhichPaint(ConstantsUtil.PAINT_POINT,false);
                    setWhichPaint(ConstantsUtil.PAINT_REASON,false);
                    setWhichPaint(ConstantsUtil.PAINT_WHITE_OUT,false);

                }else {
                    eraseLayout.setVisibility(View.GONE);
                    eraseBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_erase_unchecked,0,0);
                    eraseBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;

            case PAINT_WIDTH_LAYOUT:
                if (isVisible){
                    whichToolLayoutText.setText(getString(R.string.width));
                    paintWidthLayout.setVisibility(View.VISIBLE);
                    changeLayout(ERASE_WIDTH_LAYOUT,false);
                    changeLayout(CONTRAST_RATION_LAYOUT,false);
                }else{
                    paintWidthLayout.setVisibility(View.GONE);
                }
                break;

            case CONTRAST_RATION_LAYOUT:
                if (isVisible){
                    whichToolLayoutText.setText(getString(R.string.contrast_ratio));
                    contrastRatioLayout.setVisibility(View.VISIBLE);
                    contrastRadioBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_ratio_checked,0,0);
                    contrastRadioBtn.setTextColor(getColor(R.color.orange_f7));

                    changeLayout(ERASE_WIDTH_LAYOUT,false);
                    changeLayout(PAINT_WIDTH_LAYOUT,false);

                    setWhichPaint(ConstantsUtil.PAINT_RIGHT,false);
                    setWhichPaint(ConstantsUtil.PAINT_ERROR,false);
                    setWhichPaint(ConstantsUtil.PAINT_POINT,false);
                    setWhichPaint(ConstantsUtil.PAINT_REASON,false);
                    setWhichPaint(ConstantsUtil.PAINT_WHITE_OUT,false);
                }else{
                    contrastRatioLayout.setVisibility(View.GONE);
                    contrastRadioBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_ratio_unchecked,0,0);
                    contrastRadioBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;

            default:
                break;
        }
    }

    /**
     * 处理画笔按钮
     * @param whichPaint 画笔参数
     * @param isSelect 是否选中
     */
    private void setWhichPaint(String whichPaint, boolean isSelect){

        if (isSelect){
            drawingView.setNowPaintWidth(nowPaintWidth);
            drawingView.setNowWhichPaint(whichPaint);
            changeLayout(PAINT_WIDTH_LAYOUT,true);
            Log.v(TAG,"setWhichPaint-->"+whichPaint);
        }

        switch (whichPaint) {
            case ConstantsUtil.PAINT_RIGHT:
                if (isSelect){
                    setWhichPaint(ConstantsUtil.PAINT_ERROR,false);
                    setWhichPaint(ConstantsUtil.PAINT_POINT,false);
                    setWhichPaint(ConstantsUtil.PAINT_REASON,false);
                    setWhichPaint(ConstantsUtil.PAINT_WHITE_OUT,false);
                    paintRight.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_right,0,0);
                    paintRight.setTextColor(getColor(R.color.blue_right));
                }else{
                    paintRight.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_unchecked,0,0);
                    paintRight.setTextColor(getColor(R.color.gray_9c));
                }
                break;
            case ConstantsUtil.PAINT_ERROR:
                if (isSelect){
                    setWhichPaint(ConstantsUtil.PAINT_RIGHT,false);
                    setWhichPaint(ConstantsUtil.PAINT_POINT,false);
                    setWhichPaint(ConstantsUtil.PAINT_REASON,false);
                    setWhichPaint(ConstantsUtil.PAINT_WHITE_OUT,false);
                    paintError.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_error,0,0);
                    paintError.setTextColor(getColor(R.color.red_error));
                }else{
                    paintError.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_unchecked,0,0);
                    paintError.setTextColor(getColor(R.color.gray_9c));
                }
                break;
            case ConstantsUtil.PAINT_POINT:
                if (isSelect){
                    setWhichPaint(ConstantsUtil.PAINT_RIGHT,false);
                    setWhichPaint(ConstantsUtil.PAINT_ERROR,false);
                    setWhichPaint(ConstantsUtil.PAINT_REASON,false);
                    setWhichPaint(ConstantsUtil.PAINT_WHITE_OUT,false);
                    paintPoint.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_point,0,0);
                    paintPoint.setTextColor(getColor(R.color.green_point));
                }else{
                    paintPoint.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_unchecked,0,0);
                    paintPoint.setTextColor(getColor(R.color.gray_9c));
                }
                break;
            case ConstantsUtil.PAINT_REASON:
                if (isSelect){
                    setWhichPaint(ConstantsUtil.PAINT_RIGHT,false);
                    setWhichPaint(ConstantsUtil.PAINT_ERROR,false);
                    setWhichPaint(ConstantsUtil.PAINT_POINT,false);
                    setWhichPaint(ConstantsUtil.PAINT_WHITE_OUT,false);
                    paintErrorReason.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_error_reason,0,0);
                    paintErrorReason.setTextColor(getColor(R.color.yellow_reason));
                }else{
                    paintErrorReason.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_unchecked,0,0);
                    paintErrorReason.setTextColor(getColor(R.color.gray_9c));
                }
                break;
            case ConstantsUtil.PAINT_WHITE_OUT:
                if (isSelect){
                    setWhichPaint(ConstantsUtil.PAINT_RIGHT,false);
                    setWhichPaint(ConstantsUtil.PAINT_ERROR,false);
                    setWhichPaint(ConstantsUtil.PAINT_POINT,false);
                    setWhichPaint(ConstantsUtil.PAINT_REASON,false);
                    whiteOutBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_white_out_check,0,0);
                    whiteOutBtn.setTextColor(getColor(R.color.orange_f7));
                }else{
                    whiteOutBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_white_out_uncheck,0,0);
                    whiteOutBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;

        }
    }

    /**
     * 处理画笔宽度
     * @param whichPrintWidth 画笔宽度参数
     * @param isSelect 是否选中
     */
    private void setPrintWidth(int whichPrintWidth, boolean isSelect){

        if (isSelect){
            nowPaintWidth = whichPrintWidth;
            drawingView.setNowPaintWidth(nowPaintWidth);
            Log.v(TAG,"setPaintWidth-->"+whichPrintWidth);
        }

        switch (whichPrintWidth){
            case ConstantsUtil.PAINT_THIN:
                if (isSelect){
                    widthThinBtn.setBackground(getDrawable(R.drawable.ic_paint_width_check_24dp));
                    widthThinBtn.setTextColor(getColor(R.color.orange_f7));
                    setPrintWidth(ConstantsUtil.PAINT_MEDIUM,false);
                    setPrintWidth(ConstantsUtil.PAINT_THICK,false);
                    setPrintWidth(INITIAL_PAINT_WIDTH,false);
                }else{
                    widthThinBtn.setBackground(getDrawable(R.drawable.ic_paint_width_uncheck_24dp));
                    widthThinBtn.setTextColor(getColor(R.color.gray_9c));
                }

                break;
            case ConstantsUtil.PAINT_MEDIUM:
                if (isSelect){
                    widthMediumBtn.setBackground(getDrawable(R.drawable.ic_paint_width_check_24dp));
                    widthMediumBtn.setTextColor(getColor(R.color.orange_f7));
                    setPrintWidth(ConstantsUtil.PAINT_THIN,false);
                    setPrintWidth(ConstantsUtil.PAINT_THICK,false);
                    setPrintWidth(INITIAL_PAINT_WIDTH,false);
                }else{
                    widthMediumBtn.setBackground(getDrawable(R.drawable.ic_paint_width_uncheck_24dp));
                    widthMediumBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;
            case ConstantsUtil.PAINT_THICK:
                if (isSelect){
                    widthThickBtn.setBackground(getDrawable(R.drawable.ic_paint_width_check_24dp));
                    widthThickBtn.setTextColor(getColor(R.color.orange_f7));
                    setPrintWidth(ConstantsUtil.PAINT_THIN,false);
                    setPrintWidth(ConstantsUtil.PAINT_MEDIUM,false);
                    setPrintWidth(INITIAL_PAINT_WIDTH,false);
                }else{
                    widthThickBtn.setBackground(getDrawable(R.drawable.ic_paint_width_uncheck_24dp));
                    widthThickBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;

                default:
                    if (isSelect){
                        initialWidthBtn.setBackground(getDrawable(R.drawable.ic_paint_width_check_24dp));
                        initialWidthBtn.setTextColor(getColor(R.color.red_ff));
                        setPrintWidth(ConstantsUtil.PAINT_THIN,false);
                        setPrintWidth(ConstantsUtil.PAINT_MEDIUM,false);
                        setPrintWidth(ConstantsUtil.PAINT_THICK,false);
                    }else{
                        initialWidthBtn.setBackground(getDrawable(R.drawable.ic_paint_width_uncheck_24dp));
                        initialWidthBtn.setTextColor(getColor(R.color.gray_9c));
                    }
                    break;
        }
    }

    /**
     * 处理橡皮擦宽度
     * @param whichEraseWidth 橡皮擦宽度参数
     * @param isSelect 是否选中
     */
    private void setEraseWidth(int whichEraseWidth, boolean isSelect){

        if (isSelect){
            nowEraseWidth =  whichEraseWidth;
            drawingView.setNowPaintWidth(nowEraseWidth);
            Log.v(TAG,"setEraseWidth-->"+whichEraseWidth);
        }

        switch (whichEraseWidth){
            case ConstantsUtil.ERASE_THIN:
                if (isSelect){
                    eraseThinBtn.setBackground(getDrawable(R.drawable.ic_paint_width_check_24dp));
                    setEraseWidth(ConstantsUtil.ERASE_MEDIUM,false);
                    setEraseWidth(ConstantsUtil.ERASE_THICK,false);
                }else{
                    eraseThinBtn.setBackground(getDrawable(R.drawable.ic_paint_width_uncheck_24dp));
                }

                break;
            case ConstantsUtil.ERASE_MEDIUM:
                if (isSelect){
                    eraseMediumBtn.setBackground(getDrawable(R.drawable.ic_paint_width_check_24dp));
                    setEraseWidth(ConstantsUtil.ERASE_THIN,false);
                    setEraseWidth(ConstantsUtil.ERASE_THICK,false);
                }else{
                    eraseMediumBtn.setBackground(getDrawable(R.drawable.ic_paint_width_uncheck_24dp));
                }
                break;
            case ConstantsUtil.ERASE_THICK:
                if (isSelect){
                    eraseThickBtn.setBackground(getDrawable(R.drawable.ic_paint_width_check_24dp));
                    setEraseWidth(ConstantsUtil.ERASE_THIN,false);
                    setEraseWidth(ConstantsUtil.ERASE_MEDIUM,false);
                }else{
                    eraseThickBtn.setBackground(getDrawable(R.drawable.ic_paint_width_uncheck_24dp));
                }
                break;
            default:
                break;
        }
    }

    /**
     * 处理对比度
     * @param whichContrastRadio 对比度参数
     * @param isSelect 是否选中
     */
    private void setContrastRadio(String whichContrastRadio, boolean isSelect){

        if (isSelect){
            this.whichContrastRadio = whichContrastRadio;
            drawingView.setImageBitmap(whichContrastRadio, imagePath);
            Log.v(TAG,"setContrastRadio-->"+whichContrastRadio);
        }

        switch (whichContrastRadio){
            case ConstantsUtil.CONTRAST_RADIO_WEAK:
                if (isSelect){
                    contrastWeakBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_weak_ratio_checked,0,0);
                    contrastWeakBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                    setContrastRadio(ConstantsUtil.CONTRAST_RADIO_COMMON,false);
                    setContrastRadio(ConstantsUtil.CONTRAST_RADIO_STRONG,false);
                }else{
                    contrastWeakBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_weak_ratio_unchecked,0,0);
                    contrastWeakBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;

            case ConstantsUtil.CONTRAST_RADIO_COMMON:
                if (isSelect){
                    contrastCommonBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_common_ratio_checked,0,0);
                    contrastCommonBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                    setContrastRadio(ConstantsUtil.CONTRAST_RADIO_WEAK,false);
                    setContrastRadio(ConstantsUtil.CONTRAST_RADIO_STRONG,false);
                }else{
                    contrastCommonBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_common_ratio_unchecked,0,0);
                    contrastCommonBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;

            case ConstantsUtil.CONTRAST_RADIO_STRONG:
                if (isSelect){
                    contrastStrongBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_strong_ratio_checked,0,0);
                    contrastStrongBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                    setContrastRadio(ConstantsUtil.CONTRAST_RADIO_WEAK,false);
                    setContrastRadio(ConstantsUtil.CONTRAST_RADIO_COMMON,false);
                }else{
                    contrastStrongBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_paint_contrast_strong_ratio_unchecked,0,0);
                    contrastStrongBtn.setTextColor(getColor(R.color.gray_9c));
                }
                break;

            default:
                break;
        }
    }


    /**
     * 选择错题本
     */
    private void selectBook() {
        selectBookDialog = new BottomSheetDialog(EditPhotoActivity.this);
        View view = LayoutInflater.from(EditPhotoActivity.this).inflate(R.layout.select_book_dialog,null);
        TextView createBook = view.findViewById(R.id.create_book);
        TextView finish = view.findViewById(R.id.select_book_finish);

        int mRows = 2;
        int mClomns = 4;

        RecyclerView recyclerView;
        PagerGridLayoutManager mLayoutManager;

        mLayoutManager = new PagerGridLayoutManager(mRows,mClomns,PagerGridLayoutManager.HORIZONTAL);

        recyclerView = view.findViewById(R.id.recycler_view);

        //设置页面变化监听器
        mLayoutManager.setPageListener(this);
        recyclerView.setLayoutManager(mLayoutManager);

        //设置滚动辅助工具
        PagerGridSnapHelper pagerGridSnapHelper = new PagerGridSnapHelper();
        pagerGridSnapHelper.attachToRecyclerView(recyclerView);

        adapter = new SelectBookAdapter(bookList);

        recyclerView.setAdapter(adapter);
        initAdapterSelectItem(adapter);

        //recyclerView
        adapter.setOnItemClickListener(new SelectBookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.selectItem = position;
                adapter.notifyDataSetChanged();
            }
        });
        //新建错题本
        createBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookDialog();
            }
        });

        //完成后 数据库添加  页面跳转
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookList.size()>0) {

                    topic.setBook_id(bookList.get(adapter.selectItem).getId());

                    //判断是否是从收藏夹中添加的错题 若是则标记为收藏
                    SharedPreferences sp = getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION, MODE_PRIVATE);
                    Boolean fromFavorite = sp.getBoolean(ConstantsUtil.IF_FROM_FAVORITE, false);
                    if(fromFavorite){
                        topic.setTopic_collection(1);
                        Editor editor = sp.edit();
                        editor.putBoolean(ConstantsUtil.IF_FROM_FAVORITE, false);
                        editor.apply();
                        //Toast.makeText(EditPhotoActivity.this, String.valueOf(topic.getTopic_collection()), Toast.LENGTH_LONG).show();
                    }

                    finishNext();

                    Intent intent = new Intent(EditPhotoActivity.this, BookDetailActivity.class);
                    intent.putExtra(ConstantsUtil.WHICH_ACTIVITY,whichActivity);
                    intent.putExtra(ConstantsUtil.BOOK_ID,topic.getBook_id());
                    startActivity(intent);

                    //销毁活动
                    DestroyActivityUtil.destroyActivityALL();
                    Toasty.success(EditPhotoActivity.this, R.string.successful, Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        selectBookDialog.setContentView(view);
        setLp(view);
        selectBookDialog.show();

    }

    private void finishNext(){
        ISTOUCH = false;
        //如果不是新创建的涂抹，需要更新最后一个（即先删除最后一个，在添加）
        if (!isNewTopicPaints){
            topicImagesPaint.getImagePaintsList().remove(positionImage);
            topicImagesPaint.getImageWordSizeList().remove(positionImage);
            topicImagesPaint.getImageContrastRadioList().remove(positionImage);
        }

        topicImagesPaint.getImageWordSizeList().add(positionImage,INITIAL_PAINT_WIDTH);
        topicImagesPaint.getImageContrastRadioList().add(positionImage,whichContrastRadio);
        topicImagesPaint.getImagePaintsList().add(positionImage,drawingView.getImagePaintList());

        String json = FastJsonUtil.objectToJson(topicImagesPaint);
        topic.setTopic_original_picture(json);
        topic.save();

        isSensor = false;
        whichContrastRadioS = "";
        INITIAL_PAINT_WIDTH_S = 0;
        imagePaintListS = null;
        redoImagePaintListS = null;
    }

    //bottomsheet 横屏下正常显示
    public void setLp(View view) {
        View parent = (View)view.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        view.measure(0,0);
        Log.d(TAG, "setLp: "+behavior.getPeekHeight()+"    "+view.getMeasuredHeight());
        behavior.setPeekHeight(view.getMeasuredHeight());
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        parent.setLayoutParams(lp);
    }

    /**
     * 初始化错题本的选中状态
     * @param adapter
     * @author zc
     */
    private void initAdapterSelectItem(SelectBookAdapter adapter) {
        SharedPreferences sp = getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION, MODE_PRIVATE);
        int book_id = sp.getInt(ConstantsUtil.TABLE_FROM_BOOK_ID,0);
        //读完之后重置bookid的数据，防止造成干扰
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(ConstantsUtil.BOOK_ID, 0);
        editor.apply();

        adapter.selectItem = adapter.transferBookIdToIndex(book_id);
        adapter.notifyDataSetChanged();
    }

    /**
     * 添加错题本
     */

    @SuppressLint("SetTextI18n")
    private void addBookDialog() {
        @SuppressLint("InflateParams")
         View view =  LayoutInflater.from(EditPhotoActivity.this).inflate(R.layout.dialog_add_book,null);
        final EditText bookNameEdt = view.findViewById(R.id.bookNameEdt);
        final TextView bookNameNum = view.findViewById(R.id.bookNameEdtNum);

        //输入框字数提示和限制
        bookNameEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                bookNameNum.setText(s.length()+"/10");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                bookNameNum.setText(s.length()+"/10");
            }
        });

        AlertDialog.Builder mChangeBookDialog = new AlertDialog.Builder(EditPhotoActivity.this);

        mChangeBookDialog.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String bookName = bookNameEdt.getText().toString();
                //保存创建
                if (bookName.length() <= 0) {

                    Toasty.warning(EditPhotoActivity.this,R.string.empty_input, Toast.LENGTH_SHORT, true).show();
                } else {
                    Book newBook = new Book();
                    newBook.setBook_name(bookNameEdt.getText().toString());
                    newBook.setBook_cover(PhotoUtil.getResultPath());
                    //插入到litepal数据库
                    newBook.save();
                    bookList.add(newBook);
                    adapter.notifyDataSetChanged();
                    Toasty.success(EditPhotoActivity.this, R.string.successful, Toast.LENGTH_SHORT, true).show();

                }
            }
        }).setNegativeButton(R.string.cancel,null);

        mChangeBookDialog.setView(view);
        mChangeBookDialog.create();
        mChangeBookDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (selectBookDialog !=null) {
            selectBookDialog.dismiss();
        }
    }

    @Override
    public void onPageSizeChanged(int pageSize) {

    }

    @Override
    public void onPageSelect(int pageIndex) {

    }
}
