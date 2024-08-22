package com.luckyxmobile.correction.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.adapter.BookDetailAdapter;
import com.luckyxmobile.correction.bean.Book;
import com.luckyxmobile.correction.bean.Tag;
import com.luckyxmobile.correction.bean.Topic;
import com.luckyxmobile.correction.dao.impl.TagDaoImpl;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.DestroyActivityUtil;
import com.luckyxmobile.correction.util.PhotoUtil;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 * @author LiuGen
 * @date 2019/7/23
 *
 * @date 2019/08/03
 * @author lg
 * 修缮该页面item布局
 *
 * @date 2019/10/12
 * @author lg
 * 为错题本详情界面添加标签索引
 * */

public class BookDetailActivity extends AppCompatActivity {

    public static final String TAG = "BookDetailActivity";

    private RecyclerView recyclerView;
    private BookDetailAdapter adapter;
    private Toolbar toolbar;
    private TagFlowLayout tagNavigationLayout;
    private ImageView topicNothingImage;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Book book;
    private int book_id;
    private List<Topic> topics;
    private List<Tag> tags = new ArrayList<>();
    private boolean isNewest = true;
    /**添加错题按钮、删除按钮、从旧到新、从新到旧*/
    private MenuItem addTopic = null,deleteTopic = null,oldestFirst,newestFirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去除顶部标题栏
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_book_detail);

        initViewData();

        initToolBar();

        initTag();

        initRecyclerView();

    }

    private void initViewData() {

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_correction);
        tagNavigationLayout = findViewById(R.id.tags_navigation);
        topicNothingImage = findViewById(R.id.book_topic_nothing);

        // 获取Intent传过来的数据集
        book_id = getIntent().getIntExtra(ConstantsUtil.BOOK_ID, 0);
        book = LitePal.find(Book.class, book_id);
        // 由错题本获取其中所有存储的错题
        if (book.getBook_cover().equals("R.mipmap.favorite")){
            //收藏
            topics = LitePal.where("topic_collection=?", "1").find(Topic.class);
        }else{
            topics = LitePal.where("book_id=?", String.valueOf(book_id)).find(Topic.class);
        }

        preferences = getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);

        if (preferences.getBoolean(ConstantsUtil.TABLE_SHARED_IS_NEWEST_ORDER,true)){
            Collections.reverse(topics);
            isNewest = true;
        }else{
            isNewest = false;
        }

        // 获取到当前所有的Tags
        tags = TagDaoImpl.findTagsByTopics(topics);
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);// 显示返回键
            actionBar.setDisplayShowTitleEnabled(false);// 不显示默认标题
        }
        toolbar.setTitle(book.getBook_name());
        //toolbar的返回按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initTag() {

        // 加载标签导航布局的Adapter
        tagNavigationLayout.setAdapter(new TagAdapter<Tag>(tags) {
            @Override
            public View getView(FlowLayout parent, int position, Tag tag) {
                CheckBox checkBox = (CheckBox) LayoutInflater.from(BookDetailActivity.this).inflate
                        (R.layout.flow_item_tag_on_navigation, parent, false);
                checkBox.setText(tag.getTag_name());
                return checkBox;
            }
        });

        // 通过调用Adapter中的搜索过滤方法，达到点击标签进行分类显示的目的
        tagNavigationLayout.setOnSelectListener(selectPosSet -> {

            List<Integer> SelectedPositions = new ArrayList<>(selectPosSet);

            StringBuilder tagsum = new StringBuilder();
            for (int i = 0; i < SelectedPositions.size(); i++) {
                Tag tag = tags.get(SelectedPositions.get(i));
                if (i != SelectedPositions.size()-1){
                    tagsum.append(tag.getTag_name()).append(",");
                }else{
                    tagsum.append(tag.getTag_name());
                }
            }
            adapter.getFilter().filter(tagsum.toString());
        });
    }

    private void initRecyclerView() {

        LinearLayoutManager manager = new LinearLayoutManager(BookDetailActivity.this);
        adapter = new BookDetailAdapter(BookDetailActivity.this, topics, book_id);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemListener(new BookDetailAdapter.onItemListener() {
            @Override
            public void onItemLongClickListener(int position) {
                //删除图标显示，添加图标隐藏
                deleteTopic.setVisible(true);
                addTopic.setVisible(false);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // 切换其他应用回到当前活动后 删除图标消失
        if (deleteTopic !=null && deleteTopic.isVisible()) {
            deleteTopic.setVisible(false);
            //删除图标隐藏，添加图标显示
            addTopic.setVisible(true);
        }

        if (topics.isEmpty()){
            topicNothingImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        BookDetailAdapter.mShowDelete = false;

    }

    @Override
    public void onBackPressed() {
        // back键取消删除图标
        if (BookDetailAdapter.mShowDelete) {
            BookDetailAdapter.mShowDelete = false;
            // 更新adapter，取消删除图标的同时取消选中的item
            adapter.notifyDataSetChanged();
            deleteTopic.setVisible(false);
            addTopic.setVisible(true);
            //此活动返回时 将错题是否是从收藏夹添加的 标记为false
            editor = preferences.edit();
            editor.putBoolean(ConstantsUtil.IF_FROM_FAVORITE, false);
            editor.apply();
            return;
        }
        super.onBackPressed();
    }


    /**
     * @author lg
     * @date 2019/08/23
     * 当活动不可见的时候，要将删除图标也设置为不可见；
     * 修复 之前活动不可见之后再次进入，删除图标仍显示 的问题
     * */
    @Override
    protected void onStop() {
        super.onStop();
        // 当活动不可见的时候，要将删除图标也设置为不可见
        BookDetailAdapter.mShowDelete = false;
        if(deleteTopic !=null){
            deleteTopic.setVisible(false);
            addTopic.setVisible(true);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * @author LiuGen
     * @function 为Toolbar加载子项样式
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_bookdetail, menu);

        deleteTopic = menu.findItem(R.id.book_topic_delete);
        addTopic = menu.findItem(R.id.add_topic_toolbar);
        oldestFirst = menu.findItem(R.id.sort_oldest_first);
        newestFirst = menu.findItem(R.id.sort_newest_first);

        if (preferences.getBoolean(ConstantsUtil.TABLE_SHARED_IS_NEWEST_ORDER,true)){
            newestFirst.setTitle(getString(R.string.newest_order) + "   √");
            oldestFirst.setTitle(getString(R.string.oldest_order));
        }else{
            newestFirst.setTitle(getString(R.string.newest_order));
            oldestFirst.setTitle(getString(R.string.oldest_order) + "   √");
        }


        return true;
    }

    /**
     * @author lg
     * @function 为Toolbar子项设置点击事件
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //删除选中错题
            case R.id.book_topic_delete:
                deleteSelectedTopic();
                break;
            // 从新到旧
            case R.id.sort_newest_first:
                setTopicsSort(true);
                break;
            // 从旧到新
            case R.id.sort_oldest_first:
                setTopicsSort(false);
                break;
            //在错题本内添加错题
            case R.id.add_topic_toolbar:
                createAddTopicDialog();
                break;
            default:
                break;
        }
        return true;
    }

    private void deleteSelectedTopic() {
        new AlertDialog.Builder(BookDetailActivity.this)
                .setTitle(R.string.confirm_delete)
                .setIcon(R.drawable.ic_delete_red_24dp)
                .setMessage(R.string.confirm_delete_topic)
                .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.deleteTopics();
                        // 重新获取删除更新后的topics列表
                        topics = LitePal.where("book_id=?", String.valueOf(book_id)).find(Topic.class);
                        tags = TagDaoImpl.findTagsByTopics(topics);
                        tagNavigationLayout.onChanged();
                        adapter.upTopics(topics);
                        BookDetailAdapter.mShowDelete = false;
                        deleteTopic.setVisible(false);
                        addTopic.setVisible(true);
                    }
                }).setNegativeButton(R.string.cancel, null).show();

        if (topics.isEmpty()){
            topicNothingImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void setTopicsSort(boolean sortNewestFirst){

        if (isNewest != sortNewestFirst){
            //对题目排序
            Collections.reverse(topics);
            adapter.upTopics(topics);
            isNewest = sortNewestFirst;
        }

        //从旧到新
        if (!sortNewestFirst){
            newestFirst.setTitle(getString(R.string.newest_order));
            oldestFirst.setTitle(getString(R.string.oldest_order) + "   √");
        }else{
            newestFirst.setTitle(getString(R.string.newest_order) + "   √");
            oldestFirst.setTitle(getString(R.string.oldest_order));
        }

        editor = preferences.edit();
        editor.putBoolean(ConstantsUtil.TABLE_SHARED_IS_NEWEST_ORDER,sortNewestFirst);
        editor.apply();
    }

    private void createAddTopicDialog() {
        //Toast.makeText(this, String.valueOf(book.getId()), Toast.LENGTH_SHORT).show();
        editor = preferences.edit();
        editor.putInt(ConstantsUtil.TABLE_FROM_BOOK_ID, book_id);
        editor.apply();

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(BookDetailActivity.this).inflate(R.layout.upload_topic_dialog,null);
        new AlertDialog.Builder(BookDetailActivity.this).setView(view).show();

        Button uploadCamera = view.findViewById(R.id.upload_topic_dialog_camera);
        Button uploadAlbum = view.findViewById(R.id.upload_topic_dialog_album);
        uploadCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTopicInBook(false);
            }
        });

        uploadAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               addTopicInBook(true);
            }
        });
    }

    private void addTopicInBook(boolean fromAlbum){

        if (!PhotoUtil.hasPermission) {
            //监测权限是否分配
            PhotoUtil.checkPermissions(BookDetailActivity.this);
        }

        //在此处判断是否是从收藏夹里添加的错题 记录到SharedPreferences 中
        preferences = getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION, MODE_PRIVATE);
        editor = preferences.edit();
        if(book.getId()==1){
            editor.putBoolean(ConstantsUtil.IF_FROM_FAVORITE,true);
            editor.apply();
        }

        startActivityForResult(CropActivity.getJumpIntent(BookDetailActivity.this,MainActivity.TAG,
                fromAlbum,ConstantsUtil.IMAGE_ORIGINAL,true,true,-1),ConstantsUtil.REQUEST_CODE);

        DestroyActivityUtil.addDestroyActivityToMap(BookDetailActivity.this,MainActivity.TAG);

    }

}
