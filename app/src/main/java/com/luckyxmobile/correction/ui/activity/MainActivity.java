package com.luckyxmobile.correction.ui.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.HeadBookAdapter;
import com.luckyxmobile.correction.adapter.RecentTopicAdapter;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.CorrectionLab;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;
import com.luckyxmobile.correction.ui.view.AddBookDialog;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.luckyxmobile.correction.util.SDCardUtil;
import com.noober.menu.FloatMenu;
import com.wang.avi.AVLoadingIndicatorView;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import org.litepal.LitePal;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import es.dmoral.toasty.Toasty;
import me.pqpo.smartcropperlib.SmartCropper;


/**
 * @author qjj
 */
public class MainActivity extends AppCompatActivity implements  View.OnClickListener {

    public static final String TAG = "MainActivity";
    private long lastClickTime = 0L;
    private Toolbar toolbar;
    private SearchView mSearchView;
    private SwipeRecyclerView headBook;
    private HeadBookAdapter headBookAdapter;
    private SwipeRecyclerView recentTopic;
    public static TextView recentTopicText;
    private WindowManager windowManager;
    private View view;
    private int StartY,StartX;
    private TranslateAnimation showAnim,hideAnim;//Fab组的滑动动画效果
    private FloatingActionsMenu floatActionMenu,floatingActionsMenuWindow;
    private  List<Book> books;
    private RecentTopicAdapter recentTopicAdapter;
    private int recentIndex = 0;
    private NestedScrollView scrollView;
    /**上拉加载动画*/
    private AVLoadingIndicatorView loadingIndicatorView;
    private List<Topic> topics;
    private AddBookDialog addBookDialog;

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
            } else {
                super.onManagerConnected(status);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //初始化openCV
        initOpenCV();

        //判断是否首次安装,用于初始化数据库
        firstLog();

        //初始化动画
        initAnim();

        initView();

        //初始化智能剪裁
        SmartCropper.buildImageDetector(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSearchView.clearFocus();

        //删去没有在错题本内的错题
        LitePal.deleteAll(Topic.class, "book_id = ?", "0");

        //初始化错题本布局
        initHeadBook();

        //初始化最近错题布局
        initRecentTopics();

    }


    private void initOpenCV() {
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallBack);
        } else {
            Log.i(TAG, "openCV successful");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            System.out.println(Runtime.getRuntime().maxMemory());
        }
    }

    private void firstLog() {
        SharedPreferences preferences = getSharedPreferences("init", MODE_PRIVATE);
        if (preferences.getBoolean("firstStart", true)) {
            SharedPreferences.Editor editor = preferences.edit();
            // 将登录标志位设置为false，下次登录时不在初始化
            editor.putBoolean("firstStart", false);
            editor.apply();
            //初始化数据库
            Book book = new Book();
            book.setBook_name(getString(R.string.favorite));
            book.setBook_cover("R.mipmap.favorite");
            book.save();
            TagDaoImpl.newTag("重要");
            TagDaoImpl.newTag("选择题");
            TagDaoImpl.newTag("填空题");
            TagDaoImpl.newTag("知识点");
            // 检查是否有存储和拍照权限
            PhotoUtil.checkPermissions(MainActivity.this);
        }
    }

    private void initAnim(){
        showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnim.setDuration(500);
        hideAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        hideAnim.setDuration(500);
    }

    private void initView() {

        toolbar = findViewById(R.id.toolbar);
        TextView addBookBtn = findViewById(R.id.add_book_main);
        mSearchView = findViewById(R.id.main_top_search);
        headBook = findViewById(R.id.main_recycler_book);
        recentTopic = findViewById(R.id.main_recycler_recent_topic);
        floatActionMenu = findViewById(R.id.fab_menu);
        scrollView = findViewById(R.id.scrollView);
        loadingIndicatorView = findViewById(R.id.avi);
        recentTopicText = findViewById(R.id.recent_topic_text);

        addBookBtn.setOnClickListener(this);

        setSupportActionBar(toolbar);

        initWindowFAB();//设置FAB的展开和关闭

        setSearchViewOnClickListener(mSearchView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initHeadBook(){
        books = LitePal.findAll(Book.class);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this,4);
        headBookAdapter = new HeadBookAdapter(this,books);
        headBook.setLayoutManager(mLayoutManager);
        headBook.setItemAnimator(new DefaultItemAnimator());
        headBook.setNestedScrollingEnabled(false);//解决卡顿
        headBook.setAdapter(headBookAdapter);
        headBookAdapter.setmOnItemListener(new HeadBookAdapter.onItemListener() {
            @Override
            public void onItemClickListener(HeadBookAdapter.ViewHolder viewHolder, int position) {
                //防止多次点击
                if (System.currentTimeMillis() - lastClickTime < ConstantsUtil.MIN_CLICK_DELAY_TIME){
                    return;
                }
                lastClickTime = System.currentTimeMillis();
                Intent intent = new Intent(MainActivity.this,BookDetailActivity.class);
                intent.putExtra(ConstantsUtil.BOOK_ID,books.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClickListener(HeadBookAdapter.ViewHolder viewHolder,final int bookPosition,View view) {
                //长按出现菜单
                Animation(view);
                if(bookPosition!=0) {
                    FloatMenu menu = new FloatMenu(MainActivity.this);
                    menu.items(350, getResources().getString(R.string.delete),getResources().getString(R.string.change_book_info));
                    menu.show(point);
                    //pop 菜单点击事件
                    menu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                        @Override
                        public void onClick(View v, int position) {
                            switch (position){
                                case 0:
                                    //删除错题本
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.confirm_delete)
                                            .setIcon(R.drawable.ic_delete_red_24dp)
                                            .setMessage(R.string.delete_book)
                                            .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //删除错题本的数据库操作
                                                    Book book = books.get(bookPosition);
                                                    books.remove(bookPosition);
                                                    SDCardUtil.cascadeDeleteBook(book.getId(),MainActivity.this);
                                                    CorrectionLab.deleteBook(book.getId());
                                                    //迭代器删除
                                                    Iterator<Topic> topicIterator = topics.iterator();
                                                    while (topicIterator.hasNext()) {
                                                        Topic topic = topicIterator.next();
                                                        if (topic.getBook_id() == book.getId()){
                                                            topicIterator.remove();
                                                        }
                                                    }
                                                    headBookAdapter.notifyDataSetChanged();
                                                    recentTopicAdapter.deleteTopic(book.getId());
                                                    recentTopicAdapter.notifyDataSetChanged();
                                                }
                                            })
                                            .setNegativeButton(R.string.cancel,null).show();

                                    break;

                                case 1:
                                    addBookDialog =  new AddBookDialog(MainActivity.this,books.get(bookPosition),false);
                                    addBookDialog.setPositiveButton(R.string.ensure, (dialogInterface, i) -> {
                                        headBookAdapter.setBook(bookPosition,addBookDialog.getSaveBook());
                                        headBookAdapter.notifyDataSetChanged();
                                    }).setNegativeButton(R.string.cancel,null).show();
                                    addBookDialog.getAlterBookCoverBtn().setOnClickListener(view ->
                                            startActivityForResult(CropActivity.getJumpIntent(MainActivity.this,TAG,
                                            true, ConstantsUtil.IMAGE_BOOK_COVER,false,false,0),100));
                                    PhotoUtil.resetResultPath();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }

            }

        });


    }

    private void initRecentTopics(){
        topics = LitePal.findAll(Topic.class);  //数据库中的topics
        Collections.reverse(topics);

        toolbar.setFocusable(true);
        toolbar.setFocusableInTouchMode(true);
        toolbar.requestFocus();
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                   if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                       //滑动到底部 目前触发两次
                       Log.d(TAG, "onScrollChange: ");

                       if (recentTopicAdapter.index<recentTopicAdapter.getGroupCount()) {
                           recentIndex=recentTopicAdapter.index++;
                           Log.d(TAG, "notifyItemRangeChanged1 "+recentIndex);
                           loadingIndicatorView.smoothToShow(); //加载动画显示
                           //动画延迟消失，更新adapter
                           new Handler().postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   loadingIndicatorView.smoothToHide();
                                   Log.d(TAG, "notifyItemRangeChanged2 "+recentIndex);
                                   recentTopicAdapter.notifyItemRangeChanged(recentIndex*5,5);

                               }
                           },500);

                       }
                   }
                }
            });

        recentTopicAdapter = new RecentTopicAdapter(this, topics);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recentTopic.setLayoutManager(mLayoutManager);
        recentTopic.setFocusable(false);
        recentTopic.setItemAnimator(new DefaultItemAnimator());
        recentTopic.setNestedScrollingEnabled(false);//解决卡顿
        recentTopic.setAdapter(recentTopicAdapter);

    }


    /**
     * SearchView设置不能输入
     */
    public static void setSearchViewOnClickListener(View view,View.OnClickListener listener){
        if (view instanceof ViewGroup){
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++){
                View child = group.getChildAt(i);
                if (child instanceof LinearLayout || child instanceof RelativeLayout){
                    setSearchViewOnClickListener(child,listener);
                }
                if (child instanceof TextView){
                    TextView textView = (TextView)child;
                    textView.setFocusable(false);
                }
                child.setOnClickListener(listener);
            }
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data.getBooleanExtra(ConstantsUtil.IMAGE_BOOK_COVER,false)){
                addBookDialog.setBookCover();
            }
        }
    }

    //点击事件
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.add_book_main:
                floatingActionsMenuWindow.collapse();
                addBookDialog = new AddBookDialog(this,new Book(),true);
                addBookDialog.setPositiveButton(R.string.ensure, (dialogInterface, i) -> {
                    headBookAdapter.addBook(addBookDialog.getSaveBook());
                    headBookAdapter.notifyDataSetChanged();
                }).setNegativeButton(R.string.cancel,null).show();
                addBookDialog.getAlterBookCoverBtn().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(CropActivity.getJumpIntent(MainActivity.this,TAG,
                                true, ConstantsUtil.IMAGE_BOOK_COVER,false,false,0),100);
                    }
                });
                Log.v(TAG, "onClick: "+"添加错题本");
                break;
            case R.id.fab_camera:
                //每次使用相机前重置resultPath
                PhotoUtil.resetResultPath();
                if (!PhotoUtil.hasPermission) {//当没有权限的时候再申请权限
                    //监测权限是否分配
                    PhotoUtil.checkPermissions(MainActivity.this);
                }
                floatingActionsMenuWindow.collapse();
                startActivityForResult(CropActivity.getJumpIntent(MainActivity.this,TAG,
                        false, ConstantsUtil.IMAGE_ORIGINAL,true,true,-1),100);
                Log.v(TAG, "onClick: "+"拍照上传错题");
                break;
            case R.id.fab_album:
                //每次使用相机前重置resultPath
                PhotoUtil.resetResultPath();
                if (!PhotoUtil.hasPermission) {//当没有权限的时候再申请权限
                    //监测权限是否分配
                    PhotoUtil.checkPermissions(MainActivity.this);
                }
                floatingActionsMenuWindow.collapse();
                startActivityForResult(CropActivity.getJumpIntent(MainActivity.this,TAG,
                        true,ConstantsUtil.IMAGE_ORIGINAL,true,true,-1),100);
                Log.v(TAG, "onClick: "+"相册上传错题");
                break;

            default:
                break;

        }
        //使用完后重置
        PhotoUtil.resetResultPath();
    }

    /**
     * 对于FAB的展开和关闭的设置
     * floatActionMenu.collapse();//关闭按钮组
     * floatActionMenu.expand();//展开按钮组
     */
    public void initWindowFAB(){
        windowManager  = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.window_fab, null);//获取到新建window的布局
        view.setFocusableInTouchMode(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenuWindow.collapse();
                windowManager.removeView(view);
                floatActionMenu.setVisibility(View.VISIBLE);
            }
        });

        floatingActionsMenuWindow = view.findViewById(R.id.fab_menu);
        FloatingActionButton fabCamera = view.findViewById(R.id.fab_camera);
        FloatingActionButton fabAlbum = view.findViewById(R.id.fab_album);
        //为FAB设置关闭时将window中的view移除
        floatingActionsMenuWindow.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {

            }
            @Override
            public void onMenuCollapsed() {
                floatingActionsMenuWindow.collapse();
                windowManager.removeView(view);
                floatActionMenu.setVisibility(View.VISIBLE);
            }
        });
        //为FAB设置展开时新建一个window
        floatActionMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                floatActionMenu.collapse();
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.gravity = Gravity.CENTER;
                if (Build.VERSION.SDK_INT > 18 && Build.VERSION.SDK_INT < 25){
                    layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                } else {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
                }
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
                layoutParams.format = PixelFormat.RGBA_8888;
                floatingActionsMenuWindow.expand();
                windowManager.addView(view,layoutParams);
                floatActionMenu.setVisibility(View.GONE);
            }
            @Override
            public void onMenuCollapsed() {

            }
        });

        //FAB按钮组的点击事件
        fabCamera.setOnClickListener(this);
        fabAlbum.setOnClickListener(this);
    }

    //全局变量point记录手指位置
    private Point point =new Point();
    /**
     * @param ev
     * @return
     * 主要用于判断手指在屏幕上的滑动方向，注意不能在onTouchEvent中使用，因为viewPager会截获
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 一定要spuer，否则事件打住,不会在向下调用了
        super.dispatchTouchEvent(ev);
        switch (ev.getAction()) {
            // 记录用户手指点击的位置
            case MotionEvent.ACTION_DOWN:
                StartX = (int) ev.getRawX();
                point.x = (int) ev.getRawX();
                point.y = (int) ev.getRawY();
                StartY = (int) ev.getRawY();
                Log.d(TAG, "StartX = " + StartX+",StartY="+StartY);
                break;
            case MotionEvent.ACTION_UP:
                int upX = (int) ev.getRawX();
                int upY = (int) ev.getRawY();
                Log.d(TAG, "UpX = " + upX +",upY="+ upY);
                if(upY -StartY>80){
                    if(floatActionMenu.getVisibility()!=View.VISIBLE){
                        floatActionMenu.startAnimation(showAnim);
                        floatActionMenu.setVisibility(View.VISIBLE);
                    }
                }
                if(StartY- upY >80 && LitePal.findAll(Topic.class).size() > 5){
                    if(floatActionMenu.getVisibility()!=View.GONE){
                        floatActionMenu.startAnimation(hideAnim);
                        floatActionMenu.setVisibility(View.GONE);
                    }
                }
                break;
        }
        return false;// return false,继续向下传递，return true;拦截,不向下传递
    }

    /**
     * 加载菜单选项
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_paper:
                startActivity(new Intent(MainActivity.this, PaperActivity.class));
                break;
            case R.id.main_set:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;

                default:
                    break;
        }

        return super.onOptionsItemSelected(item);
    }
    public void Animation(View v){
        Animation animation =  AnimationUtils.loadAnimation(MainActivity.this,R.anim.layout_longpress);
        v.startAnimation(animation);
    }

}
