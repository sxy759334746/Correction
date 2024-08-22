package com.luckyxmobile.correction.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.like.OnLikeListener;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.TopicErrorAdapter;
import com.luckyxmobile.correction.adapter.TopicErrorCauseAdapter;
import com.luckyxmobile.correction.adapter.TopicOriginalAdapter;
import com.luckyxmobile.correction.adapter.TopicPointAdapter;
import com.luckyxmobile.correction.adapter.TopicRightAdapter;
import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.like.LikeButton;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.loader.ImageLoader;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import org.litepal.LitePal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import es.dmoral.toasty.Toasty;

/**
 * @author rfa
 * 错题详情界面
 *
 * LiuGen
 *  2019/07/24
 * 关于错题详情页面的信息筛选显示，修改按钮的优化
 *
 * @date 2019/07/30
 * 当前存在问题：
 * 1.点击查看大图
 * 2.图片显示——长图被压缩，短图被拉伸，效果不好
 * 3.从数据库读出原有文字描述回显的添加文字描述的对话框
 * 4.光标显示移动到文本末端
 * 5.活动的进入方式
 *
 * 2019/08/08
 * 当前存在的问题：
 * 如果拍摄的图片过小，在该页面显示的时候，
 * 图片会被不等比例拉伸，显示出来比较难看
 *
 * 2019/07/31
 * @author lg
 * 题目详情页面查看时默认隐藏工具栏
 *
 * 2019/08/28
 * @author qjj
 * 每个模块显示多张图片（recyclerView），规整代码（将topic进行唯一化）
 *
 * @date 2020/02/08
 * @author qjj
 * 规整
 */

public class TopicActivity extends AppCompatActivity implements View.OnClickListener{

    public static final  String TAG = "TopicActivity";
    /**toolbar，设置*/
    private Toolbar toolbar;
    /**每种图片类型的总布局（题干，正解，错解，考点，原因），每一模块：标题（工具按钮），图片，文字*/
    private LinearLayout originalLayout, rightLayout, errorLayout, pointLayout, causeLayout;
    /** 图片 recycle（题干，正解，错解，考点，原因）*/
    private RecyclerView recyclerTopicOriginal,recyclerTopicRight,recyclerTopicError,recyclerTopicPoint, recyclerTopicErrorCause;
    /**recycle的适配类*/
    private TopicOriginalAdapter topicOriginalAdapter;
    private TopicRightAdapter topicRightAdapter;
    private TopicErrorAdapter topicErrorAdapter;
    private TopicPointAdapter topicPointAdapter;
    private TopicErrorCauseAdapter topicErrorCauseAdapter;
    /** 文字 */
    private TextView originalText, errorSolutionText, rightSolutionText, pointText, causeText;
    /**工具按钮（更多：正解，错解，考点，原因）*/
    private ImageButton originalMoreBtn, rightMoreBtn, errorMoreBtn, pointMoreBtn, errorCauseMoreBtn;
    /**收藏按钮*/
    private LikeButton storeButton;
    /**显示原图按钮*/
    private CheckBox showPrimitiveImages;
    /**是否处于编辑状态*/
    public static boolean isEditing = false;
    /**只有唯一的topic，topicS是原状态*/
    private Topic topic,topicS;
    private int topicId;
    /**用于判断从哪个页面跳转的*/
    private String fromActivity;
    /**用来判断是哪个模块点击的*/
    private String whichModule;
    /**用于存储错题所对应的错题本名称，在Toolbar中显示*/
    private String toolbarName;
    /**添加文字内容文本*/
    private EditText editTextView;
    /**标签布局*/
    private TagFlowLayout tagLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        // 初始化布局
        initViewDate();

        // 初始化标题栏
        initToolbar();

        //设置收藏按钮
        setStoreButton(topic);

        initImagesRecycler();

