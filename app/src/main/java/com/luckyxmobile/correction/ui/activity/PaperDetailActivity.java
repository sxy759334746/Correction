package com.luckyxmobile.correction.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.PaperDetailAdapter;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Paper;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.PaperTopicDao;
import com.luckyxmobile.correction.dao.impl.PaperTopicDaoImpl;
import com.luckyxmobile.correction.ui.callback.ItemTouchCallback;
import com.luckyxmobile.correction.util.PdfUtils;
import com.luckyxmobile.correction.util.ProgressDialogUtil;
import com.luckyxmobile.correction.util.ThreadPool;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * @author ChangHao
 */
public class PaperDetailActivity extends AppCompatActivity {

    public static final String TAG = "PaperDetailActivity";
    private final int REQ_NEXT = 1;

    private String reviewPaperId;
    private RecyclerView recyclerView;
    private Toolbar reviewPaperDetailToolBar;
    private PaperDetailAdapter paperDetailAdapter;

    private PaperTopicDao paper_topic = new PaperTopicDaoImpl();
    private List<Topic> topicList = new ArrayList<>();
    private List<Book> booksList = new ArrayList<>();
    private Paper paper;
    private Handler handler = new Handler();

    /**用于防止多次按打印按钮*/
    private boolean canPrint = true;

    /**
     * 传递错题卷的名称到TopicActivity
     */
    private String paperName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_paper_detail);

        reviewPaperId = getIntent().getStringExtra("reviewPaperId");
        initToolbar();
        // 先执行该方法，其中获取到当前错题卷名称
        refreshDataFromDataBase();
        initDisplay();
    }

    /**
     * 从数据库中获取当前的错题卷对象
     */
    private void refreshDataFromDataBase() {
        // 使用PaperDetailAdapter的时候把这个list传了过去, 所以list不可以new一个新的, 只能在原本的list上修改
        topicList.clear();
        // 从数据库中获取数据
        List<Topic> topicList = paper_topic.selectPaper(Integer.valueOf(reviewPaperId));
        paper = LitePal.find(Paper.class, Integer.valueOf(reviewPaperId));
        // 获取到当前错题卷的名称
        paperName = paper.getPaper_name();
        this.topicList.addAll(topicList);
        reviewPaperDetailToolBar.setTitle(paper.getPaper_name());
        if (paperDetailAdapter != null) {
            paperDetailAdapter.notifyDataSetChanged();
        }
        booksList = LitePal.findAll(Book.class);

    }

    /**
     * 初始化组件
     */
    private void initDisplay() {

        recyclerView = findViewById(R.id.review_paper_recycleview);
        //'重新组卷'按钮 设置按键监听
//        reGroup_btn.setOnClickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        paperDetailAdapter = new PaperDetailAdapter(this, topicList, paperName);
        recyclerView.setAdapter(paperDetailAdapter);
        //给复习卷的item设置长按拖动
        ItemTouchHelper.Callback callback = new ItemTouchCallback(paperDetailAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    //使用反射的方法 让menu里面可以显示图标
//    @Override
//    public boolean onMenuOpened(int featureId, Menu menu) {
//        if (menu != null) {
//            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
//                try {
//                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
//                    method.setAccessible(true);
//                    method.invoke(menu, true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return super.onMenuOpened(featureId, menu);
//    }

    /**
     * @author lg
     * @date 2019/08/01
     * 为了方便当前活动向PaperDetailActivity传值，在此对Toolbar初始化的方法单独拆分出来
     */
    public void initToolbar() {
        reviewPaperDetailToolBar = findViewById(R.id.review_paper_detail_toolbar);
        //获取界面的toolbar, 然后给它设标题
        setSupportActionBar(reviewPaperDetailToolBar);
        ActionBar actionBar = getSupportActionBar();
        //androidx 需要手动设置这个参数,才能让默认的home键上的返回图标显示出来 v7貌似不用
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //给home图标(返回)设置点击返回事件
        reviewPaperDetailToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 加载菜单选项
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_review_paper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.review_paper_print:
                if(!canPrint)break;
                canPrint = false;
                ProgressDialogUtil.showProgressDialog(this);
                ThreadPool.singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            PdfUtils.printPreviewWindow(PaperDetailActivity.this, topicList, booksList, paper);
                        }catch (IllegalArgumentException e){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.warning(PaperDetailActivity.this, R.string.paper_empty_cant_print, Toast.LENGTH_SHORT).show();
                                    ProgressDialogUtil.dismiss();
                                    canPrint = true;
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.review_paper_regroup_menu:
                //跳转到试卷题重组页面
                LitePal.deleteAll(Topic.class, "book_id = ?", "0");
                Intent intent = new Intent(PaperDetailActivity.this, SelectTopicActivity.class);
                intent.putExtra("from", "PaperDetailActivity");
                intent.putExtra("reviewPaperId", reviewPaperId);
                startActivityForResult(intent, REQ_NEXT);
                break;
            default:
                Log.d(TAG, "onOptionsItemSelected: switch error");
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ProgressDialogUtil.dismiss();
        refreshDataFromDataBase();
        canPrint=true;

    }

}