        initTopicTextView();
    }

    /**
     * 界面布局初始化，并设置初始参数
     */
    @SuppressLint("SimpleDateFormat")
    private void initViewDate() {
        toolbar = findViewById(R.id.problem_content_toolbar);

        storeButton = findViewById(R.id.store_button);
        TextView createDateTopic = findViewById(R.id.topic_create_date);
        originalLayout = findViewById(R.id.region_original);
        originalMoreBtn = findViewById(R.id.btn_original);
        showPrimitiveImages = findViewById(R.id.show_original_images);
        recyclerTopicOriginal = findViewById(R.id.recycler_topic_original);
        originalText = findViewById(R.id.topic_original_text);

        rightLayout = findViewById(R.id.region_right);
        rightMoreBtn = findViewById(R.id.btn_right);
        recyclerTopicRight = findViewById(R.id.recycler_topic_right);
        rightSolutionText = findViewById(R.id.topic_right_solution_text);

        errorLayout = findViewById(R.id.region_error);
        errorMoreBtn = findViewById(R.id.btn_error);
        recyclerTopicError = findViewById(R.id.recycler_topic_error);
        errorSolutionText = findViewById(R.id.topic_error_solution_text);

        pointLayout = findViewById(R.id.region_point);
        pointMoreBtn = findViewById(R.id.btn_point);
        recyclerTopicPoint = findViewById(R.id.recycler_topic_point);
        pointText = findViewById(R.id.topic_knowledge_point_text);

        causeLayout = findViewById(R.id.region_cause);
        errorCauseMoreBtn = findViewById(R.id.btn_cause);
        recyclerTopicErrorCause = findViewById(R.id.recycler_topic_error_cause);
        causeText = findViewById(R.id.topic_error_cause_text);

        tagLayout = findViewById(R.id.tag_layout);
        Button setTagBtn = findViewById(R.id.set_tag);

        //按钮设置点击监听事件
        showPrimitiveImages.setOnClickListener(this);
        originalMoreBtn.setOnClickListener(this);
        rightMoreBtn.setOnClickListener(this);
        errorMoreBtn.setOnClickListener(this);
        pointMoreBtn.setOnClickListener(this);
        errorCauseMoreBtn.setOnClickListener(this);
        setTagBtn.setOnClickListener(this);

        //获取唯一的错题id
        topicId = getIntent().getIntExtra(ConstantsUtil.TOPIC_ID, 0);
        //判断从哪个活动跳转的
        fromActivity = getIntent().getStringExtra(ConstantsUtil.WHICH_ACTIVITY);
        // 获取toolbarName
        toolbarName = getIntent().getStringExtra(ConstantsUtil.TOOLBAR_NAME);
        //确认唯一topic
        topic = LitePal.find(Topic.class, topicId);
        topicS = topic;

        createDateTopic.setText( new SimpleDateFormat("yyyy-MM-dd").format(topic.getTopic_create_time()));
    }


    /**
     * @author qjj
     * 初始化标题栏Toolbar
     * */
    private void initToolbar() {

        // 用Toolbar代替ActionBar成为标题栏
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // 显示Toolbar自带的返回键
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // 显示标题
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        toolbar.setTitle(toolbarName);

        //toolbar的返回按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    /**
     * 设置收藏按钮
     * @param topic 当前topic
     */
    private void setStoreButton(final Topic topic){

        //1表示收藏，0表示取消收藏
        if (topic.getTopic_collection() == 1) {
            storeButton.setLiked(true);
        } else {
            storeButton.setLiked(false);
        }

        storeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                topic.setTopic_collection(1); // 设置选中
                topic.save();
                Toasty.info(TopicActivity.this,getString(R.string.collect),Toast.LENGTH_SHORT,true).show();
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                topic.setTopic_collection(0);
                //topic.setToDefault("topic_collection"); // 不选中的话，保持原来状态
                topic.save();
                Toasty.info(TopicActivity.this,getString(R.string.collect_cancel),Toast.LENGTH_SHORT,true).show();
            }
        });

    }


    /**
     * 初始化ImagesRecycler
     */
    private void initImagesRecycler() {

        //显示题干图片
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        topicOriginalAdapter = new TopicOriginalAdapter(this, topic);
        recyclerTopicOriginal.setLayoutManager(mLayoutManager);
        recyclerTopicOriginal.setItemAnimator(new DefaultItemAnimator());
        recyclerTopicOriginal.setAdapter(topicOriginalAdapter);

        //显示正解图片
        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(this);
        topicRightAdapter = new TopicRightAdapter(this, topic,rightLayout);
        recyclerTopicRight.setLayoutManager(mLayoutManager2);
        recyclerTopicRight.setItemAnimator(new DefaultItemAnimator());
        recyclerTopicRight.setAdapter(topicRightAdapter);

        //显示错解图片
        LinearLayoutManager mLayoutManager3 = new LinearLayoutManager(this);
        topicErrorAdapter = new TopicErrorAdapter(this, topic,errorLayout);
        recyclerTopicError.setLayoutManager(mLayoutManager3);
        recyclerTopicError.setItemAnimator(new DefaultItemAnimator());
        recyclerTopicError.setAdapter(topicErrorAdapter);

        //显示考点图片
        LinearLayoutManager mLayoutManager4 = new LinearLayoutManager(this);
        topicPointAdapter = new TopicPointAdapter(this, topic,pointLayout);
        recyclerTopicPoint.setLayoutManager(mLayoutManager4);
        recyclerTopicPoint.setItemAnimator(new DefaultItemAnimator());
        recyclerTopicPoint.setAdapter(topicPointAdapter);

        //显示错误原因图片
        LinearLayoutManager mLayoutManager5 = new LinearLayoutManager(this);
        topicErrorCauseAdapter = new TopicErrorCauseAdapter(this, topic,causeLayout);
        recyclerTopicErrorCause.setLayoutManager(mLayoutManager5);
        recyclerTopicErrorCause.setItemAnimator(new DefaultItemAnimator());
        recyclerTopicErrorCause.setAdapter(topicErrorCauseAdapter); //设置显示原图事件

    }

    /**
     * 初始化topicTextView
     */
    private void initTopicTextView(){

        setTextViewToShow(topic.getTopic_original_text(), originalText);
        setTextViewToShow(topic.getTopic_error_solution_text(), errorSolutionText);
        setTextViewToShow(topic.getTopic_right_solution_text(), rightSolutionText);
        setTextViewToShow(topic.getTopic_knowledge_point_text(), pointText);
        setTextViewToShow(topic.getTopic_error_cause_text(), causeText);
    }

    /**
     * 设置显示原图
     * @param isCheck 是否选中
     */
    private void setShowPrimitiveImages(boolean isCheck){
        if (isCheck){
            topicOriginalAdapter.showPrimitiveImages(true);
            topicRightAdapter.showPrimitiveImages(true);
            topicErrorAdapter.showPrimitiveImages(true);
            topicPointAdapter.showPrimitiveImages(true);
            topicErrorCauseAdapter.showPrimitiveImages(true);
            showPrimitiveImages.setTextColor(getColor(R.color.colorWhite));
        }else{
            topicOriginalAdapter.showPrimitiveImages(false);
            topicRightAdapter.showPrimitiveImages(false);
            topicErrorAdapter.showPrimitiveImages(false);
            topicPointAdapter.showPrimitiveImages(false);
            topicErrorCauseAdapter.showPrimitiveImages(false);
            showPrimitiveImages.setTextColor(getColor(R.color.blue_title));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        topic = LitePal.find(Topic.class, topicId);
        //设置标签
        initTag();
    }

    /**
     * @author lg
     * @date 2019/8/01
     * 手机的返回键执行和标题栏回退建相同的操作
     * */
    @Override
    public void onBackPressed() {
        if (isEditing){
            new AlertDialog.Builder(this).setMessage(R.string.save_data_when_quit)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isEditing = false;
                            topic = LitePal.find(Topic.class, topicId);
                            // 保存修改
                            topic.save();
                            finish();
                        }})
                    .setNegativeButton(R.string.unsave, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isEditing = false;
                            //还原原始状态
                            topicS.save();
                            finish();
                        }}).show();
        }else{
            finish();
        }
    }

    /**
     * 设置更多按钮点击弹出框
     * @param whichBtnString whichImage
     */
    private void setPopupWindow(String whichBtnString, final String dialogTitle, final boolean isEdit){

        whichModule = whichBtnString;
        View view = View.inflate(TopicActivity.this,R.layout.topic_popupmenu,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(TopicActivity.this).setView(view);

        builder.setTitle(dialogTitle);

        final AlertDialog dialog = builder.create();
        dialog.show();

        TextView addPicAlbum = view.findViewById(R.id.add_pic_album);
        TextView addPicCamera = view.findViewById(R.id.add_pic_camera);
        TextView addWord = view.findViewById(R.id.add_word);

        //监测权限是否分配
        if (!PhotoUtil.hasPermission) {
            PhotoUtil.checkPermissions(TopicActivity.this);
        }

        addPicAlbum.setOnClickListener(v -> {
            dialog.dismiss();
            startActivityForResult(CropActivity.getJumpIntent(TopicActivity.this,TAG,
                    true, whichModule,isEdit,true,topicId),ConstantsUtil.REQUEST_CODE);
        });

        addPicCamera.setOnClickListener(v -> {
            dialog.dismiss();
            startActivityForResult(CropActivity.getJumpIntent(TopicActivity.this,TAG,
                    false, whichModule,isEdit,true,topicId),ConstantsUtil.REQUEST_CODE);
        });

        addWord.setOnClickListener(v -> {
            dialog.dismiss();
            createAddWordDialog(dialogTitle);
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //设置是否显示原图按钮
            case R.id.show_original_images:
                setShowPrimitiveImages(showPrimitiveImages.isChecked());
                break;
            //题干：更多按钮
            case R.id.btn_original:
                setPopupWindow(ConstantsUtil.IMAGE_ORIGINAL,getString(R.string.stem),true);
                break;
            //正解：更多按钮
            case R.id.btn_right:
                setPopupWindow(ConstantsUtil.IMAGE_RIGHT,getString(R.string.right_solution),false);
                break;
            //错解：更多按钮
            case R.id.btn_error:
                setPopupWindow(ConstantsUtil.IMAGE_ERROR,getString(R.string.error_solution),false);
                break;
            //考点：更多按钮
            case R.id.btn_point:
                setPopupWindow(ConstantsUtil.IMAGE_POINT,getString(R.string.point),false);
                break;
            //错误原因：更多按钮
            case R.id.btn_cause:
                setPopupWindow(ConstantsUtil.IMAGE_REASON,getString(R.string.error_reason),false);
                break;
            case  R.id.set_tag:
                Intent intent = new Intent(TopicActivity.this, TagActivity.class);
                intent.putExtra(ConstantsUtil.TOPIC_ID, topicId);
                startActivity(intent);
            default:
                break;
        }
    }


    /***
     * @author qjj
     * 初始化标签布局，并响应点击事件
     */
    private void initTag() {

        //设置适配器
        tagLayout.setAdapter(new TagAdapter<Tag>(TagDaoImpl.findTagByTopic(LitePal.find(Topic.class, topicId).getTopic_tag())) {
            @Override
            public View getView(FlowLayout parent, int position, Tag tag) {
                CheckBox checkBox = (CheckBox) LayoutInflater.from(TopicActivity.this).inflate
                        (R.layout.flow_item_tag, tagLayout, false);
                checkBox.setText(tag.getTag_name());
                checkBox.setTextColor(Color.WHITE);
                checkBox.setChecked(true);
                checkBox.setClickable(false);
                return checkBox;
            }
        });
    }


    /**
     * images弹出框（展示图片）
     * @author lyw
     */
    public static void showImageBiggerDialog(List<String> imagesPath,Context context,int position){

        List<String> listImage = new ArrayList<>();
        for (int i = position; i < imagesPath.size(); i++) {
            listImage.add(imagesPath.get(i));
        }
        for (int i = 0; i < position; i++) {
            listImage.add(imagesPath.get(i));
        }
        List<?> imageUrls = new ArrayList<>(listImage);

        final Dialog dialog = new Dialog(context, R.style.dialog_topic);
        dialog.setContentView(R.layout.dialog_topic);

        final Banner banner = dialog.findViewById(R.id.banner);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(imageUrls);
        banner.isAutoPlay(false);
        banner.start();
        dialog.setCanceledOnTouchOutside(true);
        // 获取设备的宽高（得到的都是pixels的值）
        Window window = dialog.getWindow();
        assert window != null;
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //dialog的宽高
        layoutParams.width = PhotoUtil.getScreenWidth(context);
        layoutParams.height = PhotoUtil.getScreenHeight(context);
        dialog.onWindowAttributesChanged(layoutParams);
        dialog.show();
        banner.setOnBannerListener(position1 -> dialog.dismiss());
    }

    static class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            //Glide 加载图片简单用法
            Bitmap bitmap = PhotoUtil.getBitmapByImagePath((String) path);
            if (bitmap.getWidth() >= bitmap.getHeight()){
                int width = PhotoUtil.getScreenWidth(context);
                int height = width*bitmap.getHeight()/bitmap.getWidth();
                bitmap = PhotoUtil.resizeBitmap(bitmap,width,height);
            }else{
                int height = PhotoUtil.getScreenHeight(context)*3/4;
                int width = height*bitmap.getWidth()/bitmap.getHeight();
                bitmap = PhotoUtil.resizeBitmap(bitmap,width,height);
            }
            Glide.with(context).load(bitmap).into(imageView);
        }
    }

    /**
     * @author lg
     * @date 2019/07/24
     * @param topicText:文本框文字
     * @param textView：加载文本的控件
     */
    private void setTextViewToShow(String topicText, TextView textView) {
        if (topicText != null){
            //去除开头空格
            topicText = topicText.trim();
            if (!topicText.isEmpty()){
                textView.setVisibility(View.VISIBLE);
                textView.setText(topicText);
                return;
            }
        }
        textView.setVisibility(View.GONE);
    }

    /**
     * author LiuGen
     * @date 2019/07/24
     * 将隐藏布局设置为可见
     */
    private void makeComponentsVisible() {
        // 整体布局显示
        originalLayout.setVisibility(View.VISIBLE);
        rightLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.VISIBLE);
        pointLayout.setVisibility(View.VISIBLE);
        causeLayout.setVisibility(View.VISIBLE);
        // 工具栏布局显示
        originalMoreBtn.setVisibility(View.VISIBLE);
        rightMoreBtn.setVisibility(View.VISIBLE);
        errorMoreBtn.setVisibility(View.VISIBLE);
        pointMoreBtn.setVisibility(View.VISIBLE);
        errorCauseMoreBtn.setVisibility(View.VISIBLE);
    }

    /**
     * @author LiuGen
     * @date 2019/07/24
     * 设置错题详情页面的菜单选项；并根据传过来的不同类名显示不同内容
     * @date  2019/8/01
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //获取menu和子项对象
        getMenuInflater().inflate(R.menu.toolbar_menu_topic, menu);
        MenuItem item = menu.getItem(0);
        if(isEditing){
            item.setTitle(R.string.finish);
        }else{
            item.setTitle(R.string.edit);
        }
        return true;
    }

    /**
     * @author LiuGen
     * @date 2019/07/24
     * 响应错题详情页面菜单选项的点击事件
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // 标题栏编辑按钮，修改后将数据存入数据库
        if (item.getItemId() == R.id.topic_menu_edit) {
            if (item.getTitle().equals(getString(R.string.edit))){
                //编辑状态
                makeComponentsVisible();
                topicOriginalAdapter.notifyDataSetChanged();
                topicRightAdapter.notifyDataSetChanged();
                topicErrorAdapter.notifyDataSetChanged();
                topicPointAdapter.notifyDataSetChanged();
                topicErrorCauseAdapter.notifyDataSetChanged();
                isEditing = true;
                item.setTitle(R.string.finish);
            }else{
                /*完成*/
                isEditing = false;
                //保存之前一定要刷新topic
                Intent intent = new Intent(this, TopicActivity.class);
                intent.putExtra(ConstantsUtil.TOPIC_ID, topicId);
                intent.putExtra(ConstantsUtil.TOOLBAR_NAME, toolbarName);
                intent.putExtra(ConstantsUtil.WHICH_ACTIVITY, fromActivity);
                startActivity(intent);
                finish();
            }
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ConstantsUtil.REQUEST_CODE) {

                String whichImage;
                try {
                    whichImage = data.getStringExtra(ConstantsUtil.WHICH_IMAGE);
                }catch (Exception e){
                    whichImage = whichModule;
                    Log.w(TAG, "onActivityResult: "+whichImage,e );
                }

                //更新topic
                topic = LitePal.find(Topic.class,topic.getId());
                //根据whichPhoto来显示对应的图片
                switch (whichImage) {
                    case ConstantsUtil.IMAGE_ORIGINAL:
                        topicOriginalAdapter.flashImagePath();
                        break;
                    case ConstantsUtil.IMAGE_RIGHT:
                        topicRightAdapter.flashImagePath();
                        break;
                    case ConstantsUtil.IMAGE_REASON:
                        topicErrorCauseAdapter.flashImagePath();
                        break;
                    case ConstantsUtil.IMAGE_ERROR:
                        topicErrorAdapter.flashImagePath();
                        break;
                    case ConstantsUtil.IMAGE_POINT:
                        topicPointAdapter.flashImagePath();
                        break;
                    default:
                        break;
                }

                topic.save();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ConstantsUtil.REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PhotoUtil.hasPermission = true;
            } else {
                Toast.makeText(this, R.string.permission_fail, Toast.LENGTH_SHORT).show();
                PhotoUtil.hasPermission = false;
            }
        }
    }

    /**
     * 创建添加文字dialog
     * @author zc
     */
    private void createAddWordDialog(String dialogTitle) {

        /*自定义的EditText布局样式*/
        @SuppressLint("InflateParams")
        LinearLayout addWordView = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_add_text_info, null);
        editTextView = addWordView.findViewById(R.id.add_page_text);

        //设置编辑框（添加已有文字）
        setAddWordEditTextView(null,false);

       AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(dialogTitle).setView(addWordView)
                //添加
            .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setAddWordEditTextView(editTextView.getText().toString(),true);
                    dialog.dismiss(); }})
                //取消
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editTextView.setText("");
                    dialog.dismiss(); }})
                //清空
            .setNeutralButton(R.string.empty, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editTextView.setText("");
            }
        }).setCancelable(false);

        //添加文字内容的对话框
        AlertDialog dialog = builder.create();
        dialog.show();

        // 这里来设置neutral的监听事件，这样可以防止dialog自动退出的问题
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextView.setText("");
            }
        });


    }


    /**
     * 初始化其中已有内容
     * @author zc
     */
    private void setAddWordEditTextView(String setTextString, boolean isSet) {

        String getTextString = null;

        switch (whichModule) {
            case ConstantsUtil.IMAGE_ORIGINAL:
                getTextString = topic.getTopic_original_text();
                if (isSet){
                    topic.setTopic_original_text(setTextString);
                    topic.save();
                    setTextViewToShow(setTextString,originalText);
                }
                break;
            case ConstantsUtil.IMAGE_RIGHT:
                getTextString = topic.getTopic_right_solution_text();
                if (isSet){
                    topic.setTopic_right_solution_text(setTextString);
                    topic.save();
                    setTextViewToShow(setTextString,rightSolutionText);
                }
                break;
            case ConstantsUtil.IMAGE_REASON:
                getTextString = topic.getTopic_error_cause_text();
                if (isSet){
                    topic.setTopic_error_cause_text(setTextString);
                    topic.save();
                    setTextViewToShow(setTextString,causeText);
                }
                break;
            case ConstantsUtil.IMAGE_ERROR:
                getTextString = topic.getTopic_error_solution_text();
                if (isSet){
                    topic.setTopic_error_solution_text(setTextString);
                    topic.save();
                    setTextViewToShow(setTextString,errorSolutionText);
                }
                break;
            case ConstantsUtil.IMAGE_POINT:
                getTextString = topic.getTopic_knowledge_point_text();
                if (isSet){
                    topic.setTopic_knowledge_point_text(setTextString);
                    topic.save();
                    setTextViewToShow(setTextString,pointText);
                }
                break;
            default:
                break;
        }

        if(getTextString != null && !isSet){
            editTextView.setText(getTextString);
            //设置光标位置
            editTextView.setSelection(getTextString.length());
        }else{
            editTextView.setText("");
        }

    }
}
